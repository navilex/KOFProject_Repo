

CREATE TABLE Grupos (
    id_grupo SERIAL PRIMARY KEY,
    Grado INTEGER NOT NULL,
    Grupo VARCHAR(50) UNIQUE NOT NULL,
    Cantidad_alumnos INTEGER NOT NULL,
    Aula VARCHAR(50) NOT NULL,
    Nombre_maestro VARCHAR(100) NOT NULL,
    Apellido_maestro VARCHAR(100) NOT NULL,
    Domicilio_maestro VARCHAR(255) NOT NULL,
    Correo_maestro VARCHAR(255) UNIQUE NOT NULL,
    Telefono_maestro VARCHAR(20) UNIQUE NOT NULL
);

INSERT INTO Grupos (Grado, Grupo, Cantidad_alumnos, Aula, Nombre_maestro, Apellido_maestro, Domicilio_maestro, Correo_maestro, Telefono_maestro) VALUES
(1, '1A', 0, 'Aula 101', 'Laura', 'Sánchez', 'Av. del Sol 1, Ciudad', 'laura.sanchez@gmail.com', '525511112222'),
(1, '1B', 0, 'Aula 102', 'Ricardo', 'Martínez', 'Calle de la Luna 2, Ciudad', 'ricardo.martinez@gmail.com', '525533334444'),
(2, '2A', 0, 'Aula 201', 'Sofía', 'López', 'Blvd. de las Estrellas 3, Ciudad', 'sofia.lopez@gmail.com', '525555556666'),
(2, '2B', 0, 'Aula 202', 'Javier', 'García', 'Paseo de las Nubes 4, Ciudad', 'javier.garcia@gmail.com', '525577778888'),
(3, '3A', 0, 'Aula 301', 'Paola', 'Hernández', 'Camino del Bosque 5, Ciudad', 'paola.hernandez@gmail.com', '525599990000'),
(3, '3B', 0, 'Aula 302', 'Diego', 'Ramírez', 'Río de las Flores 6, Ciudad', 'diego.ramirez@gmail.com', '525512345678'),
(4, '4A', 0, 'Aula 401', 'Ana', 'Pérez', 'Montaña del Viento 7, Ciudad', 'ana.perez@gmail.com', '525587654321'),
(4, '4B', 0, 'Aula 402', 'Carlos', 'Torres', 'Laguna del Cielo 8, Ciudad', 'carlos.torres@gmail.com', '525524681357'),
(5, '5A', 0, 'Aula 501', 'María', 'Díaz', 'Valle de la Niebla 9, Ciudad', 'maria.diaz@gmail.com', '525598765432'),
(5, '5B', 0, 'Aula 502', 'David', 'Soto', 'Playa de la Brisa 10, Ciudad', 'david.soto@gmail.com', '525513572468'),
(6, '6A', 0, 'Aula 601', 'Sandra', 'Rojas', 'Cerro del Sol 11, Ciudad', 'sandra.rojas@gmail.com', '525508642079'),
(6, '6B', 0, 'Aula 602', 'Fernando', 'Castro', 'Avenida del Lago 12, Ciudad', 'fernando.castro@gmail.com', '525597531086');

select * from grupos;