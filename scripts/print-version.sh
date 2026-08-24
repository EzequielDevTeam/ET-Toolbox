#!/usr/bin/env bash
# Imprime versionCode/versionName atuais do módulo app.
set -euo pipefail
grep -E 'version(Code|Name)' "$(dirname "$0")/../app/build.gradle.kts"
