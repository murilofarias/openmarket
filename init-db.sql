-- Create database for OpenMarket API
CREATE DATABASE openmarket;

-- Create database for Keycloak
CREATE DATABASE keycloak;

-- Create user for OpenMarket API
CREATE USER openmarket_user WITH PASSWORD 'secret';
GRANT ALL PRIVILEGES ON DATABASE openmarket TO openmarket_user;

-- Create user for Keycloak
CREATE USER keycloak_user WITH PASSWORD 'keycloak_secret';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;

-- Connect to openmarket database and grant schema privileges
\c openmarket
GRANT ALL ON SCHEMA public TO openmarket_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO openmarket_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO openmarket_user;

-- Connect to keycloak database and grant schema privileges
\c keycloak
GRANT ALL ON SCHEMA public TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak_user;
