CREATE TABLE envios_documentos (
    id_envio SERIAL PRIMARY KEY,
    curp_alumno VARCHAR(18),
    id_grupo INTEGER,
    tipo_documento VARCHAR(20) NOT NULL,
    destinatario_tipo VARCHAR(10) NOT NULL,
    destinatario_nombre VARCHAR(150),
    destinatario_correo VARCHAR(100),
    destinatario_telefono VARCHAR(20),
    metodo_envio VARCHAR(20) NOT NULL,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ruta_archivo VARCHAR(255),
    estado VARCHAR(20) DEFAULT 'ENVIADO',
    observaciones TEXT,
    CONSTRAINT fk_alumno FOREIGN KEY (curp_alumno) REFERENCES alumnos(curp) ON DELETE CASCADE,
    CONSTRAINT fk_grupo FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo) ON DELETE CASCADE
);