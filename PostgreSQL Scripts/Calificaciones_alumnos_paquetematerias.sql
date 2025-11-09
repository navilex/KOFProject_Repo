
CREATE TABLE public.paquetematerias (

    idpaquetematerias SERIAL PRIMARY KEY,
    materia1 character varying(45) NOT NULL,
    materia2 character varying(45) NOT NULL,
    materia3 character varying(45) NOT NULL,
    materia4 character varying(45) NOT NULL,
    materia5 character varying(45) NOT NULL,
    materia6 character varying(45) NOT NULL,
    materia7 character varying(45) NOT NULL,
    materia8 character varying(20) NOT NULL
);

INSERT INTO public.paquetematerias (materia1, materia2, materia3, materia4, materia5, materia6, materia7, materia8) 
VALUES 
('Español', 'Inglés', 'Artes', 'Matemáticas', 'Tecnología', 'Conocimiento del Medio', 'Form. Cívica y ética', 'Ed. Física');

INSERT INTO public.paquetematerias (materia1, materia2, materia3, materia4, materia5, materia6, materia7, materia8) 
VALUES 
('Español', 'Inglés', 'Artes', 'Matemáticas', 'Tecnología', 'Ciencias Naturales', 'Form. Cívica y ética', 'Ed. Física');

ALTER TABLE grupos ADD COLUMN idpaquetematerias integer;

ALTER TABLE public.grupos
ADD CONSTRAINT fk_paquetematerias
FOREIGN KEY (idpaquetematerias) REFERENCES public.paquetematerias(idpaquetematerias);

select * from paquetematerias;

select * from grupos;

select * from alumnos;

select * from calificaciones_inferior;