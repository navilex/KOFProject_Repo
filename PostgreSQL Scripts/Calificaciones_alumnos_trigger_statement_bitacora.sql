CREATE OR REPLACE FUNCTION fn_registrar_bitacora_statement()
RETURNS TRIGGER AS $$
DECLARE
    v_id_usuario_app INTEGER;
    v_query_text TEXT;
BEGIN
    -- PASO 1: Intentar obtener el ID de la sesión.
    BEGIN
        v_id_usuario_app := current_setting('myapp.current_user_id')::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        v_id_usuario_app := NULL; -- Por defecto es NULL
    END;

    -- PASO 2: Decidir si registrar.
    -- (Ya no hay lógica de 'auto-registro' aquí)
    -- Si tenemos un ID válido, registramos.
    IF v_id_usuario_app IS NOT NULL THEN
    
        BEGIN
            v_query_text := current_query();

            INSERT INTO Bitacora (Fecha, Instruccion, id_usuario)
            VALUES (
                NOW(),
                LEFT(v_query_text, 1000), 
                v_id_usuario_app
            );
        EXCEPTION
            WHEN OTHERS THEN
                RAISE WARNING 'Fallo inesperado al registrar en bitacora (statement): %', SQLERRM;
        END;
        
    END IF; -- Fin de la condición de registro

    -- En triggers FOR EACH STATEMENT, el valor de retorno se ignora.
    RETURN NULL;

END;
$$ LANGUAGE plpgsql;