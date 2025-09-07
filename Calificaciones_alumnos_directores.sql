-- Crear Grupo de directores

CREATE ROLE directores NOLOGIN;

-- Otorgar permisos elevados a nivel base de datos

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA PUBLIC TO directores;

-- Crear 1 usuario

CREATE USER director_ivan WITH LOGIN PASSWORD 'ivan.clave';

-- Asignar el rol de administradores a los usuarios

GRANT directores TO director_ivan;

-- Dar permiso para crear otros usuarios

ALTER ROLE directores CREATEROLE;

-- Asignar directores como dueño de secretarios

GRANT secretarios TO directores WITH ADMIN OPTION;


