CREATE TABLE alumnos (
    id_alumno SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    edad INTEGER NOT NULL,
    genero VARCHAR(50) NOT NULL,
    domicilio VARCHAR(255) NOT NULL,
    nombre_padre VARCHAR(100) NOT NULL,
    apellido_padre VARCHAR(100) NOT NULL,
    correo_padre VARCHAR(255) NOT NULL UNIQUE,
    telefono_padre VARCHAR(20) NOT NULL UNIQUE,
    grupo VARCHAR(2) NOT NULL,
    grado INTEGER NOT NULL
);

INSERT INTO alumnos (
    nombre,
    apellido,
    edad,
    genero,
    domicilio,
    nombre_padre,
    apellido_padre,
    correo_padre,
    telefono_padre,
    grupo
) VALUES (
    'Sofía',
    'Ramírez',
    9,
    'Femenino',
    'Calle de la Paz 25, Guadalajara',
    'Juan',
    'Ramírez',
    'juan.ramirez@ejemplo.com',
    '523398765432',
    'A'
	3
);