ALTER TABLE alumnos
ADD CONSTRAINT fk_id_grupo
FOREIGN KEY (id_grupo)
REFERENCES Grupos (id_grupo) ON DELETE SET NULL;

select * from alumnos;