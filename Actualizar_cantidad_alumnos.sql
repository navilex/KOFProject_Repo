CREATE OR REPLACE FUNCTION actualizar_cantidad_alumnos()
RETURNS TRIGGER AS $$
BEGIN
    -- Cuando se inserta un alumno
    IF (TG_OP = 'INSERT') THEN
        UPDATE Grupos
        SET cantidad_alumnos = cantidad_alumnos + 1
        WHERE id_grupo = NEW.id_grupo;
        RETURN NEW;

    -- Cuando se elimina un alumno
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE Grupos
        SET cantidad_alumnos = cantidad_alumnos - 1
        WHERE id_grupo = OLD.id_grupo;
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;