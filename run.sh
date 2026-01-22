#!/bin/bash

set -e

# Check if infrastructure network exists
if ! docker network inspect openmarket-network >/dev/null 2>&1; then
    echo "Infrastructure network not found. Starting infrastructure first..."
    ./run-infra.sh
    echo ""
    echo "Waiting 30 seconds for infrastructure to initialize..."
    sleep 30
fi

echo "Stopping application containers..."
docker rm -f openmarket-app openmarket-heartbeat 2>/dev/null || true

echo "Building and starting application..."
docker compose up --build

echo "Done!"
