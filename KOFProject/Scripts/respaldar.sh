#!/bin/bash
# Este script encontrará la carpeta /Respaldos relativa a su ubicación
# La contraseña es proporcionada por PGPASSWORD

echo "Iniciando script de respaldo (.sh)..."

# --- Rutas Relativas ---
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
PROJECT_DIR=$(dirname "$SCRIPT_DIR")
BACKUP_DIR="$PROJECT_DIR/Respaldos"

# --- Configuración DB ---
USUARIO="postgres"
HOST="localhost"
PUERTO="5432"
DB_NAME="Calificaciones_alumnos"
FECHA=$(date +"%Y-%m-%d_%H%M%S")

# --- Ejecución ---
mkdir -p "$BACKUP_DIR"
echo "Destino del respaldo: $BACKUP_DIR"

echo "Paso 1: Respaldando roles y globales..."
pg_dumpall -U $USUARIO -h $HOST -p $PUERTO --globals-only -f "$BACKUP_DIR/globales_${FECHA}.sql"
if [ $? -ne 0 ]; then echo "Error en pg_dumpall."; exit 1; fi

echo "Paso 2: Respaldando la base de datos ${DB_NAME}..."
pg_dump -U $USUARIO -h $HOST -p $PUERTO -Fc -d ${DB_NAME} -f "$BACKUP_DIR/${DB_NAME}_${FECHA}.dump"
if [ $? -ne 0 ]; then echo "Error en pg_dump."; exit 1; fi

echo "------------------------------------------------"
echo "✅ Respaldo completado con éxito."
