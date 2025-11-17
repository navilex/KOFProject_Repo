#!/bin/bash

# ==========================================================
# SCRIPT DE RESPALDO DE POSTGRESQL (PARA GIT BASH EN WINDOWS)
# ==========================================================

# --- Configuración ---
USUARIO="postgres"
HOST="localhost"
PUERTO="5432"
DB_NAME="Calificaciones_alumnos"

# Ruta absoluta fácil de encontrar
BACKUP_DIR="/c/MisRespaldosPostgres"

# Fecha para el nombre del archivo
FECHA=$(date +"%Y-%m-%d_%H%M%S")

# --- Fin Configuración ---

mkdir -p $BACKUP_DIR
echo "Iniciando proceso de respaldo para PostgreSQL..."
echo "Host: $HOST"
echo "Base de Datos: $DB_NAME"
echo "Destino: C:\\MisRespaldosPostgres"
echo "------------------------------------------------"

echo "Paso 1: Respaldando roles y globales..."
pg_dumpall -U $USUARIO -h $HOST -p $PUERTO --globals-only -f "${BACKUP_DIR}/globales_${FECHA}.sql"
if [ $? -ne 0 ]; then
  echo "Error al respaldar los globales. Abortando."
  exit 1
fi

echo "Paso 2: Respaldando la base de datos ${DB_NAME}..."
pg_dump -U $USUARIO -h $HOST -p $PUERTO -Fc -d ${DB_NAME} -f "${BACKUP_DIR}/${DB_NAME}_${FECHA}.dump"
if [ $? -ne 0 ]; then
  echo "Error al respaldar la base de datos ${DB_NAME}. Abortando."
  exit 1
fi

echo "------------------------------------------------"
echo "✅ Respaldo completado con éxito."
echo "Archivos creados en C:\\MisRespaldosPostgres"
read -p "Presiona Enter para salir..."
