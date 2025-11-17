CREATE OR REPLACE FUNCTION fn_registrar_bitacora()
RETURNS TRIGGER AS $$
DECLARE
    v_id_usuario_app INTEGER;
    v_query_text TEXT;
BEGIN
    -- PASO 1: Obtener el 'id_usuario' de la APLICACIÓN.
    -- Usamos un try-catch por si la variable no está configurada.
    BEGIN
        v_id_usuario_app := current_setting('myapp.current_user_id')::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        -- Si 'myapp.current_user_id' no está configurado, 
        -- asignamos un ID por defecto (ej. 0 o -1) para indicar un log del sistema
        -- O podríamos lanzar una excepción:
        -- RAISE EXCEPTION 'El ID de usuario de la aplicación (myapp.current_user_id) no está configurado.';
        v_id_usuario_app := 0; -- ID por defecto para "usuario desconocido" o "sistema"
    END;

    -- PASO 2: Obtener la instrucción SQL que se está ejecutando.
    v_query_text := current_query();

    -- PASO 3: Insertar el registro en la tabla Bitacora.
    INSERT INTO Bitacora (Fecha, Instruccion, id_usuario)
    VALUES (
        NOW(),
        LEFT(v_query_text, 1000), -- Usamos LEFT() para truncar si la consulta es más larga que VARCHAR(1000)
        v_id_usuario_app
    );

    -- PASO 4: Retornar el registro (para triggers AFTER, se suele ignorar, 
    -- pero es buena práctica. Para DELETE retornamos OLD, para los demás NEW).
    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;

EXCEPTION
    -- Capturar cualquier error durante la inserción en la bitácora
    -- para evitar que la operación principal (el INSERT/UPDATE/DELETE) falle.
    WHEN OTHERS THEN
        RAISE WARNING 'No se pudo registrar en la bitácora: %', SQLERRM;
        -- Devolver el control para que la operación original no falle.
        IF (TG_OP = 'DELETE') THEN
            RETURN OLD;
        ELSE
            RETURN NEW;
        END IF;
END;
$$ LANGUAGE plpgsql;