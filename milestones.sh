#!/usr/bin/env bash
#
# This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
# Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#

set -euo pipefail

REPO_OWNER="epam"
REPO_NAME="sap-commerce-intellij-idea-plugin"
TOKEN="${GITHUB_TOKEN:-}"
OUTPUT_FILE="milestone_report.md"
MAX_RETRIES=5
SLEEP_TIME=2

[[ -z "$TOKEN" ]] && { echo "❌ Set GITHUB_TOKEN environment variable"; exit 1; }

> "$OUTPUT_FILE"

# Fetch milestones
GRAPHQL_QUERY='
{
  repository(owner:"'"$REPO_OWNER"'", name:"'"$REPO_NAME"'") {
    milestones(first:100, orderBy:{field:CREATED_AT, direction:DESC}) {
      nodes {
        id
        number
        title
      }
    }
  }
}'

JSON_PAYLOAD=$(jq -n --arg query "$GRAPHQL_QUERY" '{query: $query}')
RESPONSE=$(curl -s -H "Authorization: bearer $TOKEN" -H "Content-Type: application/json" -X POST -d "$JSON_PAYLOAD" https://api.github.com/graphql)

if ! MILESTONES=$(echo "$RESPONSE" | jq -r '.data.repository.milestones.nodes[] | "\(.id)\t\(.number)\t\(.title)"'); then
  echo "❌ Failed to parse milestones. Response:"
  echo "$RESPONSE"
  exit 1
fi

while IFS=$'\t' read -r MILESTONE_ID MILESTONE_NUMBER MILESTONE_TITLE; do
    [[ -z "$MILESTONE_TITLE" ]] && continue
    echo "🌀 Processing milestone: $MILESTONE_TITLE"

    RETRY=0
    AUTHORS=""

    while [[ $RETRY -lt $MAX_RETRIES ]]; do
      GRAPHQL_PR_QUERY='
      {
        repository(owner:"'"$REPO_OWNER"'", name:"'"$REPO_NAME"'") {
          milestone(number: '"$MILESTONE_NUMBER"') {
            pullRequests(first:100, states:MERGED) {
              nodes {
                author {
                  login
                }
              }
            }
          }
        }
      }'

      JSON_PR_PAYLOAD=$(jq -n --arg query "$GRAPHQL_PR_QUERY" '{query: $query}')
      RESPONSE=$(curl -s -H "Authorization: bearer $TOKEN" -H "Content-Type: application/json" -X POST -d "$JSON_PR_PAYLOAD" https://api.github.com/graphql)

      if AUTHORS=$(echo "$RESPONSE" | jq -r '.data.repository.milestone.pullRequests.nodes[] | .author.login // empty' 2>/tmp/jq_error.log); then
        [[ -n "$AUTHORS" ]] && break
      fi

      ((RETRY++))
      echo "⚠️ No merged PRs found or parsing failed for $MILESTONE_TITLE, retry $RETRY/$MAX_RETRIES..."
      echo "   Possible reason: API returned invalid or unexpected JSON."
      echo "   Response snippet:"
      echo "$RESPONSE" | head -n 20
      sleep $SLEEP_TIME
    done

    [[ -z "$AUTHORS" ]] && { echo "⚠️ No merged PRs after $MAX_RETRIES retries for $MILESTONE_TITLE"; continue; }

    PR_COUNT=0
    AUTHOR_COUNT=0

    echo "$AUTHORS" | sort | uniq -c | while read -r COUNT AUTHOR; do
        [[ -z "$AUTHOR" ]] && continue
        ((PR_COUNT+=COUNT))
        ((AUTHOR_COUNT+=1))

        # Fetch real name
        NAME=$(curl -s -H "Authorization: bearer $TOKEN" "https://api.github.com/users/$AUTHOR" | jq -r '.name // empty')
        [[ -z "$NAME" ]] && NAME="$AUTHOR"

        COUNT_TEXT=$([[ "$COUNT" -eq 1 ]] && echo "1 PR" || echo "${COUNT} PR(s)")
        LINE="- ${COUNT_TEXT} by [$NAME](https://github.com/$REPO_OWNER/$REPO_NAME/pulls?q=milestone%3A$MILESTONE_TITLE+author%3A$AUTHOR+is%3Apr)"
        echo "$LINE" | tee -a "$OUTPUT_FILE"
    done

    echo | tee -a "$OUTPUT_FILE"

done <<< "$MILESTONES"

echo "✅ Markdown report generated at: $OUTPUT_FILE"