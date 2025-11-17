-- Reemplaza 'pacientes'
CREATE TRIGGER trg_log_alumnos_stmt
AFTER INSERT OR UPDATE OR DELETE ON alumnos
FOR EACH STATEMENT -- <-- La parte clave
EXECUTE FUNCTION fn_registrar_bitacora_statement();