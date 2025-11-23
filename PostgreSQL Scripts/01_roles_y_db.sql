-- 1. Crear Roles y Usuarios
DO
$do$
BEGIN
   -- Crear grupo directores
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'directores') THEN
      CREATE ROLE directores;
   END IF;

   -- Crear grupo secretarios
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'secretarios') THEN
      CREATE ROLE secretarios;
   END IF;

   -- Crear usuario director
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'director') THEN
      CREATE USER director WITH PASSWORD 'director.clave' CREATEDB;
   END IF;
END
$do$;

-- 2. Asignaciones
GRANT directores TO director;

-- 3. Crear la Base de Datos (Truco \gexec para ejecución condicional)
SELECT 'CREATE DATABASE "Calificaciones_alumnos" OWNER director ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'Calificaciones_alumnos')\gexec