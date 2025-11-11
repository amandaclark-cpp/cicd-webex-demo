#!/usr/bin/env bash
set -euo pipefail

echo "Running tests for the sample project..."
pip3 install --quiet pytest
pytest -q

