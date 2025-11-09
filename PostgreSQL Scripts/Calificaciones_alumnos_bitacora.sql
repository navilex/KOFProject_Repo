CREATE TABLE Bitacora (
    id_bitacora SERIAL PRIMARY KEY,
    Fecha TIMESTAMP NOT NULL,
    Instruccion VARCHAR(1000) NOT NULL,
    id_usuario INTEGER NOT NULL,
    CONSTRAINT fk_usuario_bitacora
        FOREIGN KEY (id_usuario)
        REFERENCES Secretarios (id_secretario)
        ON DELETE CASCADE
);