#!/bin/bash

set -e

echo "Starting infrastructure services (postgres, keycloak, elasticsearch, kibana, apm, metricbeat)..."
docker compose -f docker-compose.infra.yml up

echo ""
echo "Waiting for services to be healthy..."
echo "  - Postgres:      http://localhost:5432"
echo "  - Keycloak:      http://localhost:8180"
echo "  - Elasticsearch: http://localhost:9200"
echo "  - Kibana:        http://localhost:5601"
echo "  - APM Server:    http://localhost:8200"
echo ""
echo "Infrastructure is starting in background. Use 'docker compose -f docker-compose.infra.yml logs -f' to follow logs."
echo "Once healthy, run './run.sh' to start the application."
