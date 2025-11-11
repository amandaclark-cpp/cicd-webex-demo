#!/usr/bin/env bash
set -euo pipefail

echo "Building the sample project..."
python3 sample_code.py

echo "Checking for PEP8 compliance..."
pip3 install --quiet pycodestyle
pycodestyle sample_code.py --max-line-length=100
