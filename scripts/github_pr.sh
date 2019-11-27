#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

if [[ $# -ne 2 ]]; then
    echo "usage: $0 $$GITHUB_EVENT_NAME $$GITHUB_REF"
fi


EVENT="$1"
REF="$2"

if [[ "$1" == "pull_request" ]]; then
    PR=$(echo "$REF" | sed 's|refs/pull/\([^/]*\)/merge|\1|')
    if ! echo -n "$PR" | grep '^[0-9]\+$'; then
        echo "REF not in expected format (refs/pull/<PR>/merge): $REF"
        exit 1
    fi
fi
