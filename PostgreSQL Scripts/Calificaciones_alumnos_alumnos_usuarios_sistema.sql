-- 1. Crear la tabla con las columnas exactas que pide tu Java
CREATE TABLE public.usuarios_sistema (
    id_usuario SERIAL PRIMARY KEY,           -- Requerido por: rs.getInt("id_usuario")
    usuario_postgresql VARCHAR(50) NOT NULL, -- Requerido por: WHERE usuario_postgresql = ...
    nombre_completo VARCHAR(100),            -- Requerido por: rs.getString("nombre_completo")
    tipo_usuario VARCHAR(50),                -- Requerido por: rs.getString("tipo_usuario")
    activo BOOLEAN DEFAULT TRUE              -- Requerido por: AND activo = true
);

-- 2. Dar los permisos necesarios a tu usuario de conexión (director_ivan)
GRANT ALL ON TABLE public.usuarios_sistema TO director_ivan;
GRANT USAGE, SELECT ON SEQUENCE public.usuarios_sistema_id_usuario_seq TO director_ivan;

-- 3. ¡CRUCIAL! Insertar tu usuario actual para que el sistema lo reconozca
-- Si no haces esto, la conexión funcionará pero el trigger fallará o dará ID nulo.
INSERT INTO public.usuarios_sistema (usuario_postgresql, nombre_completo, tipo_usuario, activo)
VALUES ('director_ivan', 'Director Iván De la Rosa', 'Administrador', true);