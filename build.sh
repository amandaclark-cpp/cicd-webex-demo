#!/usr/bin/env bash
set -euo pipefail

echo "Building the sample project..."
python sample_code.py

echo "Checking for PEP8 compliance..."
pycodestyle sample_code.py --max-line-length=100
