@echo off
REM Este script encontrará la carpeta /Respaldos relativa a su ubicación
REM La contraseña es proporcionada por PGPASSWORD

echo Iniciando script de respaldo (.bat)...

REM --- Rutas Relativas ---
set "SCRIPT_DIR=%~dp0"
set "BACKUP_DIR=%SCRIPT_DIR%..\Respaldos"

REM --- Configuración DB ---
set "USUARIO=postgres"
set "HOST=localhost"
set "PUERTO=5432"
set "DB_NAME=Calificaciones_alumnos"

REM Fecha y Hora
set "FECHA_HORA=%DATE%_%TIME%"
set "FECHA_HORA=%FECHA_HORA:/=-%"
set "FECHA_HORA=%FECHA_HORA::=-%"
set "FECHA_HORA=%FECHA_HORA: =0%"
set "FECHA_HORA=%FECHA_HORA:,=.%"
set "FECHA_HORA=%FECHA_HORA:.=%"

REM --- Ejecución ---
if not exist "%BACKUP_DIR%" (
    echo "Creando directorio: %BACKUP_DIR%"
    mkdir "%BACKUP_DIR%"
)
echo "Destino del respaldo: %BACKUP_DIR%"

echo Paso 1: Respaldando roles y globales...
pg_dumpall.exe -U %USUARIO% -h %HOST% -p %PUERTO% --globals-only -f "%BACKUP_DIR%\globales_%FECHA_HORA%.sql"
if %ERRORLEVEL% neq 0 (
    echo Error en pg_dumpall. Abortando.
    goto end
)

echo Paso 2: Respaldando la base de datos %DB_NAME%...
pg_dump.exe -U %USUARIO% -h %HOST% -p %PUERTO% -Fc -d %DB_NAME% -f "%BACKUP_DIR%\%DB_NAME%_%FECHA_HORA%.dump"
if %ERRORLEVEL% neq 0 (
    echo Error en pg_dump. Abortando.
    goto end
)

echo ------------------------------------------------
echo.
echo Respaldo completado con exito.

:end
