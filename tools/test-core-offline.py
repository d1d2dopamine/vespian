#!/usr/bin/env python3
"""Run actual Model.kt/Filter.kt and JUnit tests without Android or downloads.

Requires an existing Gradle 8.10.2 distribution (its Kotlin 1.9.24/JUnit jars)
and JDK 17. Set VESPIAN_GRADLE_HOME and VESPIAN_JAVA_HOME explicitly.
Compile-only stubs cover DB DTOs, JSONArray, Calib.Score and Behaviour.MAX_WIDEN;
JSON stubs throw (callers may catch them). No persistence, calibration, behaviour-model or Android
integration is exercised. The production Filter/Physics mathematics is not copied or
reimplemented. The normal Android CI remains authoritative for the full app.
"""
import argparse
import hashlib
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--production-root", type=Path, default=ROOT,
                    help="Read production code here; tests always come from this checkout")
args = parser.parse_args()
production = args.production_root.resolve()
java = Path(os.environ["VESPIAN_JAVA_HOME"]) / "bin/java"
lib = Path(os.environ["VESPIAN_GRADLE_HOME"]) / "lib"
jar_names = ["kotlin-stdlib-1.9.24.jar", "junit-4.13.2.jar", "hamcrest-core-1.3.jar", "annotations-24.0.1.jar"]
jars = [lib / name for name in jar_names]
for path in [java, lib / "kotlin-compiler-embeddable-1.9.24.jar", *jars]:
    if not path.is_file():
        raise SystemExit(f"Missing existing tool: {path}; this runner never downloads dependencies")
main = production / "app/src/main/java/dev/vespian/model"
tests = ROOT / "app/src/test/java/dev/vespian/model"
sources = [main / "Model.kt", main / "Filter.kt", *(tests / f for f in ("PhysicsTest.kt", "BandTest.kt", "FilterTest.kt"))]
for path in sources:
    print(f"SOURCE {path.name} sha256={hashlib.sha256(path.read_bytes()).hexdigest()}", flush=True)
behaviour = (main / "Behaviour.kt").read_text()
match = re.search(r"const val MAX_WIDEN = ([0-9.]+)", behaviour)
if not match:
    raise SystemExit("Cannot resolve Behaviour.MAX_WIDEN from the selected production source")

# Restrict both compiler and tests to a single CPU, at low scheduling priority.
if hasattr(os, "sched_getaffinity"):
    os.sched_setaffinity(0, {max(os.sched_getaffinity(0))})
if hasattr(os, "nice"):
    os.nice(15)
java_cmd = [str(java), "-XX:ActiveProcessorCount=1", "-Xmx384m"]
classpath = os.pathsep.join(map(str, jars))

with tempfile.TemporaryDirectory(prefix="vespian-core-") as directory:
    temp = Path(directory)
    stubs = {
        "DatabaseBoundary.kt": """package dev.vespian.db
data class Night(val sleepStart: Long, val sleepEnd: Long)
data class Answer(val mugs: Int?)
""",
        "ModelBoundary.kt": """package dev.vespian.model
object Calib { class Score }
object Behaviour { const val MAX_WIDEN = """ + match.group(1) + " }\n",
        "JsonBoundary.kt": """package org.json
class JSONArray {
    constructor()
    constructor(text: String) { error("JSON is outside the numerical test boundary") }
    fun length(): Int = error("JSON is outside the numerical test boundary")
    fun getJSONArray(index: Int): JSONArray = error("JSON is outside the numerical test boundary")
    fun getDouble(index: Int): Double = error("JSON is outside the numerical test boundary")
    fun put(value: Any?): JSONArray = error("JSON is outside the numerical test boundary")
}
""",
    }
    for name, text in stubs.items():
        path = temp / name
        path.write_text(text)
        sources.append(path)
    output = temp / "classes"
    print("Compiling actual Filter/Model; explicit compile-only DB, JSON, Calib and Behaviour stubs", flush=True)
    subprocess.run(java_cmd + ["-cp", str(lib / "*"), "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler",
                   "-no-stdlib", "-no-reflect", "-jvm-target", "17", "-classpath", classpath,
                   "-d", str(output), *map(str, sources)], check=True)
    print("Running numerical regression tests only; no Android, JSON, DB or calibration validation", flush=True)
    result = subprocess.run(java_cmd + ["-cp", str(output) + os.pathsep + classpath,
                            "org.junit.runner.JUnitCore", "dev.vespian.model.PhysicsTest",
                            "dev.vespian.model.BandTest", "dev.vespian.model.FilterTest"])
    raise SystemExit(result.returncode)
