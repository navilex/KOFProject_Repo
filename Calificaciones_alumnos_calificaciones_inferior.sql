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