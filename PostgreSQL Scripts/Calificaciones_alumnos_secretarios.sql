CREATE TABLE secretarios 
(
    id_secretario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    clave VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO secretarios (
    nombre,
    apellido,
	usuario,
    clave
) VALUES (
    'Ana',
    'Gómez',
	'agomez',
	'ana.clave'
);

-- Crear rol de secretarios
CREATE ROLE secretarios WITH NOLOGIN;

INSERT INTO secretarios (nombre, apellido, usuario, clave)
VALUES 
('Airi', 'Domínguez', 'adominguez', 'airi.clave'),
('Pedro', 'Martínez', 'pmartinez', 'pedro.clave'),
('Marta', 'Rodríguez', 'mrodriguez', 'marta.clave'),
('Miguel', 'Pérez', 'mperez', 'miguel.clave'),
('Sofía', 'Orozco', 'sorozco', 'sofia.clave');

select * from secretarios;