-- Conectado a: Calificaciones_alumnos
SET client_encoding = 'UTF8';

-- ==================================================================
-- 1. FUNCIONES (LÓGICA, AUDITORÍA Y NEGOCIO)
-- ==================================================================

-- Función auxiliar para insertar en bitácora
CREATE OR REPLACE FUNCTION public.registrar_en_bitacora(p_usuario_id integer, p_instruccion text)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    -- Permitimos NULL en usuario_id para evitar errores
    INSERT INTO bitacora (id_usuario, instruccion, fecha)
    VALUES (p_usuario_id, p_instruccion, CURRENT_TIMESTAMP);
END;
$$;

-- Funciones Trigger Específicas para ALUMNOS (Del Backup)
CREATE OR REPLACE FUNCTION public.trigger_bitacora_insert_alumnos() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    v_user_id INTEGER;
    v_instruccion TEXT;
BEGIN
    BEGIN
        v_user_id := NULLIF(current_setting('myapp.current_user_id', true), '')::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        v_user_id := NULL;
    END;
    v_instruccion := '  INSERCIÓN de alumno: ' || COALESCE(NEW.nombre, '') || ' ' || COALESCE(NEW.apellido, '') || ' (CURP: ' || COALESCE(NEW.curp, 'N/A') || ')';
    PERFORM registrar_en_bitacora(v_user_id, v_instruccion);
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.trigger_bitacora_update_alumnos() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    v_user_id INTEGER;
    v_instruccion TEXT;
    v_cambios TEXT := '';
BEGIN
    BEGIN
        v_user_id := NULLIF(current_setting('myapp.current_user_id', true), '')::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        v_user_id := NULL;
    END;
    
    IF OLD.nombre IS DISTINCT FROM NEW.nombre THEN v_cambios := v_cambios || 'nombre, '; END IF;
    IF OLD.apellido IS DISTINCT FROM NEW.apellido THEN v_cambios := v_cambios || 'apellido, '; END IF;
    IF OLD.edad IS DISTINCT FROM NEW.edad THEN v_cambios := v_cambios || 'edad, '; END IF;
    IF OLD.id_grupo IS DISTINCT FROM NEW.id_grupo THEN v_cambios := v_cambios || 'grupo, '; END IF;
    
    v_instruccion := '  MODIFICACIÓN de alumno: ' || COALESCE(NEW.nombre, '') || ' | Cambios: ' || v_cambios;
    PERFORM registrar_en_bitacora(v_user_id, v_instruccion);
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.trigger_bitacora_delete_alumnos() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    v_user_id INTEGER;
    v_instruccion TEXT;
BEGIN
    BEGIN
        v_user_id := NULLIF(current_setting('myapp.current_user_id', true), '')::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        v_user_id := NULL;
    END;
    v_instruccion := '  ELIMINACIÓN de alumno: ' || COALESCE(OLD.nombre, '') || ' ' || COALESCE(OLD.apellido, '') || ' (CURP: ' || COALESCE(OLD.curp, 'N/A') || ')';
    PERFORM registrar_en_bitacora(v_user_id, v_instruccion);
    RETURN OLD;
END;
$$;

-- Función Genérica para auditoría (Statement)
CREATE OR REPLACE FUNCTION fn_registrar_bitacora_statement() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    v_id_usuario_app INTEGER;
    v_query_text TEXT;
BEGIN
    BEGIN
        v_id_usuario_app := current_setting('myapp.current_user_id')::INTEGER;
    EXCEPTION WHEN OTHERS THEN
        v_id_usuario_app := NULL;
    END;

    BEGIN
        v_query_text := current_query();
        INSERT INTO Bitacora (Fecha, Instruccion, id_usuario)
        VALUES (NOW(), LEFT(v_query_text, 1000), v_id_usuario_app);
    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Fallo registro bitacora: %', SQLERRM;
    END;
    RETURN NULL;
END;
$$;

-- Función para sincronizar usuarios de Postgres con tabla Secretarios
CREATE OR REPLACE FUNCTION gestionar_secretarios() RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    reg_secretario RECORD;
    reg_rol RECORD;
    usuario_existe_en_tabla INTEGER;
BEGIN
    FOR reg_secretario IN SELECT usuario, clave FROM secretarios LOOP
        SELECT COUNT(*) INTO usuario_existe_en_tabla FROM pg_catalog.pg_roles WHERE rolname = reg_secretario.usuario;
        IF usuario_existe_en_tabla = 0 THEN
            EXECUTE format('CREATE USER %I WITH PASSWORD %L', reg_secretario.usuario, reg_secretario.clave);
            EXECUTE format('GRANT secretarios TO %I', reg_secretario.usuario);
        ELSE
            EXECUTE format('ALTER USER %I WITH PASSWORD %L', reg_secretario.usuario, reg_secretario.clave);
        END IF;
    END LOOP;
    
    FOR reg_rol IN SELECT r.rolname AS usuario_postgres FROM pg_catalog.pg_roles r
        JOIN pg_auth_members am ON am.member = r.oid JOIN pg_catalog.pg_roles ar ON ar.oid = am.roleid
        WHERE ar.rolname = 'secretarios' AND r.rolname != 'directores' LOOP
        SELECT COUNT(*) INTO usuario_existe_en_tabla FROM secretarios WHERE usuario = reg_rol.usuario_postgres;
        IF usuario_existe_en_tabla = 0 THEN EXECUTE format('DROP USER %I', reg_rol.usuario_postgres); END IF;
    END LOOP;
END;
$$;

-- Función de Negocio: Actualizar conteo de alumnos
CREATE OR REPLACE FUNCTION fn_actualizar_cantidad_alumnos() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE Grupos SET Cantidad_alumnos = Cantidad_alumnos + 1 WHERE id_grupo = NEW.id_grupo;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE Grupos SET Cantidad_alumnos = Cantidad_alumnos - 1 WHERE id_grupo = OLD.id_grupo;
    ELSIF (TG_OP = 'UPDATE') THEN
        IF NEW.id_grupo IS DISTINCT FROM OLD.id_grupo THEN
            UPDATE Grupos SET Cantidad_alumnos = Cantidad_alumnos - 1 WHERE id_grupo = OLD.id_grupo;
            UPDATE Grupos SET Cantidad_alumnos = Cantidad_alumnos + 1 WHERE id_grupo = NEW.id_grupo;
        END IF;
    END IF;
    IF (TG_OP = 'DELETE') THEN RETURN OLD; ELSE RETURN NEW; END IF;
END;
$$;

-- ==================================================================
-- 2. TABLAS (ESTRUCTURA EXACTA DEL BACKUP)
-- ==================================================================

-- 1. Paquete Materias (pk: id)
CREATE TABLE public.paquetematerias (
    id serial PRIMARY KEY,
    materia1 character varying(45),
    materia2 character varying(45),
    materia3 character varying(45),
    materia4 character varying(45),
    materia5 character varying(45),
    materia6 character varying(45),
    materia7 character varying(45),
    materia8 character varying(20)
);

-- 2. Secretarios
CREATE TABLE public.secretarios (
    id_secretario serial PRIMARY KEY,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    usuario character varying(50) NOT NULL UNIQUE,
    clave character varying(255) NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

-- 3. Usuarios del Sistema (Del Backup)
CREATE TABLE public.usuarios_sistema (
    id_usuario serial PRIMARY KEY,
    usuario_postgresql character varying(50) NOT NULL UNIQUE,
    id_secretario integer,
    id_director integer,
    nombre_completo character varying(200),
    tipo_usuario character varying(50),
    activo boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bitácora (MODIFICADA: Sin NOT NULL en id_usuario para evitar errores)
CREATE TABLE public.bitacora (
    id_bitacora serial PRIMARY KEY,
    fecha timestamp without time zone NOT NULL,
    instruccion character varying(1000) NOT NULL,
    id_usuario integer REFERENCES public.usuarios_sistema(id_usuario) ON DELETE SET NULL
);

-- 5. Grupos (Incluye ambas columnas de paquetes según backup)
CREATE TABLE public.grupos (
    id_grupo serial PRIMARY KEY,
    grado integer NOT NULL,
    grupo character varying(50) NOT NULL,
    cantidad_alumnos integer NOT NULL,
    aula character varying(50) NOT NULL,
    nombre_maestro character varying(100) NOT NULL,
    apellido_maestro character varying(100) NOT NULL,
    domicilio_maestro character varying(255) NOT NULL,
    correo_maestro character varying(255) NOT NULL,
    telefono_maestro character varying(20) NOT NULL,
    idpaquetematerias integer REFERENCES public.paquetematerias(id),
    id_paquetematerias integer REFERENCES public.paquetematerias(id)
);

-- 6. Alumnos
CREATE TABLE public.alumnos (
    id_alumno serial PRIMARY KEY,
    curp character varying(18) NOT NULL UNIQUE,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    edad integer NOT NULL,
    genero character varying(50) NOT NULL,
    nombre_padre character varying(100) NOT NULL,
    apellido_padre character varying(100) NOT NULL,
    correo_padre character varying(255) NOT NULL UNIQUE,
    telefono_padre character varying(20) NOT NULL UNIQUE,
    id_grupo integer NOT NULL REFERENCES public.grupos(id_grupo),
    fecha_nacimiento date
);

-- 7. Calificaciones
CREATE TABLE public.calificaciones_inferior (
    id_calificacion serial PRIMARY KEY,
    mes character varying(50) NOT NULL,
    espanol numeric(4,2) NOT NULL,
    ingles numeric(4,2) NOT NULL,
    artes numeric(4,2) NOT NULL,
    matematicas numeric(4,2) NOT NULL,
    tecnologia numeric(4,2) NOT NULL,
    conocimiento_del_medio numeric(4,2) NOT NULL,
    civica_y_etica numeric(4,2) NOT NULL,
    educacion_fisica numeric(4,2) NOT NULL,
    inasistencias integer NOT NULL,
    promedio numeric(4,2) NOT NULL,
    id_alumno integer NOT NULL REFERENCES public.alumnos(id_alumno) ON DELETE CASCADE
);

CREATE TABLE public.calificaciones_superior (
    id_calificacion serial PRIMARY KEY,
    mes character varying(50) NOT NULL,
    espanol numeric(4,2) NOT NULL,
    ingles numeric(4,2) NOT NULL,
    artes numeric(4,2) NOT NULL,
    matematicas numeric(4,2) NOT NULL,
    tecnologia numeric(4,2) NOT NULL,
    ciencias_naturales numeric(4,2) NOT NULL,
    geografia numeric(4,2) NOT NULL,
    historia numeric(4,2) NOT NULL,
    civica_y_etica numeric(4,2) NOT NULL,
    educacion_fisica numeric(4,2) NOT NULL,
    inasistencias integer NOT NULL,
    promedio numeric(4,2) NOT NULL,
    id_alumno integer NOT NULL REFERENCES public.alumnos(id_alumno) ON DELETE CASCADE
);

-- 8. Envíos (Estructura actualizada del backup)
CREATE TABLE public.envios_documentos (
    id_envio serial PRIMARY KEY,
    curp_alumno character varying(18) REFERENCES public.alumnos(curp) ON DELETE CASCADE,
    id_grupo integer REFERENCES public.grupos(id_grupo) ON DELETE CASCADE,
    tipo_documento character varying(20) NOT NULL,
    destinatario_tipo character varying(10) NOT NULL,
    destinatario_nombre character varying(150),
    destinatario_correo character varying(100),
    destinatario_telefono character varying(20),
    metodo_envio character varying(20) NOT NULL,
    fecha_envio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ruta_archivo character varying(255),
    estado character varying(20) DEFAULT 'ENVIADO',
    observaciones text
);

-- ==================================================================
-- 3. TRIGGERS
-- ==================================================================

CREATE TRIGGER trigger_insert_alumnos AFTER INSERT ON public.alumnos FOR EACH ROW EXECUTE FUNCTION public.trigger_bitacora_insert_alumnos();
CREATE TRIGGER trigger_update_alumnos AFTER UPDATE ON public.alumnos FOR EACH ROW EXECUTE FUNCTION public.trigger_bitacora_update_alumnos();
CREATE TRIGGER trigger_delete_alumnos AFTER DELETE ON public.alumnos FOR EACH ROW EXECUTE FUNCTION public.trigger_bitacora_delete_alumnos();

CREATE TRIGGER trg_actualizar_cantidad_alumnos AFTER INSERT OR UPDATE OR DELETE ON public.alumnos FOR EACH ROW EXECUTE FUNCTION fn_actualizar_cantidad_alumnos();

CREATE TRIGGER trg_log_grupos AFTER INSERT OR UPDATE OR DELETE ON public.grupos FOR EACH STATEMENT EXECUTE FUNCTION public.fn_registrar_bitacora_statement();
CREATE TRIGGER trg_log_paquetematerias AFTER INSERT OR UPDATE OR DELETE ON public.paquetematerias FOR EACH STATEMENT EXECUTE FUNCTION public.fn_registrar_bitacora_statement();
CREATE TRIGGER trg_log_envios AFTER INSERT OR UPDATE OR DELETE ON public.envios_documentos FOR EACH STATEMENT EXECUTE FUNCTION public.fn_registrar_bitacora_statement();
CREATE TRIGGER trg_log_calif_inf AFTER INSERT OR UPDATE OR DELETE ON public.calificaciones_inferior FOR EACH STATEMENT EXECUTE FUNCTION public.fn_registrar_bitacora_statement();
CREATE TRIGGER trg_log_calif_sup AFTER INSERT OR UPDATE OR DELETE ON public.calificaciones_superior FOR EACH STATEMENT EXECUTE FUNCTION public.fn_registrar_bitacora_statement();

-- ==================================================================
-- 4. DATOS SEMILLA
-- ==================================================================

INSERT INTO public.paquetematerias (materia1, materia2, materia3, materia4, materia5, materia6, materia7, materia8) 
VALUES ('Español', 'Inglés', 'Artes', 'Matemáticas', 'Tecnología', 'Conocimiento del Medio', 'Form. Cívica y ética', 'Ed. Física');

INSERT INTO public.paquetematerias (materia1, materia2, materia3, materia4, materia5, materia6, materia7, materia8) 
VALUES ('Español', 'Inglés', 'Artes', 'Matemáticas', 'Tecnología', 'Ciencias Naturales', 'Form. Cívica y ética', 'Ed. Física');

-- Grupos (Se inserta en ambas columnas FK para compatibilidad)
INSERT INTO Grupos (Grado, Grupo, Cantidad_alumnos, Aula, Nombre_maestro, Apellido_maestro, Domicilio_maestro, Correo_maestro, Telefono_maestro, idpaquetematerias, id_paquetematerias) VALUES
(1, '1A', 0, 'Aula 101', 'Laura', 'Sánchez', 'Av. del Sol 1', 'laura@gmail.com', '5511223344', 1, 1),
(1, '1B', 0, 'Aula 102', 'Ricardo', 'Martínez', 'Calle Luna 2', 'ricardo@gmail.com', '5522334455', 1, 1),
(2, '2A', 0, 'Aula 201', 'Sofía', 'López', 'Blvd. Estrellas 3', 'sofia@gmail.com', '5533445566', 1, 1),
(2, '2B', 0, 'Aula 202', 'Javier', 'García', 'Paseo Nubes 4', 'javier@gmail.com', '5544556677', 1, 1),
(3, '3A', 0, 'Aula 301', 'Paola', 'Hernández', 'Camino Bosque 5', 'paola@gmail.com', '5555667788', 2, 2),
(3, '3B', 0, 'Aula 302', 'Diego', 'Ramírez', 'Río Flores 6', 'diego@gmail.com', '5566778899', 2, 2),
(4, '4A', 0, 'Aula 401', 'Ana', 'Pérez', 'Montaña Viento 7', 'ana@gmail.com', '5577889900', 2, 2),
(4, '4B', 0, 'Aula 402', 'Carlos', 'Torres', 'Laguna Cielo 8', 'carlos@gmail.com', '5588990011', 2, 2),
(5, '5A', 0, 'Aula 501', 'María', 'Díaz', 'Valle Niebla 9', 'maria@gmail.com', '5599001122', 2, 2),
(5, '5B', 0, 'Aula 502', 'David', 'Soto', 'Playa Brisa 10', 'david@gmail.com', '5500112233', 2, 2),
(6, '6A', 0, 'Aula 601', 'Sandra', 'Rojas', 'Cerro Sol 11', 'sandra@gmail.com', '5512345678', 2, 2),
(6, '6B', 0, 'Aula 602', 'Fernando', 'Castro', 'Av. Lago 12', 'fernando@gmail.com', '5598765432', 2, 2);

-- ==================================================================
-- 5. DATOS DE USUARIOS
-- ==================================================================

-- 1. Secretario Operativo
INSERT INTO public.secretarios (nombre, apellido, usuario, clave, fecha_creacion)
VALUES ('Maria', 'Lopez', 'secretario1', 'secre.clave', NOW());

-- 2. Usuarios de Sistema (Director y Secretaria)
INSERT INTO public.usuarios_sistema (usuario_postgresql, id_secretario, id_director, nombre_completo, tipo_usuario, activo, fecha_creacion)
VALUES 
('director', NULL, 1, 'Director General', 'Director', true, NOW()),
('secretario1', 1, NULL, 'Maria Lopez', 'Secretaria', true, NOW());

-- 3. Crear Login en Postgres
SELECT gestionar_secretarios();

-- ==================================================================
-- 6. PERMISOS
-- ==================================================================

GRANT ALL ON SCHEMA public TO directores;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO directores;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO directores;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO directores;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO directores;

GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLE public.alumnos TO secretarios;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLE public.grupos TO secretarios;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLE public.calificaciones_inferior TO secretarios;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLE public.calificaciones_superior TO secretarios;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLE public.bitacora TO secretarios;
GRANT SELECT, INSERT, UPDATE, DELETE, TRIGGER ON TABLE public.paquetematerias TO secretarios;
GRANT SELECT, INSERT ON TABLE public.envios_documentos TO secretarios;
GRANT SELECT, INSERT ON TABLE public.usuarios_sistema TO secretarios;
GRANT SELECT ON TABLE public.secretarios TO secretarios;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO secretarios;