CREATE TABLE alumnos (
    id_alumno SERIAL PRIMARY KEY,
	curp VARCHAR(18) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    edad INTEGER NOT NULL,
    genero VARCHAR(50) NOT NULL,
    nombre_padre VARCHAR(100) NOT NULL,
    apellido_padre VARCHAR(100) NOT NULL,
    correo_padre VARCHAR(255) NOT NULL UNIQUE,
    telefono_padre VARCHAR(20) NOT NULL UNIQUE,
	id_grupo INTEGER NOT NULL
);