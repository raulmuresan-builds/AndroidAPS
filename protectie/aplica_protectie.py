#!/usr/bin/env python3
"""
Applies the phone-bound activation on top of the official AAPS source, at build time.
Run from the AAPS source root (official tag already checked out):
    ACTIVATION_SECRET=... python3 aplica_protectie.py <protectie folder>
Stops with an error if a hook point is not found exactly once
(AAPS changed its code) - it never produces an unprotected or broken APK silently.
"""
import os
import re
import shutil
import sys

src = sys.argv[1]
secret = os.environ.get("ACTIVATION_SECRET", "").strip()
if not re.fullmatch(r"[0-9a-f]{32}", secret):
    sys.exit("EROARE: secretul AAPS_ACTIVATION_SECRET lipseste sau nu are formatul asteptat (32 caractere hex).")

dest = "app/src/main/kotlin/app/aaps/activation"
os.makedirs(dest, exist_ok=True)
for name in ("ActivationGate.kt", "ActivationActivity.kt"):
    shutil.copy(os.path.join(src, name), os.path.join(dest, name))
with open(os.path.join(dest, "ActivationSecret.kt"), "w") as f:
    f.write("package app.aaps.activation\n\ninternal object ActivationSecret {\n"
            f"    const val VALUE = \"{secret}\"\n}}\n")
print("Fisiere de activare copiate.")


def patch(path, anchor, insert, after=True):
    with open(path) as f:
        text = f.read()
    count = text.count(anchor)
    if count != 1:
        sys.exit(f"EROARE: punctul de legatura apare de {count} ori in {path} (asteptat: 1).\n{anchor}")
    new = text.replace(anchor, anchor + insert if after else insert + anchor)
    with open(path, "w") as f:
        f.write(new)
    print(f"Modificat: {path}")


patch(
    "app/src/main/AndroidManifest.xml",
    "\n    </application>",
    "\n        <!-- Activare raulmuresan-builds -->\n"
    "        <activity\n"
    "            android:name=\"app.aaps.activation.ActivationActivity\"\n"
    "            android:exported=\"false\"\n"
    "            android:theme=\"@android:style/Theme.Material.Light.NoActionBar\"\n"
    "            android:windowSoftInputMode=\"adjustResize\" />\n",
    after=False,
)

patch(
    "app/src/main/kotlin/app/aaps/ComposeMainActivity.kt",
    "override fun onCreate(savedInstanceState: Bundle?) {",
    "\n        if (!app.aaps.activation.ActivationGate.isActivated(this)) {"
    "\n            // Activare raulmuresan-builds: telefon neactivat -> ecranul de activare."
    "\n            super.onCreate(savedInstanceState)"
    "\n            app.aaps.activation.ActivationGate.open(this)"
    "\n            finish()"
    "\n            return"
    "\n        }",
)
print("Protectie aplicata.")
