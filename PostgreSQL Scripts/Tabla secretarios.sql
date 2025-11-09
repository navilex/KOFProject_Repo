CREATE TABLE secretarios 
(
    id_secretario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
	correo VARCHAR(255) UNIQUE NOT NULL,
	telefono VARCHAR(20) UNIQUE NOT NULL,
	domicilio VARCHAR(255) NOT NULL,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    clave VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO secretarios (
    nombre,
    apellido,
	correo,
    telefono,
    domicilio,
	usuario,
    clave
) VALUES (
    'Ana',
    'Gómez',
	'ana.gomez@ejemplo.com',
	'525512345678',
    'Avenida Siempre Viva 742, Ciudad de México',
	'agomez',
	'contraseña_segura_hashed' -- Se recomienda usar un hash
);