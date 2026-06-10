#!/usr/bin/env bash
set -euo pipefail

# Rebases workshop/base onto current main and updates all tags + pushes to GitHub.
# Run this after committing changes on main that should be included in the workshop.

echo "=== Rebasing workshop/base onto main ==="
git checkout workshop/base
git rebase main

echo ""
echo "=== Updating tags ==="
HASH_START=$(git log --oneline workshop/base | grep "workshop/base: prepare base state" | awk '{print $1}')
HASH_E2=$(git log --oneline workshop/base | grep "enablement: activate all skills" | awk '{print $1}')
HASH_E3=$(git log --oneline workshop/base | grep "enablement: activate all quality gates" | awk '{print $1}')

git tag -f workshop/start "$HASH_START"
git tag -f workshop/enablement-2 "$HASH_E2"
git tag -f workshop/enablement-3 "$HASH_E3"

echo ""
echo "  workshop/start        -> $HASH_START"
echo "  workshop/enablement-2 -> $HASH_E2"
echo "  workshop/enablement-3 -> $HASH_E3"

echo ""
echo "=== Pushing to GitHub ==="
git push origin main
git push origin workshop/base --force-with-lease
git push origin --tags --force

echo ""
echo "=== Switching back to main ==="
git checkout main

echo ""
echo "Done."
