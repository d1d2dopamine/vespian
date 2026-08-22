import pathlib, re, sys

root = pathlib.Path("/data/out/vespian-lattice")
lat = root / "lattice/src/main/kotlin/dev/lattice"
app = root / "app/src/main/java/dev/vespian"
fails = []
OPEN, CLOSE = "([{", ")]}"

files = {p: p.read_text(encoding="utf-8") for p in list(lat.rglob("*.kt")) + list(app.rglob("*.kt"))}

def imports(src):
    return set(re.findall(r"^import ([\w.]+)", src, re.M))

# ---- A. properties assigned inside a semantics { } block are extensions and
#         need their own import, one per name.
SEM_PROPS = {"role", "contentDescription", "stateDescription", "selected",
             "heading", "disabled", "liveRegion", "progressBarRangeInfo",
             "toggleableState", "password", "error", "testTag"}
for path, src in files.items():
    imps = imports(src)
    for m in re.finditer(r"semantics(?:\([^)]*\))?\s*\{", src):
        i, depth = m.end(), 1
        while i < len(src) and depth:
            c = src[i]
            if c in OPEN:
                depth += 1
            elif c in CLOSE:
                depth -= 1
            i += 1
        body = src[m.end():i]
        for prop in re.findall(r"(\w+)\s*=(?!=)", body):
            if prop in SEM_PROPS and ("androidx.compose.ui.semantics." + prop) not in imps:
                fails.append("%s: semantics %s not imported" % (path.name, prop))
                print("MISSING IMPORT", path.name, "androidx.compose.ui.semantics." + prop)
print("semantics blocks checked")

# ---- B. nullable argument into a non-null parameter, within this codebase.
def balanced(src, start):
    depth, i, out = 1, start, []
    while i < len(src):
        c = src[i]
        if c in OPEN:
            depth += 1
        elif c in CLOSE:
            depth -= 1
            if depth == 0:
                break
        out.append(c)
        i += 1
    return "".join(out)

def split_top(body):
    depth, cur, out = 0, "", []
    for c in body:
        if c in OPEN:
            depth += 1
        elif c in CLOSE:
            depth -= 1
        if c == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += c
    out.append(cur)
    return out

def param_types(body):
    types = {}
    for seg in split_top(body):
        m = re.match(r"\s*(?:@\w+\s*)*(\w+)\s*:\s*(.+?)(?:\s*=.*)?$", seg.strip(), re.S)
        if m:
            types[m.group(1)] = m.group(2).strip()
    return types

# signatures of every function declared in the library
sigs = {}
for path, src in files.items():
    for m in re.finditer(r"public fun (?:\w+\.)?(\w+)\(", src):
        sigs.setdefault(m.group(1), []).append(param_types(balanced(src, m.end())))

checked = 0
for path, src in files.items():
    for fm in re.finditer(r"public fun (?:\w+\.)?(\w+)\(", src):
        outer = param_types(balanced(src, fm.end()))
        nullable = {k for k, v in outer.items() if v.rstrip().endswith("?")}
        if not nullable:
            continue
        # body: from the end of the parameter list to the next top-level declaration
        start = fm.end() + len(balanced(src, fm.end()))
        nxt = src.find("\npublic ", start)
        body = src[start:nxt if nxt > 0 else len(src)]
        for cm in re.finditer(r"(?<![\w.])([A-Z]\w+)\(", body):
            name = cm.group(1)
            if name not in sigs:
                continue
            args = split_top(balanced(body, cm.end()))
            for a in args:
                am = re.match(r"\s*(\w+)\s*=\s*([\w.]+)\s*$", a)
                if not am:
                    continue
                pname, aval = am.group(1), am.group(2)
                if aval not in nullable:
                    continue
                checked += 1
                for cand in sigs[name]:
                    t = cand.get(pname)
                    if t and not t.rstrip().endswith("?") and "=" not in t:
                        fails.append("%s: %s(%s = %s) nullable into %s" % (path.name, name, pname, aval, t))
                        print("NULLABLE INTO NON-NULL", path.name, name, pname, "->", t)
print("nullable passthroughs checked:", checked)

# ---- C. every capitalised symbol used in a library file must be imported,
#         declared in the file, or declared in the same package.
STD = set("""Int Long Float Double Boolean String Char Unit Any Nothing List Set Map MutableList
MutableSet MutableMap Array IntArray FloatArray Pair Triple Comparable Iterable Sequence Result
Exception IllegalArgumentException IllegalStateException Math Regex StringBuilder Number Byte Short
CharSequence Throwable Function0 Function1 Enum T R E K V Suppress JvmStatic JvmInline JvmName
Composable ReadOnlyComposable Stable Immutable Preview RequiresOptIn Deprecated OptIn Target
Retention MustBeDocumented Volatile Synchronized Nullable NonNull""".split())

decl_by_pkg = {}
for path, src in files.items():
    pkg = re.search(r"package ([\w.]+)", src).group(1)
    names = set(re.findall(r"(?:^|\s)(?:public |private |internal )?(?:data |enum |annotation |sealed |abstract )?(?:class|object|interface)\s+(\w+)", src))
    names |= set(re.findall(r"(?:^|\s)(?:public |private |internal )?(?:val|var)\s+([A-Z]\w+)", src))
    names |= set(re.findall(r"(?:^|\s)(?:public |private |internal )?fun\s+(?:<[^>]*>\s*)?(?:\w+\.)?([A-Z]\w+)", src))
    decl_by_pkg.setdefault(pkg, set()).update(names)

unknown = {}
for path, src in files.items():
    pkg = re.search(r"package ([\w.]+)", src).group(1)
    imps = imports(src)
    imported = {i.rsplit(".", 1)[-1] for i in imps}
    wild = {i.rsplit(".", 1)[0] for i in imps if i.endswith(".*")}
    local = decl_by_pkg.get(pkg, set())
    body = re.sub(r"^(?:package|import) .*$", "", src, flags=re.M)
    body = re.sub(r'"(?:[^"\\]|\\.)*"', '""', body)
    body = re.sub(r"//.*|/\*.*?\*/", "", body, flags=re.S)
    for m in re.finditer(r"(?<![\w.\"])([A-Z]\w+)", body):
        n = m.group(1)
        if n in STD or n in imported or n in local or wild:
            continue
        unknown.setdefault(path.name, set()).add(n)
print()
print("capitalised symbols with no import and no local declaration:")
for f, names in sorted(unknown.items()):
    print(" ", f, sorted(names))

print()
print("FAILURES:", fails if fails else "none")
sys.exit(1 if fails else 0)
