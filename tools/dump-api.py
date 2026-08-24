#!/usr/bin/env python3
# The library's public surface, written down.
#
# A design system is a promise: every name in here is something an app can call
# and will keep calling. Renaming one is not a refactor, it is a broken build in
# somebody else's repository. So the surface is dumped to a file that is
# reviewed like any other change -- if a commit widens or narrows it, the diff
# says so in one place instead of being spread across twenty files.
#
#   python3 tools/dump-api.py            rewrite the dump
#   python3 tools/dump-api.py --check    fail if the dump is out of date (CI)
import pathlib, re, sys

REPO = pathlib.Path(__file__).resolve().parent.parent
SRC = REPO / "trial3lib" / "src" / "main" / "kotlin"
OUT = REPO / "trial3lib" / "api" / "trial3lib.api"
if not SRC.is_dir():
    sys.exit("cannot find %s" % SRC)

PAIRS = {"(": ")", "[": "]", "{": "}"}


def mask(src):
    """Blank out comments and string bodies, keeping every offset in place."""
    out, i, n = list(src), 0, len(src)
    while i < n:
        c = src[i]
        if c == "/" and i + 1 < n and src[i + 1] == "/":
            while i < n and src[i] != "\n":
                out[i] = " "
                i += 1
        elif c == "/" and i + 1 < n and src[i + 1] == "*":
            while i < n and not (src[i] == "*" and i + 1 < n and src[i + 1] == "/"):
                out[i] = " " if src[i] != "\n" else "\n"
                i += 1
            for j in range(i, min(i + 2, n)):
                out[j] = " "
            i += 2
        elif c == '"':
            triple = src.startswith('"""', i)
            end = '"""' if triple else '"'
            i += len(end)
            while i < n and not src.startswith(end, i):
                if not triple and src[i] == "\\":
                    out[i] = " "
                    i += 1
                if i < n:
                    out[i] = " " if src[i] != "\n" else "\n"
                    i += 1
            i += len(end)
        else:
            i += 1
    return "".join(out)


def match_pair(masked, i):
    """Index just past the bracket that closes the one at [i]."""
    want, depth = PAIRS[masked[i]], 0
    for j in range(i, len(masked)):
        if masked[j] in PAIRS:
            depth += 1
        elif masked[j] in PAIRS.values():
            depth -= 1
            if depth == 0:
                return j + 1
    return -1


def one_line(text):
    return re.sub(r"\s+", " ", text).strip().rstrip("{").strip()


entries = set()
for path in sorted(SRC.rglob("*.kt")):
    src = path.read_text(encoding="utf-8")
    masked = mask(src)
    owner = ""
    for m in re.finditer(r"^(?P<indent>[ \t]*)public\s+", masked, re.M):
        start = m.start()
        indent = len(m.group("indent"))
        head = masked[m.end():m.end() + 400]
        # a parameter list, if this declaration has one
        rel = head.find("(")
        line_end = masked.find("\n", m.end())
        if rel != -1 and (line_end == -1 or m.end() + rel < line_end):
            close = match_pair(masked, m.end() + rel)
            if close == -1:
                continue
            cut = len(masked)
            for ch in "{=\n":
                at = masked.find(ch, close)
                if at != -1:
                    cut = min(cut, at)
            text = one_line(src[start:cut])
        else:
            cut = len(masked)
            for ch in "={\n":
                at = masked.find(ch, m.end())
                if at != -1:
                    cut = min(cut, at)
            text = one_line(src[start:cut])
        if not text:
            continue
        if indent == 0:
            kind = re.match(r"public\s+(?:\w+\s+)*?(class|object|interface|enum|annotation)\b", text)
            owner = ""
            if kind:
                name = re.search(r"\b(?:class|object|interface)\s+([A-Za-z_]\w*)", text)
                owner = name.group(1) if name else ""
            entries.add(text)
        elif owner:
            entries.add("%s.%s" % (owner, text))
lines = sorted(entries)
body = "\n".join(lines) + "\n"

if "--check" in sys.argv:
    have = OUT.read_text(encoding="utf-8") if OUT.exists() else ""
    if have != body:
        old, new = set(have.splitlines()), set(lines)
        for gone in sorted(old - new):
            print("  removed: %s" % gone)
        for added in sorted(new - old):
            print("  added:   %s" % added)
        sys.exit(
            "the public surface changed but %s was not updated.\n"
            "Run: python3 tools/dump-api.py" % OUT.relative_to(REPO)
        )
    print("public surface unchanged: %d declarations" % len(lines))
else:
    OUT.write_text(body, encoding="utf-8")
    print("wrote %s: %d declarations" % (OUT.relative_to(REPO), len(lines)))
