CREATE OR REPLACE FUNCTION gestionar_secretarios()
RETURNS void AS $$
DECLARE
    -- Para recorrer los usuarios en la tabla 'secretarios'
    reg_secretario RECORD;
    
    -- Para recorrer los usuarios existentes en PostgreSQL que tienen el rol 'secretarios'
    reg_rol RECORD;
    
    usuario_existe_en_tabla INTEGER;
    
BEGIN
    -- 1. CREAR o ACTUALIZAR usuarios que están en la tabla 'secretarios'
    
    FOR reg_secretario IN
        SELECT usuario, clave FROM secretarios
    LOOP
        -- a. Verificar si el usuario ya existe en PostgreSQL
        SELECT COUNT(*) INTO usuario_existe_en_tabla
        FROM pg_catalog.pg_roles
        WHERE rolname = reg_secretario.usuario;

        IF usuario_existe_en_tabla = 0 THEN
            -- b. Si no existe, CREAR el usuario y asignar el rol 'secretarios'
            EXECUTE format('CREATE USER %I WITH PASSWORD %L', reg_secretario.usuario, reg_secretario.clave);
            EXECUTE format('GRANT secretarios TO %I', reg_secretario.usuario);
        ELSE
            -- c. Si existe, ACTUALIZAR la contraseña
            EXECUTE format('ALTER USER %I WITH PASSWORD %L', reg_secretario.usuario, reg_secretario.clave);
        END IF;
    END LOOP;

    -- 2. ELIMINAR usuarios que ya NO están en la tabla 'secretarios'
    
    -- Recorrer todos los roles de PostgreSQL que tienen asignado el rol 'secretarios'
    FOR reg_rol IN
        SELECT r.rolname AS usuario_postgres
        FROM pg_catalog.pg_roles r
        JOIN pg_auth_members am ON am.member = r.oid
        JOIN pg_catalog.pg_roles ar ON ar.oid = am.roleid
        WHERE ar.rolname = 'secretarios'
		AND r.rolname != 'directores'
    LOOP
        -- a. Verificar si el usuario de PostgreSQL todavía existe en la tabla 'secretarios'
        SELECT COUNT(*) INTO usuario_existe_en_tabla
        FROM secretarios
        WHERE usuario = reg_rol.usuario_postgres;

        IF usuario_existe_en_tabla = 0 THEN
            -- b. Si no existe, ELIMINAR el usuario de PostgreSQL
            EXECUTE format('DROP USER %I', reg_rol.usuario_postgres);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;