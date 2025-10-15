CREATE TABLE Calificaciones_superior (
    id_calificacion SERIAL PRIMARY KEY,
    Mes VARCHAR(50) NOT NULL,
    Espanol DECIMAL(4, 2) NOT NULL,
    Ingles DECIMAL(4, 2) NOT NULL,
    Artes DECIMAL(4, 2) NOT NULL,
    Matematicas DECIMAL(4, 2) NOT NULL,
    Tecnologia DECIMAL(4, 2) NOT NULL,
    Ciencias_naturales DECIMAL(4, 2) NOT NULL,
    Civica_y_etica DECIMAL(4, 2) NOT NULL,
    Educacion_fisica DECIMAL(4, 2) NOT NULL,
    Inasistencias INTEGER NOT NULL,
    Promedio DECIMAL(4, 2) NOT NULL,
    id_alumno INTEGER NOT NULL,
    CONSTRAINT fk_alumno_superior
        FOREIGN KEY (id_alumno)
        REFERENCES Alumnos (id_alumno)
        ON DELETE CASCADE
);

select * from calificaciones_superior;

INSERT INTO Calificaciones_superior 
(Mes, Espanol, Ingles, Artes, Matematicas, Tecnologia, Ciencias_naturales, Civica_y_etica, Educacion_fisica, Inasistencias, Promedio, id_alumno)
VALUES
-- Calificaciones para Pablo Torreblanca (ID: 25)
('Diagnostico', 9.0, 9.2, 9.5, 8.8, 9.0, 9.2, 9.4, 9.8, 0, 9.24, 25),
('Septiembre', 9.2, 9.4, 9.6, 9.0, 9.2, 9.4, 9.5, 9.9, 0, 9.40, 25),
('Octubre', 9.5, 9.6, 9.8, 9.2, 9.4, 9.6, 9.7, 10.0, 0, 9.60, 25),
('Nov/Dic', 9.4, 9.5, 9.7, 9.1, 9.3, 9.5, 9.6, 10.0, 1, 9.51, 25),
('Enero', 9.7, 9.8, 10.0, 9.5, 9.6, 9.8, 9.9, 10.0, 0, 9.79, 25),
('Febrero', 9.6, 9.7, 9.9, 9.4, 9.5, 9.7, 9.8, 10.0, 0, 9.70, 25),
('Marzo', 9.8, 9.9, 10.0, 9.6, 9.7, 9.9, 10.0, 10.0, 0, 9.86, 25),
('Abril', 9.7, 9.8, 10.0, 9.5, 9.6, 9.8, 9.9, 10.0, 0, 9.79, 25),
('Mayo', 10.0, 10.0, 10.0, 9.8, 9.9, 10.0, 10.0, 10.0, 0, 9.96, 25),
('Junio', 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 0, 10.00, 25);