#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

COVERALLS_TOKEN=""
JSON_FILE="target/coverage/coveralls.json"
API_URL="https://coveralls.io/api/v1/jobs"
PULL_REQUEST=""
BRANCH=""
DO_PUSH=""

while [[ $# -gt 1 ]]; do
    if [[ $# -eq 1 ]]; then
        echo "Option missing argument: $1"
        exit 1
    fi

    case "$1" in
        -t|--token)
            COVERALLS_TOKEN="$2"
            ;;
        -f|--file)
            JSON_FILE="$2"
            ;;
        -a|--api)
            API_URL="$2"
            ;;
        -p|--pull)
            PULL_REQUEST="$2"
            ;;
        -b|--branch)
            BRANCH="$2"
            ;;
        *)
            echo "Invalid option: $1"
            exit 1
    esac
    shift 2
done

if ! [ -f "$JSON_FILE" ]; then
    echo "No Coveralls JSON file, exiting."
    ls -l $(basename "$JSON_FILE")
    exit 1
fi

if ! grep -q '"repo_token":' "$JSON_FILE"; then
    BRANCH=${BRANCH:-$(git rev-parse --abbrev-ref HEAD)}
    GIT_FORMAT='{"id":"%H","author_name":"%an","author_email":"%ae","committer_name":"%cn","committer_email":"%ce"}'
    GIT_INFO=$(git --no-pager show -s --format="$GIT_FORMAT" HEAD)
    GIT_MESSAGE=$(git --no-pager show -s --format=%s HEAD)
    echo "Fixing up Coveralls JSON file"
    JSON=$(jq -c \
       --argjson githead "$GIT_INFO" \
       --arg message "$GIT_MESSAGE" \
       --arg branch "$BRANCH" \
       --arg pull "$PULL_REQUEST" \
       --arg token "$COVERALLS_TOKEN" \
       'del(.service_job_id)
        | if $pull != "" then .service_pull_request=$pull else . end
        | .service_name="github-actions"
        | .git.head=$githead
        | .git.head.message=$message
        | .git.branch=$branch
        | .repo_token=$token' \
       "$JSON_FILE")
    echo "$JSON" > "$JSON_FILE"
fi

echo "Pushing to Coveralls"
curl -F "json_file=@$JSON_FILE" "$API_URL"
echo
