CREATE OR REPLACE FUNCTION crear_secretarios()
RETURNS void AS $$
DECLARE
    reg RECORD;
    existe INTEGER;
BEGIN
    -- recorrer todos los registros de la tabla secretarios
    FOR reg IN
        SELECT usuario, clave FROM secretarios
    LOOP
        -- verificar si el usuario ya existe en PostgreSQL
        SELECT COUNT(*) INTO existe
        FROM pg_catalog.pg_roles
        WHERE rolname = reg.usuario;

        -- si no existe, crearlo y asignarlo al rol secretarios
        IF existe = 0 THEN
            EXECUTE format('CREATE USER %I WITH PASSWORD %L', reg.usuario, reg.clave);
            EXECUTE format('GRANT secretarios TO %I', reg.usuario);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;