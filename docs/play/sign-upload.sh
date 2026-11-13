#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 4 || ! -f "$1" || ! -f "$2" || -e "$4" ]]; then
    echo 'Usage: bash sign-upload.sh unsigned.aab upload.keystore alias new-signed.aab (output must not exist)' >&2
    exit 2
fi
# jarsigner prompts locally for the password. No password is passed on the command line.
jarsigner -keystore "$2" -signedjar "$4" "$1" "$3"
jarsigner -verify "$4"
keytool -printcert -jarfile "$4"
