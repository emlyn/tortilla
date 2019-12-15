#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

JSON_FILE="target/coverage/coveralls.json"

while [[ $# -gt 1 ]]; do
    if [[ $# -eq 1 ]]; then
        echo "Option missing argument: $1"
        exit 1
    fi

    case "$1" in
        -j|--json-file)
            JSON_FILE="$2"
            ;;
        *)
            echo "Invalid option: $1"
            exit 1
    esac
    shift 2
done

if ! [ -f "$JSON_FILE" ]; then
    echo "JSON file ($JSON_FILE) does not exist, exiting."
    ls -l $(basename "$JSON_FILE")
    exit 1
fi

SOURCE_FILES=$(jq -r .source_files[].name < "$JSON_FILE")

QUERY='.source_files=(.source_files | map('
COND=if

for SRC_FILE in $SOURCE_FILES; do
    FULL_PATH=$(ls -1 src/*/"$SRC_FILE")
    QUERY="$QUERY $COND (.name==\"$SRC_FILE\") then .name=\"$FULL_PATH\""
    COND=elif
done

QUERY="$QUERY else . end))"

JSON=$(jq -c "$QUERY" < "$JSON_FILE")
echo "$JSON" > $JSON_FILE
