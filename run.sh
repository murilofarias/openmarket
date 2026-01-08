#!/bin/bash

set -e

echo "🧹 Cleaning up old containers and images..."
docker compose down --volumes --remove-orphans

echo "🐳 Building and starting Docker containers..."
docker compose up --build

echo "✅ Done!"
