#!/bin/sh
# Deletes the demo folder so the presentation can start from a clean state.
# Only touches demo_messy inside this project.
cd "$(dirname "$0")" || exit 1
rm -rf demo_messy
echo "demo_messy removed. Run ./run.sh and choose option 10 to create it again."
