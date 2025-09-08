CREATE TABLE alumnos (
    id_alumno SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    edad INTEGER,
    genero VARCHAR(50),
    domicilio VARCHAR(255),
    nombre_padre VARCHAR(100),
    apellido_padre VARCHAR(100),
    correo_padre VARCHAR(255),
    telefono_padre VARCHAR(20),
    grupo VARCHAR(3)
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
    '3A'
);

ALTER TABLE alumnos
ALTER COLUMN nombre SET NOT NULL,
ALTER COLUMN apellido SET NOT NULL,
ALTER COLUMN edad SET NOT NULL,
ALTER COLUMN genero SET NOT NULL,
ALTER COLUMN domicilio SET NOT NULL,
ALTER COLUMN nombre_padre SET NOT NULL,
ALTER COLUMN apellido_padre SET NOT NULL,
ALTER COLUMN correo_padre SET NOT NULL,
ALTER COLUMN telefono_padre SET NOT NULL,
ALTER COLUMN grupo SET NOT NULL;

ALTER TABLE alumnos
ADD CONSTRAINT unique_correo_padre UNIQUE (correo_padre),
ADD CONSTRAINT unique_telefono_padre UNIQUE (telefono_padre);