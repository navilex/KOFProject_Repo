@echo off
REM ==========================================================
REM SCRIPT DE RESPALDO DE POSTGRESQL (NATIVO DE WINDOWS .bat)
REM ==========================================================
REM Este script asume que las variables de entorno para
REM PostgreSQL (pg_dump.exe) están configuradas en el PATH.

REM --- Configuración ---
set "USUARIO=postgres"
set "HOST=localhost"
set "PUERTO=5432"
set "DB_NAME=Calificaciones_alumnos"

REM Ruta absoluta fácil de encontrar (sintaxis de Windows)
set "BACKUP_DIR=C:\MisRespaldosPostgres"

REM Generar fecha y hora (esto es un poco complejo en Batch)
REM Reemplaza caracteres no válidos para nombres de archivo
set "FECHA_HORA=%DATE%_%TIME%"
set "FECHA_HORA=%FECHA_HORA:/=-%"
set "FECHA_HORA=%FECHA_HORA::=-%"
set "FECHA_HORA=%FECHA_HORA: =0%"
set "FECHA_HORA=%FECHA_HORA:,=.%"
set "FECHA_HORA=%FECHA_HORA:.=%"

REM --- Fin Configuración ---

REM Asegura que el directorio exista
if not exist "%BACKUP_DIR%" (
    echo Creando directorio: %BACKUP_DIR%
    mkdir "%BACKUP_DIR%"
)

echo Iniciando proceso de respaldo para PostgreSQL...
echo Host: %HOST%
echo Base de Datos: %DB_NAME%
echo Destino: %BACKUP_DIR%
echo ------------------------------------------------

REM 1. Respaldo de Roles y Globales
echo Paso 1: Respaldando roles y globales...
pg_dumpall.exe -U %USUARIO% -h %HOST% -p %PUERTO% --globals-only -f "%BACKUP_DIR%\globales_%FECHA_HORA%.sql"

if %ERRORLEVEL% neq 0 (
    echo Error al respaldar los globales. Abortando.
    goto end
)

REM 2. Respaldo de la Base de Datos
echo Paso 2: Respaldando la base de datos %DB_NAME%...
pg_dump.exe -U %USUARIO% -h %HOST% -p %PUERTO% -Fc -d %DB_NAME% -f "%BACKUP_DIR%\%DB_NAME%_%FECHA_HORA%.dump"

if %ERRORLEVEL% neq 0 (
    echo Error al respaldar la base de datos %DB_NAME%. Abortando.
    goto end
)

echo ------------------------------------------------
echo.
echo Respaldo completado con exito.

:end
echo Presiona cualquier tecla para salir...
pause > nul
