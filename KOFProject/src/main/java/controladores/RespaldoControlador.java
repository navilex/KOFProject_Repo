/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Ivan De la Rosa
 * (Modificado para aceptar contraseña como parámetro)
 */
public class RespaldoControlador {

    // Variables de ruta
    private final String projectDir;
    private final String scriptsDir;
    private final String backupDir;

    // --- CAMBIO 1: ELIMINADA LA CONTRASEÑA "HARDCODEADA" ---
    // private final String DB_PASSWORD = "037469117d"; // <-- ELIMINADO

    public RespaldoControlador() {
        // Define las rutas clave una sola vez
        this.projectDir = System.getProperty("user.dir");
        this.scriptsDir = projectDir + File.separator + "Scripts";
        this.backupDir = projectDir + File.separator + "Respaldos";
    }

    /**
     * MÉTODO PÚBLICO PRINCIPAL
     * Orquesta la creación y ejecución de los scripts de respaldo.
     * * --- CAMBIO 2: ACEPTAR LA CONTRASEÑA COMO PARÁMETRO ---
     * @param password La contraseña de la BD proporcionada por el usuario
     * @return La salida de la consola del script.
     */
    public String crearYEjecutarRespaldo(String password) throws Exception {
        // 1. Crear directorios necesarios
        String createResult = crearDirectoriosYScripts();
        if (!createResult.equals("OK")) {
            // Lanza una excepción para que el 'done()' en la UI la capture
            throw new Exception("Error en la preparación: " + createResult);
        }

        // 2. Ejecutar el script (pasándole la contraseña)
        return ejecutarScript(password);
    }
    
    /**
     * (Privado) Crea las carpetas y los archivos .sh y .bat
     */
    private String crearDirectoriosYScripts() {
        try {
            // Crear carpeta /Scripts
            File scriptsFolder = new File(scriptsDir);
            if (!scriptsFolder.exists() && !scriptsFolder.mkdirs()) {
                return "No se pudo crear la carpeta 'Scripts'.";
            }
            
            // Crear carpeta /Respaldos
            File backupFolder = new File(backupDir);
            if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                return "No se pudo crear la carpeta 'Respaldos'.";
            }

            // Escribir script .sh
            escribirArchivo(
                new File(scriptsFolder, "respaldar.sh"),
                getContenidoScriptSh()
            );

            // Escribir script .bat
            escribirArchivo(
                new File(scriptsFolder, "respaldar.bat"),
                getContenidoScriptBat()
            );
            
            return "OK";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al crear archivos de script: " + e.getMessage();
        }
    }

    /**
     * (Privado) Ejecuta el script correspondiente al S.O.
     * * --- CAMBIO 3: ACEPTAR LA CONTRASEÑA COMO PARÁMETRO ---
     * @param password La contraseña de la BD
     */
    private String ejecutarScript(String password) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String scriptFileName;
        File scriptFile;
        String[] command;

        if (os.contains("win")) {
            scriptFileName = "respaldar.bat";
            scriptFile = new File(scriptsDir, scriptFileName);
            command = new String[]{"cmd.exe", "/c", scriptFile.getAbsolutePath()};
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            scriptFileName = "respaldar.sh";
            scriptFile = new File(scriptsDir, scriptFileName);
            
            try {
                new ProcessBuilder("chmod", "+x", scriptFile.getAbsolutePath()).start().waitFor();
            } catch (Exception e) {
                throw new Exception("Error al dar permisos de ejecución a .sh: " + e.getMessage());
            }
            
            command = new String[]{"/bin/bash", scriptFile.getAbsolutePath()};
        } else {
            throw new Exception("Error: Sistema operativo no compatible.");
        }
        
        if (!scriptFile.exists()) {
             throw new Exception("Error: El archivo de script no existe: " + scriptFile.getAbsolutePath());
        }

        // Ejecutar proceso
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            
            // --- CAMBIO 4: USAR LA CONTRASEÑA DEL PARÁMETRO ---
            processBuilder.environment().put("PGPASSWORD", password);
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Leer la salida del proceso (stdout y stderr)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } 

            if (!process.waitFor(2, TimeUnit.MINUTES)) {
                process.destroy(); 
                throw new Exception("Error: El script tardó demasiado (timeout).");
            }

            int exitCode = process.exitValue();
            
            // Verificar si la salida contiene errores de autenticación
            if (output.toString().contains("autentificación por contraseña falló")) {
                // Lanza una excepción para que la UI la maneje
                throw new Exception("Error: La contraseña de la base de datos es incorrecta.");
            }
            
            if (exitCode != 0) {
                 throw new Exception("Error (código: " + exitCode + "):\n\n" + output.toString());
            }

            // Si todo sale bien, devuelve la salida
            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
            // Re-lanza la excepción para que el 'done()' la capture
            throw e; 
        }
    }
    
    private void escribirArchivo(File archivo, String contenido) throws Exception {
        try (PrintWriter out = new PrintWriter(archivo, StandardCharsets.UTF_8.name())) {
            out.print(contenido);
        }
    }

    // --- LOS MÉTODOS getContenidoScriptSh() y getContenidoScriptBat() ---
    // --- NO NECESITAN NINGÚN CAMBIO ---

    private String getContenidoScriptSh() {
        return """
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
               """;
    }

    private String getContenidoScriptBat() {
        return """
               @echo off
               REM Este script encontrará la carpeta /Respaldos relativa a su ubicación
               REM La contraseña es proporcionada por PGPASSWORD
               
               echo Iniciando script de respaldo (.bat)...
               
               REM --- Rutas Relativas ---
               set "SCRIPT_DIR=%~dp0"
               set "BACKUP_DIR=%SCRIPT_DIR%..\\Respaldos"
               
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
               pg_dumpall.exe -U %USUARIO% -h %HOST% -p %PUERTO% --globals-only -f "%BACKUP_DIR%\\globales_%FECHA_HORA%.sql"
               if %ERRORLEVEL% neq 0 (
                   echo Error en pg_dumpall. Abortando.
                   goto end
               )
               
               echo Paso 2: Respaldando la base de datos %DB_NAME%...
               pg_dump.exe -U %USUARIO% -h %HOST% -p %PUERTO% -Fc -d %DB_NAME% -f "%BACKUP_DIR%\\%DB_NAME%_%FECHA_HORA%.dump"
               if %ERRORLEVEL% neq 0 (
                   echo Error en pg_dump. Abortando.
                   goto end
               )
               
               echo ------------------------------------------------
               echo.
               echo Respaldo completado con exito.
               
               :end
               """;
    }
}