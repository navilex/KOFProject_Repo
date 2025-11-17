CREATE TABLE Calificaciones_superior (
    id_calificacion SERIAL PRIMARY KEY,
    Mes VARCHAR(50) NOT NULL,
    Espanol DECIMAL(4, 2) NOT NULL,
    Ingles DECIMAL(4, 2) NOT NULL,
    Artes DECIMAL(4, 2) NOT NULL,
    Matematicas DECIMAL(4, 2) NOT NULL,
    Tecnologia DECIMAL(4, 2) NOT NULL,
    Ciencias_naturales DECIMAL(4, 2) NOT NULL,
    Geografia DECIMAL(4, 2) NOT NULL,
    Historia DECIMAL(4, 2) NOT NULL,
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

INSERT INTO Calificaciones_superior (
    Mes, Espanol, Ingles, Artes, Matematicas, Tecnologia, 
    Ciencias_naturales, Geografia, Historia, Civica_y_etica, 
    Educacion_fisica, Inasistencias, Promedio, id_alumno
) 
VALUES
('Diagnostico', 8.0, 8.5, 9.0, 7.5, 8.0, 8.5, 8.0, 8.5, 9.0, 10.0, 0, 8.5, 25),
('Septiembre', 8.5, 8.0, 9.5, 8.0, 8.5, 8.0, 8.5, 9.0, 9.5, 10.0, 2, 8.7, 25),
('Octubre', 9.0, 8.5, 9.0, 8.5, 9.0, 8.5, 9.0, 8.5, 9.0, 10.0, 1, 8.9, 25),
('Nov/Dic', 7.5, 8.0, 8.5, 7.0, 8.0, 7.5, 8.0, 8.5, 8.0, 10.0, 3, 8.1, 25),
('Enero', 8.0, 8.5, 9.0, 8.0, 8.5, 8.0, 8.5, 9.0, 9.0, 10.0, 0, 8.6, 25),
('Febrero', 9.5, 9.0, 9.5, 9.0, 9.0, 9.5, 9.0, 9.5, 9.0, 10.0, 1, 9.2, 25),
('Marzo', 8.5, 8.0, 9.0, 8.5, 8.5, 8.0, 8.5, 8.0, 9.0, 10.0, 2, 8.6, 25),
('Abril', 9.0, 9.5, 9.0, 9.0, 9.5, 9.0, 9.0, 9.5, 9.5, 10.0, 0, 9.2, 25),
('Mayo', 8.0, 8.5, 8.5, 7.5, 8.0, 8.5, 8.0, 8.5, 8.0, 10.0, 1, 8.3, 25),
('Junio', 9.5, 9.0, 9.5, 9.5, 9.0, 9.5, 9.5, 9.0, 9.5, 10.0, 0, 9.3, 25);

select * from calificaciones_superior;

select * from alumnos;

select * from grupos;