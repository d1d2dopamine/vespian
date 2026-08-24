#!/usr/bin/env python3
"""Checks that stand in for the compiler when the SDK is not available.

This repository is assembled and reviewed in places where `gradle assemble` cannot
run, so a whole class of mistakes -- a call that names an argument the function
does not have, a brace that never closes, a Material import creeping back in --
used to be found only after a full CI round trip. Each of those rounds costs
about as long as a coffee, and they were being spent on typos.

So the cheap half of what a compiler does is done here instead:

  1. structure: every brace, paren and bracket balances, per file
  2. arguments: every call to one of this project's own functions names only
     parameters that function declares, and fills every parameter that has no
     default
  3. duplication: the compat package delegates and never draws
  4. purity: no androidx.compose.material* anywhere, and no rounded shapes
  5. leftovers: no pre-rename names survive

It is deliberately not a parser. It reads Kotlin with the literals and comments
blanked out, which is enough for the questions above and cannot be fooled by a
brace inside a string.
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LIB = os.path.join(ROOT, "trial3lib", "src")
APP = os.path.join(ROOT, "app", "src")
errors = []
notes = []


def fail(where, message):
    errors.append("%s: %s" % (where, message))


# --------------------------------------------------------------------------
# lexing: blank out strings and comments, keep offsets and line breaks
# --------------------------------------------------------------------------
def mask(src):
    out = list(src)
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '"':
            if src.startswith('"""', i):
                j = src.find('"""', i + 3)
                j = n if j == -1 else j + 3
            else:
                j = i + 1
                while j < n and src[j] != '"':
                    if src[j] == "\\":
                        j += 2
                    elif src[j] == "\n":
                        break
                    else:
                        j += 1
                j = min(j + 1, n)
        elif c == "'":
            j = i + 1
            while j < n and src[j] != "'":
                j += 2 if src[j] == "\\" else 1
            j = min(j + 1, n)
        elif src.startswith("//", i):
            j = src.find("\n", i)
            j = n if j == -1 else j
        elif src.startswith("/*", i):
            depth, j = 0, i
            while j < n:
                if src.startswith("/*", j):
                    depth += 1
                    j += 2
                elif src.startswith("*/", j):
                    depth -= 1
                    j += 2
                    if depth == 0:
                        break
                else:
                    j += 1
        else:
            i += 1
            continue
        for k in range(i, min(j, n)):
            if out[k] != "\n":
                out[k] = " "
        i = j
    return "".join(out)


def match_pair(masked, start, opener, closer):
    depth = 0
    for i in range(start, len(masked)):
        if masked[i] == opener:
            depth += 1
        elif masked[i] == closer:
            depth -= 1
            if depth == 0:
                return i + 1
    return -1


def split_top(text):
    """Index ranges of the commas that separate arguments or parameters.

    Returns spans rather than substrings so the caller can look at both the
    masked text (to find `name =`) and the real text (to tell an argument that
    is a string literal apart from no argument at all).

    Kotlin's `->` is flattened first: read literally, its `>` closes a bracket
    that was never opened, and every parameter after the first function-typed
    one disappears.
    """
    flat = text.replace("->", "  ")
    spans, depth, start = [], 0, 0
    for i, c in enumerate(flat):
        if c in "([{<":
            depth += 1
        elif c in ")]}>":
            depth -= 1
        elif c == "," and depth == 0:
            spans.append((start, i))
            start = i + 1
    spans.append((start, len(flat)))
    return spans


def line_of(src, index):
    return src.count("\n", 0, index) + 1


def kt_files(*roots):
    found = []
    for root in roots:
        for base, _, names in os.walk(root):
            for name in sorted(names):
                if name.endswith(".kt"):
                    found.append(os.path.join(base, name))
    return found


FILES = kt_files(LIB, APP)
if not FILES:
    print("no Kotlin sources found under %s" % ROOT)
    sys.exit(1)
SRC = {}
MASK = {}
for path in FILES:
    with open(path, encoding="utf-8") as fh:
        SRC[path] = fh.read()
    MASK[path] = mask(SRC[path])


# --------------------------------------------------------------------------
# 1. structure
# --------------------------------------------------------------------------
for path in FILES:
    masked = MASK[path]
    for opener, closer, what in (("{", "}", "brace"), ("(", ")", "paren"), ("[", "]", "bracket")):
        depth, worst = 0, None
        for i, c in enumerate(masked):
            if c == opener:
                depth += 1
            elif c == closer:
                depth -= 1
                if depth < 0 and worst is None:
                    worst = i
        rel = os.path.relpath(path, ROOT)
        if worst is not None:
            fail(rel, "closing %s with nothing open at line %d" % (what, line_of(SRC[path], worst)))
        elif depth != 0:
            fail(rel, "%d %s(s) never closed" % (depth, what))


# --------------------------------------------------------------------------
# 2. declarations and call sites
# --------------------------------------------------------------------------
DECL = re.compile(
    r"(?m)^[ \t]*(?:public |internal |private |protected )?(?:inline |suspend )*"
    r"fun\s+(?:<[^>\n]*>\s*)?(?:([A-Za-z0-9_.<>]+)\.)?([A-Za-z0-9_]+)\s*\("
)
functions = {}
decl_spans = []
for path in FILES:
    src, masked = SRC[path], MASK[path]
    for m in DECL.finditer(masked):
        name = m.group(2)
        paren = masked.index("(", m.end() - 1)
        close = match_pair(masked, paren, "(", ")")
        if close == -1:
            fail(os.path.relpath(path, ROOT), "unterminated parameter list for %s" % name)
            continue
        params = []
        inner = masked[paren + 1:close - 1]
        for a, b in split_top(inner):
            piece = inner[a:b].strip()
            if not piece:
                continue
            head = piece.split(":", 1)[0]
            head = head.replace("vararg", " ").strip()
            pname = head.split()[-1] if head.split() else ""
            params.append((pname, "=" in piece.split(":", 1)[-1]))
        functions.setdefault(name, []).append(
            {"params": params, "file": path, "line": line_of(src, m.start())}
        )
        decl_spans.append((path, m.start(), close))

OWN = {n for n in functions if n.startswith("Trial3") or n.startswith("trial3")}
COMPAT = {
    "Card", "Surface", "HorizontalDivider", "Button", "OutlinedButton", "TextButton",
    "IconButton", "Icon", "TopAppBar", "Scaffold", "NavigationBar", "NavigationBarItem",
    "Switch", "RadioButton", "OutlinedTextField", "CircularProgressIndicator",
    "AlertDialog", "Text",
}
CHECKED = OWN | (COMPAT & set(functions))

for path in FILES:
    src, masked = SRC[path], MASK[path]
    spans = [(s, e) for p, s, e in decl_spans if p == path]
    for name in sorted(CHECKED):
        for m in re.finditer(r"(?<![A-Za-z0-9_.])" + re.escape(name) + r"\s*\(", masked):
            if any(s <= m.start() < e for s, e in spans):
                continue
            paren = masked.index("(", m.end() - 1)
            close = match_pair(masked, paren, "(", ")")
            if close == -1:
                continue
            inner = masked[paren + 1:close - 1]
            real = src[paren + 1:close - 1]
            named, positional = [], 0
            for a, b in split_top(inner):
                if not real[a:b].strip():
                    continue
                hit = re.match(r"\s*([A-Za-z_][A-Za-z0-9_]*)\s*=(?!=)", inner[a:b])
                if hit:
                    named.append(hit.group(1))
                else:
                    positional += 1
            trailing = bool(re.match(r"\s*\{", masked[close:close + 8]))
            where = "%s:%d" % (os.path.relpath(path, ROOT), line_of(src, m.start()))
            best, best_score = None, None
            for variant in functions[name]:
                allowed = [p for p, _ in variant["params"]]
                unknown = [a for a in named if a not in allowed]
                required = [p for p, has_default in variant["params"] if not has_default]
                covered = set(named) | set(allowed[:positional])
                if trailing and allowed:
                    covered.add(allowed[-1])
                missing = [p for p in required if p not in covered]
                score = len(unknown) * 10 + len(missing)
                if best_score is None or score < best_score:
                    best, best_score = (unknown, missing, variant), score
            if best_score:
                unknown, missing, variant = best
                target = "%s (declared %s:%d)" % (
                    name, os.path.relpath(variant["file"], ROOT), variant["line"])
                if unknown:
                    fail(where, "%s does not take argument(s): %s" % (target, ", ".join(unknown)))
                if missing:
                    fail(where, "%s is missing required argument(s): %s" % (target, ", ".join(missing)))


# --------------------------------------------------------------------------
# 3. the compat package delegates, it does not draw
# --------------------------------------------------------------------------
DRAWING = ("Canvas(", ".background(", ".border(", ".drawBehind", "drawRect", "drawLine")
for path in FILES:
    if os.sep + "compat" + os.sep not in path:
        continue
    masked = MASK[path]
    for token in DRAWING:
        for m in re.finditer(re.escape(token), masked):
            fail("%s:%d" % (os.path.relpath(path, ROOT), line_of(SRC[path], m.start())),
                 "compat draws with %s -- geometry belongs in component/ or shell" % token.strip(".("))


# --------------------------------------------------------------------------
# 4. purity, and 5. leftovers
# --------------------------------------------------------------------------
BANNED = {
    "androidx.compose.material": "Material is not a dependency of this design",
    "RoundedCornerShape": "the design has no rounded corners",
    "CircleShape": "the design has no circles",
}
STALE = {
    "dev.lattice": "pre-rename package",
    ":lattice": "pre-rename module",
}
for path in FILES:
    masked, src = MASK[path], SRC[path]
    rel = os.path.relpath(path, ROOT)
    for needle, why in BANNED.items():
        for m in re.finditer(re.escape(needle), masked):
            fail("%s:%d" % (rel, line_of(src, m.start())), "%s (%s)" % (needle, why))
    for needle, why in STALE.items():
        if needle in src:
            fail(rel, "%s survives (%s)" % (needle, why))
    for m in re.finditer(r"\bLat[A-Z][A-Za-z0-9_]*", masked):
        if m.group(0) != "LatticePlaceholder":
            fail("%s:%d" % (rel, line_of(src, m.start())), "pre-rename name %s" % m.group(0))


# --------------------------------------------------------------------------
# migration meter: how much of the app still speaks Material
# --------------------------------------------------------------------------
compat_imports = 0
for path in FILES:
    if path.startswith(APP):
        compat_imports += len(re.findall(r"import dev\.trial3lib\.ui\.compat\.", SRC[path]))
notes.append("app still imports %d compat name(s); each one is a screen not yet "
             "speaking the library's own API" % compat_imports)
notes.append("checked %d Kotlin files, %d declared functions" % (len(FILES), len(functions)))

for note in notes:
    print("note: %s" % note)
if errors:
    print("\n%d problem(s):\n" % len(errors))
    for e in errors:
        print("  %s" % e)
    sys.exit(1)
print("\nall structural checks passed")
