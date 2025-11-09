CREATE OR REPLACE FUNCTION fn_actualizar_cantidad_alumnos()
RETURNS TRIGGER AS $$
BEGIN
    -- 1. Cuando se INSERTA un alumno nuevo
    IF (TG_OP = 'INSERT') THEN
        UPDATE Grupos
        SET Cantidad_alumnos = Cantidad_alumnos + 1
        WHERE id_grupo = NEW.id_grupo;

    -- 2. Cuando se ELIMINA un alumno
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE Grupos
        SET Cantidad_alumnos = Cantidad_alumnos - 1
        WHERE id_grupo = OLD.id_grupo;

    -- 3. Cuando se ACTUALIZA un alumno (p.ej., cambio de grupo)
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Solo actuar si el ID del grupo realmente cambió
        IF NEW.id_grupo IS DISTINCT FROM OLD.id_grupo THEN
            
            -- Restar 1 del grupo ANTIGUO
            UPDATE Grupos
            SET Cantidad_alumnos = Cantidad_alumnos - 1
            WHERE id_grupo = OLD.id_grupo;
            
            -- Sumar 1 al grupo NUEVO
            UPDATE Grupos
            SET Cantidad_alumnos = Cantidad_alumnos + 1
            WHERE id_grupo = NEW.id_grupo;
        END IF;
    END IF;

    -- Retornar la fila modificada
    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

UPDATE Grupos g
SET Cantidad_alumnos = (
    SELECT COUNT(*)
    FROM alumnos a
    WHERE a.id_grupo = g.id_grupo
);