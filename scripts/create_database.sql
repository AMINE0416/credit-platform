-- Script de création de la base de données PostgreSQL
-- À exécuter avec psql ou pgAdmin

-- Créer la base de données
CREATE DATABASE credit_platform
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'French_France.1252'
    LC_CTYPE = 'French_France.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Se connecter à la base (dans psql)
\c credit_platform

-- Message de confirmation
SELECT 'Base de données credit_platform créée avec succès !' AS message;
