# Checks the library's OWN TESTS against the library's real signatures.
# The previous two CI failures were both here: a test calling a function with
# the wrong argument type, and a test reading a property off the wrong type.
import re, pathlib, sys

# Resolved from this file, never from an absolute path: a checker that points at
# a directory which no longer exists indexes nothing and then reports success,
# which is worse than having no checker at all.
REPO = pathlib.Path(__file__).resolve().parent.parent
ROOT = REPO / "trial3lib" / "src"
if not ROOT.is_dir():
    sys.exit("cannot find %s -- checker would silently pass" % ROOT)
MAIN = ROOT / "main/kotlin"
TEST = ROOT / "test/kotlin"

fails = []

# ---- 1. collect declared signatures of every public/internal library function
DECL = re.compile(r'^\s*(?:public |internal |private )?fun\s+(?:<[^>]*>\s+)?(?:[A-Za-z0-9_.<>?]+\.)?([A-Za-z_][A-Za-z0-9_]*)\s*\(([^\n]*)', re.M)

def split_params(text):
    out, depth, cur = [], 0, ""
    for ch in text:
        if ch in "(<[": depth += 1
        elif ch in ")>]": depth -= 1
        if ch == "," and depth == 0:
            out.append(cur); cur = ""
        else:
            cur += ch
    if cur.strip(): out.append(cur)
    return [p.strip() for p in out if p.strip()]

sigs = {}   # name -> list of [(pname, ptype)]
for f in sorted(MAIN.rglob("*.kt")):
    src = f.read_text(encoding="utf-8", errors="replace")
    for m in DECL.finditer(src):
        name = m.group(1)
        # gather the parameter list across lines, balancing parens
        i = src.index("(", m.start())
        depth, j = 0, i
        while j < len(src):
            if src[j] == "(": depth += 1
            elif src[j] == ")":
                depth -= 1
                if depth == 0: break
            j += 1
        raw = src[i+1:j]
        params = []
        for p in split_params(raw):
            p = re.sub(r'^(?:vararg|crossinline|noinline)\s+', '', p)
            if ":" not in p: continue
            pn, pt = p.split(":", 1)
            pt = pt.split("=")[0].strip()
            params.append((pn.strip(), pt))
        sigs.setdefault(name, []).append(params)

# ---- 2. collect member/property names of every data class and its type
CLS = re.compile(r'^\s*(?:public |internal )?(?:data )?class\s+([A-Za-z_][A-Za-z0-9_]*)\s*(?:\(|\{)', re.M)
members = {}
for f in sorted(MAIN.rglob("*.kt")):
    src = f.read_text(encoding="utf-8", errors="replace")
    for m in CLS.finditer(src):
        cname = m.group(1)
        # body: from the class head to the matching brace/paren block end
        tail = src[m.start():]
        props = set(re.findall(r'\b(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*:', tail[:4000]))
        funs  = set(re.findall(r'\bfun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(', tail[:4000]))
        members.setdefault(cname, set()).update(props | funs)

# ---- 3. inspect every call in the tests to a library function
LIT_INT = re.compile(r'^0[xX][0-9A-Fa-f]+L?\.toInt\(\)$|^-?\d+$|^0[xX][0-9A-Fa-f]+L?$')
LIT_STR = re.compile(r'^".*"$')
LIT_BOOL = re.compile(r'^(true|false)$')
SCALAR = {"Int": LIT_INT, "String": LIT_STR, "Boolean": LIT_BOOL}

calls_checked = 0
for f in sorted(TEST.rglob("*.kt")):
    src = f.read_text(encoding="utf-8", errors="replace")
    for name, overloads in sigs.items():
        for m in re.finditer(r'(?<![A-Za-z0-9_.])' + re.escape(name) + r'\s*\(', src):
            i = src.index("(", m.start())
            depth, j = 0, i
            while j < len(src):
                if src[j] in "([{": depth += 1
                elif src[j] in ")]}":
                    depth -= 1
                    if depth == 0: break
                j += 1
            args = split_params(src[i+1:j])
            if not args: continue
            line = src[:m.start()].count("\n") + 1
            calls_checked += 1
            ok_any = False
            why = []
            for params in overloads:
                bad = False
                if len(args) > len(params): bad = True
                for k, a in enumerate(args):
                    if bad: break
                    if "=" in a and not a.strip().startswith('"'):
                        pn = a.split("=")[0].strip()
                        val = a.split("=", 1)[1].strip()
                        match = [p for p in params if p[0] == pn]
                        if not match:
                            bad = True; why.append(f"no parameter named {pn}"); break
                        pt = match[0][1]
                    else:
                        if k >= len(params): bad = True; break
                        pn, pt = params[k]
                        val = a.strip()
                    base = pt.rstrip("?")
                    # a literal of an obviously different scalar type
                    for tname, rx in SCALAR.items():
                        if rx.match(val) and base not in (tname, "Any", "Number", "Comparable<*>") and base in ("Color",) + tuple(members):
                            bad = True; why.append(f"arg {k+1} ({pn}: {pt}) got a raw {tname} literal {val}")
                            break
                    if bad: break
                    # a `.member` expression whose owner type is known and whose
                    # member type is not the parameter type
                    mm = re.match(r'^([A-Za-z_][A-Za-z0-9_]*)\.([A-Za-z_][A-Za-z0-9_]*)$', val)
                    if mm and base in members and mm.group(2) in members.get(base, set()):
                        bad = True
                        why.append(f"arg {k+1} ({pn}: {pt}) got {val}, which looks like a member OF {base}, not a {base}")
                if not bad: ok_any = True; break
            if not ok_any:
                fails.append(f"{f.relative_to(ROOT)}:{line}: {name}(...) matches no overload -- " + "; ".join(dict.fromkeys(why)))

# ---- 4. destructuring / property access on List<Pair<...>> results
for f in sorted(TEST.rglob("*.kt")):
    src = f.read_text(encoding="utf-8", errors="replace")
    for m in re.finditer(r'\.([A-Za-z_][A-Za-z0-9_]*)\(\)\s*\.(?:filter|map|forEach)\s*\{([^}]*)\}', src):
        fname, body = m.group(1), m.group(2)
        rets = [p for n, ov in [(fname, sigs.get(fname, []))] for p in ov]
        # find the declared return type of that function in main
        rt = None
        for src2 in (p.read_text(encoding="utf-8", errors="replace") for p in MAIN.rglob("*.kt")):
            mr = re.search(r'fun\s+' + re.escape(fname) + r'\s*\([^)]*\)\s*:\s*([A-Za-z0-9_.<>, ?]+)', src2)
            if mr: rt = mr.group(1).strip(); break
        if rt and rt.startswith("List<Pair<") and "it." in body:
            line = src[:m.start()].count("\n") + 1
            prop = re.search(r'it\.([A-Za-z_][A-Za-z0-9_]*)', body)
            if prop and prop.group(1) not in ("first", "second"):
                fails.append(
                    f"{f.relative_to(ROOT)}:{line}: {fname}() returns {rt}; "
                    f"`it.{prop.group(1)}` reads a property off the Pair, not off its element"
                )

print(f"signatures indexed: {len(sigs)}   classes indexed: {len(members)}   test call sites checked: {calls_checked}")
print("FAILURES:", "none" if not fails else "")
for x in fails: print("  -", x)
sys.exit(1 if fails else 0)
