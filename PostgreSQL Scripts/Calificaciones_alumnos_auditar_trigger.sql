DO $$
DECLARE
    v_tablename TEXT;
BEGIN
    -- Iterar sobre TODAS las tablas (excepto bitacora)
    FOR v_tablename IN
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = 'public'
          AND tablename != 'bitacora'
    LOOP
        -- Construir el nombre del trigger (ej. 'trg_log_pacientes')
        DECLARE
            v_trigger_name TEXT := 'trg_log_' || v_tablename;
        BEGIN
            -- 1. Borrar CUALQUIER trigger viejo que tenga ese nombre
            EXECUTE format('DROP TRIGGER IF EXISTS %I ON %I;', v_trigger_name, v_tablename);

            -- 2. Crear el trigger nuevo APLICANDO LA LÓGICA
            IF v_tablename = 'secretarios' THEN
                -- CASO ESPECIAL: Tabla 'secretarios' usa el trigger ROW
                EXECUTE format(
                    'CREATE TRIGGER %I
                     AFTER INSERT OR UPDATE OR DELETE ON %I
                     FOR EACH ROW -- (POR FILA)
                     EXECUTE FUNCTION fn_registrar_bitacora_row();',
                     v_trigger_name,
                     v_tablename
                );
                RAISE NOTICE 'Trigger ROW [trg_log_%] creado para la tabla [%]', v_tablename, v_tablename;
            ELSE
                -- CASO NORMAL: Todas las demás tablas usan el trigger STATEMENT
                EXECUTE format(
                    'CREATE TRIGGER %I
                     AFTER INSERT OR UPDATE OR DELETE ON %I
                     FOR EACH STATEMENT -- (POR INSTRUCCIÓN)
                     EXECUTE FUNCTION fn_registrar_bitacora_statement();',
                     v_trigger_name,
                     v_tablename
                );
                RAISE NOTICE 'Trigger STATEMENT [trg_log_%] creado para la tabla [%]', v_tablename, v_tablename;
            END IF;
        END;
    END LOOP;
END;
$$;