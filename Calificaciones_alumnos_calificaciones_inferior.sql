
CREATE TABLE Calificaciones_inferior (
    id_calificacion SERIAL PRIMARY KEY,
    Mes VARCHAR(50) NOT NULL,
    Espanol DECIMAL(4, 2) NOT NULL,
    Ingles DECIMAL(4, 2) NOT NULL,
    Artes DECIMAL(4, 2) NOT NULL,
    Matematicas DECIMAL(4, 2) NOT NULL,
    Tecnologia DECIMAL(4, 2) NOT NULL,
    Conocimiento_del_medio DECIMAL(4, 2) NOT NULL,
    Civica_y_etica DECIMAL(4, 2) NOT NULL,
    Educacion_fisica DECIMAL(4, 2) NOT NULL,
    Inasistencias INTEGER NOT NULL,
    Promedio DECIMAL(4, 2) NOT NULL,
    id_alumno INTEGER NOT NULL,
    CONSTRAINT fk_alumno_inferior
        FOREIGN KEY (id_alumno)
        REFERENCES Alumnos (id_alumno)
        ON DELETE CASCADE
);

INSERT INTO Calificaciones_inferior 
(Mes, Espanol, Ingles, Artes, Matematicas, Tecnologia, Conocimiento_del_medio, Civica_y_etica, Educacion_fisica, Inasistencias, Promedio, id_alumno)
VALUES
-- Calificaciones para Miguel Torres (ID: 4)
('Diagnostico', 8.0, 7.5, 8.5, 7.8, 8.2, 7.5, 8.0, 9.0, 0, 8.06, 4),
('Septiembre', 8.2, 7.8, 8.5, 8.0, 8.5, 7.8, 8.2, 9.0, 1, 8.25, 4),
('Octubre', 8.5, 8.0, 8.8, 8.2, 8.7, 8.0, 8.5, 9.2, 0, 8.49, 4),
('Nov/Dic', 8.8, 8.2, 9.0, 8.5, 9.0, 8.5, 8.8, 9.5, 2, 8.79, 4),
('Enero', 9.0, 8.5, 9.2, 8.8, 9.1, 8.7, 9.0, 9.5, 0, 8.98, 4),
('Febrero', 8.7, 8.3, 9.0, 8.5, 9.0, 8.5, 8.8, 9.6, 1, 8.80, 4),
('Marzo', 9.2, 8.8, 9.3, 9.0, 9.2, 9.0, 9.1, 9.7, 0, 9.16, 4),
('Abril', 9.0, 8.5, 9.2, 8.8, 9.1, 8.7, 9.0, 9.5, 0, 8.98, 4),
('Mayo', 9.3, 9.0, 9.5, 9.2, 9.4, 9.1, 9.3, 9.8, 1, 9.33, 4),
('Junio', 9.5, 9.2, 9.6, 9.4, 9.5, 9.3, 9.4, 10.0, 0, 9.49, 4),

-- Calificaciones para Pablo Torreblanca (ID: 5)
('Diagnostico', 8.5, 9.0, 8.8, 8.2, 8.5, 8.8, 9.0, 9.5, 0, 8.79, 5),
('Septiembre', 8.8, 9.2, 9.0, 8.5, 8.8, 9.0, 9.2, 9.6, 0, 9.01, 5),
('Octubre', 9.0, 9.4, 9.2, 8.8, 9.0, 9.2, 9.4, 9.7, 1, 9.21, 5),
('Nov/Dic', 8.7, 9.0, 8.9, 8.5, 8.8, 8.9, 9.1, 9.5, 1, 8.93, 5),
('Enero', 9.2, 9.5, 9.3, 9.0, 9.2, 9.3, 9.5, 9.8, 0, 9.36, 5),
('Febrero', 9.0, 9.3, 9.1, 8.8, 9.0, 9.1, 9.3, 9.6, 0, 9.15, 5),
('Marzo', 9.4, 9.7, 9.5, 9.2, 9.4, 9.5, 9.6, 9.9, 0, 9.53, 5),
('Abril', 9.3, 9.6, 9.4, 9.1, 9.3, 9.4, 9.5, 9.8, 1, 9.43, 5),
('Mayo', 9.6, 9.8, 9.7, 9.4, 9.6, 9.7, 9.7, 10.0, 0, 9.69, 5),
('Junio', 9.8, 10.0, 9.8, 9.6, 9.8, 9.8, 9.9, 10.0, 0, 9.84, 5),

-- Calificaciones para José Morales (ID: 6)
('Diagnostico', 6.5, 7.0, 7.5, 6.8, 7.2, 6.5, 7.0, 8.0, 2, 7.06, 6),
('Septiembre', 6.8, 7.2, 7.8, 7.0, 7.5, 6.8, 7.2, 8.2, 3, 7.31, 6),
('Octubre', 7.0, 7.5, 8.0, 7.2, 7.8, 7.0, 7.5, 8.5, 1, 7.56, 6),
('Nov/Dic', 7.2, 7.8, 8.2, 7.5, 8.0, 7.2, 7.8, 8.8, 2, 7.81, 6),
('Enero', 7.5, 8.0, 8.5, 7.8, 8.2, 7.5, 8.0, 9.0, 1, 8.06, 6),
('Febrero', 7.8, 8.2, 8.6, 8.0, 8.4, 7.8, 8.2, 9.2, 0, 8.28, 6),
('Marzo', 8.0, 8.5, 8.8, 8.2, 8.6, 8.0, 8.5, 9.4, 1, 8.50, 6),
('Abril', 7.9, 8.4, 8.7, 8.1, 8.5, 7.9, 8.4, 9.3, 0, 8.40, 6),
('Mayo', 8.2, 8.7, 9.0, 8.5, 8.8, 8.2, 8.7, 9.6, 0, 8.71, 6),
('Junio', 8.5, 9.0, 9.2, 8.8, 9.0, 8.5, 9.0, 9.8, 0, 8.98, 6),

-- Calificaciones para Sofía García López (ID: 26)
('Diagnostico', 9.2, 9.0, 8.5, 9.5, 9.0, 9.2, 9.0, 9.6, 0, 9.13, 26),
('Septiembre', 9.4, 9.2, 8.8, 9.6, 9.2, 9.4, 9.2, 9.7, 1, 9.31, 26),
('Octubre', 9.6, 9.4, 9.0, 9.8, 9.4, 9.5, 9.4, 9.8, 0, 9.49, 26),
('Nov/Dic', 9.5, 9.3, 8.9, 9.7, 9.3, 9.4, 9.3, 9.7, 0, 9.39, 26),
('Enero', 9.8, 9.6, 9.2, 10.0, 9.6, 9.7, 9.6, 9.9, 0, 9.68, 26),
('Febrero', 9.7, 9.5, 9.1, 9.9, 9.5, 9.6, 9.5, 9.8, 0, 9.58, 26),
('Marzo', 9.9, 9.8, 9.4, 10.0, 9.8, 9.9, 9.8, 10.0, 0, 9.83, 26),
('Abril', 9.8, 9.7, 9.3, 10.0, 9.7, 9.8, 9.7, 9.9, 1, 9.74, 26),
('Mayo', 10.0, 9.9, 9.5, 10.0, 9.9, 10.0, 9.9, 10.0, 0, 9.90, 26),
('Junio', 10.0, 10.0, 9.8, 10.0, 10.0, 10.0, 10.0, 10.0, 0, 9.98, 26);