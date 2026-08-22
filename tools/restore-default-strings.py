#!/usr/bin/env python3
"""Rebuild app/src/main/res/values/strings.xml for Vespian.

The default resource file in the repository contains one string -- Ikna's
app_name -- while values-ru/strings.xml has all 478 of Vespian's. The code
references 465 distinct string resources, so the app cannot compile, and if it
could it would be called "Ikna" in the launcher.

Android resolves resources by falling back from values-ru to values, never the
other way round, so the *default* file is the one that has to be complete. This
script copies every name from values-ru into values. The text stays Russian --
wrong language, right resource ids, compiles today. Translate at leisure; the
Russian file keeps overriding this one on a Russian device either way.

Usage:  python3 restore-default-strings.py /path/to/vespian-main
"""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
src = root / "app/src/main/res/values-ru/strings.xml"
dst = root / "app/src/main/res/values/strings.xml"
if not src.exists():
    sys.exit("not found: %s" % src)

ru = ET.parse(src).getroot()
out = [
    '<?xml version="1.0" encoding="utf-8"?>',
    "<!--",
    "  Default (fallback) strings, generated from values-ru by",
    "  tools/restore-default-strings.py. Every id the code uses lives here, so",
    "  the app compiles and runs on any locale. The wording is still Russian:",
    "  replace it with English as you go, one string at a time.",
    "-->",
    "<resources>",
]
for child in ru:
    if child.tag != "string":
        continue
    name = child.get("name")
    text = child.text or ""
    fmt = child.get("formatted")
    # An apostrophe or an unescaped @/? at the start breaks aapt2.
    text = (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "\\'")
    )
    if name == "app_name":
        text = "Vespian"
    attrs = ' name="%s"' % name
    if fmt is not None:
        attrs += ' formatted="%s"' % fmt
    out.append("    <string%s>%s</string>" % (attrs, text))
out.append("</resources>")
dst.write_text("\n".join(out) + "\n", encoding="utf-8")
print("wrote %s (%d strings)" % (dst, sum(1 for c in ru if c.tag == "string")))
