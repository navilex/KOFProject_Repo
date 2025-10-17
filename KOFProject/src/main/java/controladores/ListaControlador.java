/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author Ivan De la Rosa
 */
public class ListaControlador {

    // --- Colores para las celdas ---
    private static final Color AZUL_CLARO = new DeviceRgb(214, 233, 248);
    private static final Color ROSA_CLARO = new DeviceRgb(245, 222, 239);
    private static final Color AMARILLO_CLARO = new DeviceRgb(252, 243, 215);
    private static final Color VERDE_CLARO = new DeviceRgb(213, 232, 213);

    /**
     * Clase interna para modelar los datos de un alumno para el reporte.
     */
    private static class AlumnoReporte {
        int numeroLista;
        String sexo;
        String nombreCompleto;
        Map<String, String[]> calificacionesPorMes;

        public AlumnoReporte(int numeroLista, String sexo, String nombreCompleto, Map<String, String[]> calificaciones) {
            this.numeroLista = numeroLista;
            this.sexo = sexo;
            this.nombreCompleto = nombreCompleto;
            this.calificacionesPorMes = calificaciones;
        }
    }

    public void generarReporte(Connection conexionExistente, JTextField paramGrupo, JTextField paramPrimerMes, JTextField paramSegundoMes, JTextField paramTercerMes) {
        String nombreGrupo = paramGrupo.getText();
        String[] mesesDelTrimestre = {
            paramPrimerMes.getText(),
            paramSegundoMes.getText(),
            paramTercerMes.getText()
        };

        if (nombreGrupo.trim().isEmpty() || mesesDelTrimestre[0].trim().isEmpty() ||
            mesesDelTrimestre[1].trim().isEmpty() || mesesDelTrimestre[2].trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos los campos (Grupo y los tres meses) son obligatorios.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idGrupo = CambiarGrupoPorID(conexionExistente, nombreGrupo);
        if (idGrupo == 0) {
            JOptionPane.showMessageDialog(null, "El grupo '" + nombreGrupo + "' no fue encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombreMaestro = ObtenerNombreMaestro(conexionExistente, idGrupo);

        List<AlumnoReporte> listaAlumnos = ObtenerDatosParaReporte(conexionExistente, idGrupo, mesesDelTrimestre);
        if (listaAlumnos == null || listaAlumnos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron alumnos o calificaciones para el grupo y meses seleccionados.", "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String ruta = "Reportes/Reporte_Grupo_" + nombreGrupo + ".pdf";
            PdfWriter writer = new PdfWriter(ruta);
            PdfDocument pdf = new PdfDocument(writer);
            
            // **MODIFICADO**: Se define un tamaño personalizado (440mm x 216mm).
            // Ancho: 440mm * 72 / 25.4 = 1247.24 puntos
            // Alto: 216mm * 72 / 25.4 = 612.28 puntos
            PageSize HorizontalPersonalizado = new PageSize(1247.24f, 612.28f);
            pdf.setDefaultPageSize(HorizontalPersonalizado);
            
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20);

            document.add(crearEncabezadoSuperior(nombreGrupo, nombreMaestro, mesesDelTrimestre));
            document.add(crearTablaCalificaciones(listaAlumnos, mesesDelTrimestre, idGrupo));

            document.close();
            JOptionPane.showMessageDialog(null, "Reporte de calificaciones generado en: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error al generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // --- METODOS DE OBTENCION DE DATOS ---

    private String ObtenerNombreMaestro(Connection conexion, int idGrupo) {
        String SQL = "SELECT Nombre_maestro, Apellido_maestro FROM Grupos WHERE id_grupo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(SQL)) {
            ps.setInt(1, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nombre_maestro") + " " + rs.getString("Apellido_maestro");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener el nombre del maestro: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
        return "";
    }

    private List<AlumnoReporte> ObtenerDatosParaReporte(Connection conexion, int idGrupo, String[] meses) {
        List<AlumnoReporte> alumnosData = new ArrayList<>();
        
        // **MODIFICADO**: Condición actualizada a >= 7
        boolean esSuperior = (idGrupo >= 7);
        String nombreTabla;
        List<String> columnas = new ArrayList<>();
        columnas.add("mes");
        columnas.add("espanol");
        columnas.add("ingles");
        columnas.add("artes");
        columnas.add("matematicas");
        columnas.add("tecnologia");
        
        if (esSuperior) {
            nombreTabla = "calificaciones_superior";
            columnas.add("ciencias_naturales");
            columnas.add("geografia");
            columnas.add("historia");
        } else {
            nombreTabla = "calificaciones_inferior";
            columnas.add("conocimiento_del_medio");
        }
        
        columnas.add("civica_y_etica");
        columnas.add("educacion_fisica");
        columnas.add("promedio");

        String sqlAlumnos = "SELECT id_alumno, nombre, apellido, genero FROM alumnos WHERE id_grupo = ? ORDER BY apellido, nombre;";
        
        try (PreparedStatement psAlumnos = conexion.prepareStatement(sqlAlumnos)) {
            psAlumnos.setInt(1, idGrupo);
            ResultSet rsAlumnos = psAlumnos.executeQuery();
            
            int numeroLista = 1;
            while (rsAlumnos.next()) {
                int idAlumno = rsAlumnos.getInt("id_alumno");
                String nombreCompleto = rsAlumnos.getString("apellido") + " " + rsAlumnos.getString("nombre");
                String sexo = rsAlumnos.getString("genero").substring(0, 1);

                String sqlCalificaciones = "SELECT " + String.join(", ", columnas) + " FROM " + nombreTabla + " WHERE id_alumno = ? AND UPPER(mes) IN (?, ?, ?);";
                
                Map<String, String[]> calificacionesDelAlumno = new java.util.HashMap<>();
                try (PreparedStatement psCalificaciones = conexion.prepareStatement(sqlCalificaciones)) {
                    psCalificaciones.setInt(1, idAlumno);
                    psCalificaciones.setString(2, meses[0].toUpperCase());
                    psCalificaciones.setString(3, meses[1].toUpperCase());
                    psCalificaciones.setString(4, meses[2].toUpperCase());
                    
                    ResultSet rsCalificaciones = psCalificaciones.executeQuery();
                    while(rsCalificaciones.next()) {
                        String mes = rsCalificaciones.getString("mes").toUpperCase();
                        String[] notas;
                        
                        if (esSuperior) {
                            notas = new String[11]; // 10 materias + promedio
                            notas[0] = rsCalificaciones.getString("espanol");
                            notas[1] = rsCalificaciones.getString("ingles");
                            notas[2] = rsCalificaciones.getString("artes");
                            notas[3] = rsCalificaciones.getString("matematicas");
                            notas[4] = rsCalificaciones.getString("tecnologia");
                            notas[5] = rsCalificaciones.getString("ciencias_naturales");
                            notas[6] = rsCalificaciones.getString("geografia");
                            notas[7] = rsCalificaciones.getString("historia");
                            notas[8] = rsCalificaciones.getString("civica_y_etica");
                            notas[9] = rsCalificaciones.getString("educacion_fisica");
                            notas[10] = rsCalificaciones.getString("promedio");
                        } else {
                            notas = new String[9]; // 8 materias + promedio
                            notas[0] = rsCalificaciones.getString("espanol");
                            notas[1] = rsCalificaciones.getString("ingles");
                            notas[2] = rsCalificaciones.getString("artes");
                            notas[3] = rsCalificaciones.getString("matematicas");
                            notas[4] = rsCalificaciones.getString("tecnologia");
                            notas[5] = rsCalificaciones.getString("conocimiento_del_medio");
                            notas[6] = rsCalificaciones.getString("civica_y_etica");
                            notas[7] = rsCalificaciones.getString("educacion_fisica");
                            notas[8] = rsCalificaciones.getString("promedio");
                        }
                        calificacionesDelAlumno.put(mes, notas);
                    }
                }
                
                alumnosData.add(new AlumnoReporte(numeroLista++, sexo, nombreCompleto, calificacionesDelAlumno));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar los datos del reporte: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
        return alumnosData;
    }

    // --- MÉTODOS DE CREACIÓN DE TABLAS PDF ---

    private Table crearEncabezadoSuperior(String nombreGrupo, String nombreMaestro, String[] meses) {
        float[] anchos = {20f, 20f, 20f, 20f, 20f};
        Table table = new Table(anchos);
        table.useAllAvailableWidth();
        
        table.addCell(crearCelda("GRADO Y GRUPO: " + nombreGrupo, 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MAESTRO(A): " + nombreMaestro, 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));

        table.addCell(crearCelda("", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: " + meses[0], 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: " + meses[1], 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: " + meses[2], 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("TRIMESTRE _________ 2025", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));

        return table;
    }

    private Table crearTablaCalificaciones(List<AlumnoReporte> listaAlumnos, String[] mesesDelTrimestre, int idGrupo) {
        boolean esSuperior = (idGrupo >= 7);
        int numMaterias = esSuperior ? 10 : 8;
        int numColsMensual = numMaterias + 1;
        int totalDataCols = (3 * numColsMensual) + numMaterias + 2;
        int totalCols = 3 + totalDataCols;

        float[] anchos = new float[totalCols];
        anchos[0] = 3f; anchos[1] = 2.5f; anchos[2] = 15f;
        for (int i = 3; i < anchos.length; i++) anchos[i] = 1.8f;

        Table table = new Table(anchos);
        table.useAllAvailableWidth();

        String[] materiasEncabezado;
        if (esSuperior) {
            materiasEncabezado = new String[]{"\nESPAÑOL", "\nINGLÉS", "\nARTES", "MATEMÁTICAS", "TECNOLOGÍA", "CIENCIASNAT.", "\nGEOGRAFÍA", "\nHISTORIA", "\nÉTICA", "\nED.FÍSICA"};
        } else {
            materiasEncabezado = new String[]{"\nESPAÑOL", "\nINGLÉS", "\nARTES", "MATEMÁTICAS", "TECNOLOGIA", "CON.MEDIO", "\n\nÉTICA", "\nED.FISICA"};
        }

        // --- ENCABEZADOS ---
        table.addCell(crearCeldaSpan("No. LISTA", 2, 1, 6f));
        table.addCell(crearCeldaSpan("SEXO", 2, 1, 6f));
        table.addCell(crearCeldaSpan("NOMBRE DEL ALUMNO", 2, 1, 6f));

        for (int i = 0; i < 4; i++) { // 3 mensuales + 1 trimestral
            table.addCell(crearCeldaSpan("LENGUAJES", 1, 3, 6f)
                .setRotationAngle(Math.PI / 2)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

            table.addCell(crearCeldaSpan("SABERES Y PENS-MAT", 1, 3, 6f)
                .setRotationAngle(Math.PI / 2)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

            table.addCell(crearCeldaSpan("ETICA, NAT Y SOC.", 1, esSuperior ? 3 : 1, 6f)
                .setRotationAngle(Math.PI / 2)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

            table.addCell(crearCeldaSpan("HUMANO Y COM.", 1, 1, 6f)
                .setRotationAngle(Math.PI / 2)
                .setVerticalAlignment(VerticalAlignment.MIDDLE));

            if (i < 3) table.addCell(new Cell());
        }

        
        table.addCell(crearCeldaSpan("\n\n\n\nPROMEDIO", 2, 1, 6f)
            .setRotationAngle(Math.PI / 2)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));
        

        table.addCell(crearCeldaSpan("\n\nPROMEDIO TRIMESTRAL", 2, 1, 6f)
            .setRotationAngle(Math.PI / 2)
            .setVerticalAlignment(VerticalAlignment.MIDDLE));

        for (int i = 0; i < 4; i++) {
            table.addCell(crearCeldaConColor(materiasEncabezado[0], 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaConColor(materiasEncabezado[1], 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaConColor(materiasEncabezado[2], 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaConColor(materiasEncabezado[3], 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaConColor(materiasEncabezado[4], 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaConColor(materiasEncabezado[5], 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            if (esSuperior) {
                table.addCell(crearCeldaConColor(materiasEncabezado[6], 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
                table.addCell(crearCeldaConColor(materiasEncabezado[7], 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
                table.addCell(crearCeldaConColor(materiasEncabezado[8], 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
                table.addCell(crearCeldaConColor(materiasEncabezado[9], 5f, VERDE_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            } else {
                table.addCell(crearCeldaConColor(materiasEncabezado[6], 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
                table.addCell(crearCeldaConColor(materiasEncabezado[7], 5f, VERDE_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            }
            if (i < 3) table.addCell(crearCeldaConColor("\nPROMEDIO", 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
        }

        // --- FILAS DE DATOS ---
        for (AlumnoReporte alumno : listaAlumnos) {
            table.addCell(crearCelda(String.valueOf(alumno.numeroLista), 8f));
            table.addCell(crearCelda(alumno.sexo, 8f));
            table.addCell(crearCelda(alumno.nombreCompleto, 8f).setTextAlignment(TextAlignment.LEFT));

            for (String mes : mesesDelTrimestre) {
                String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                for (int j = 0; j < numMaterias; j++) {
                    table.addCell(crearCelda(notas != null ? notas[j] : "", 6f));
                }
                int promIndex = esSuperior ? 10 : 8;
                table.addCell(crearCelda(notas != null ? notas[promIndex] : "", 6f));
            }

            for (int j = 0; j < numMaterias; j++) {
                double sumaMateria = 0;
                int mesesConNota = 0;
                for (String mes : mesesDelTrimestre) {
                    String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                    if (notas != null && notas[j] != null) {
                        sumaMateria += safeParseDouble(notas[j]);
                        mesesConNota++;
                    }
                }
                double promedioMateria = (mesesConNota > 0) ? sumaMateria / mesesConNota : 0;
                table.addCell(crearCelda(String.format(java.util.Locale.US, "%.1f", promedioMateria), 6f));
            }

            double promedioFinal1 = calcularPromedioFinal(alumno, mesesDelTrimestre, numMaterias);
            double promedioFinal2 = calcularPromedioDePromedios(alumno, mesesDelTrimestre, esSuperior);
            table.addCell(crearCeldaConColor(String.format(java.util.Locale.US, "%.1f", promedioFinal1), 6f, AMARILLO_CLARO));
            table.addCell(crearCeldaConColor(String.format(java.util.Locale.US, "%.1f", promedioFinal2), 6f, AMARILLO_CLARO));
        }

        // --- FILA FINAL DE PROMEDIO DEL GRUPO ---
        table.addCell(crearCeldaSpan("PROMEDIO", 1, 3, 8f));
        for (int col = 0; col < totalDataCols; col++) {
             table.addCell(crearCelda("", 8f));
        }

        return table;
    }

    // --- MÉTODOS AUXILIARES Y DE BD ---

    private double safeParseDouble(String numero) {
        if (numero == null || numero.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(numero);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private double calcularPromedioFinal(AlumnoReporte alumno, String[] mesesDelTrimestre, int numMaterias) {
        double sumaPromediosMaterias = 0;
        int materiasConPromedio = 0;
        for (int i = 0; i < numMaterias; i++) {
            double sumaMateria = 0;
            int mesesConNota = 0;
            for (String mes : mesesDelTrimestre) {
                String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                if (notas != null && i < notas.length && notas[i] != null) {
                    sumaMateria += safeParseDouble(notas[i]);
                    mesesConNota++;
                }
            }
            if (mesesConNota > 0) {
                sumaPromediosMaterias += (sumaMateria / mesesConNota);
                materiasConPromedio++;
            }
        }
        return (materiasConPromedio > 0) ? sumaPromediosMaterias / materiasConPromedio : 0;
    }

    private double calcularPromedioDePromedios(AlumnoReporte alumno, String[] mesesDelTrimestre, boolean esSuperior) {
        double sumaPromedios = 0;
        int countPromedios = 0;
        int promIndex = esSuperior ? 10 : 8;
        for (String mes : mesesDelTrimestre) {
            String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
            if (notas != null) {
                double prom = safeParseDouble(notas[promIndex]);
                if (prom > 0) {
                    sumaPromedios += prom;
                    countPromedios++;
                }
            }
        }
        return (countPromedios > 0) ? sumaPromedios / countPromedios : 0;
    }
    
    // =================================================================
    // CÓDIGO MODIFICADO
    // =================================================================
    private Cell crearCelda(String texto, float fontSize) {
        String contenido = (texto != null) ? texto : "";
        Paragraph paragraph = new Paragraph(contenido).setFontSize(fontSize);

        // Condición: aplicar leading solo si el texto tiene más de una palabra.
        if (contenido.trim().contains(" ")) {
            paragraph.setMultipliedLeading(1.2f); // Agrega un 20% de espacio extra entre líneas.
        }

        return new Cell().add(paragraph)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(2f);
    }

    private Cell crearCeldaConColor(String texto, float fontSize, Color bgColor) {
        return crearCelda(texto, fontSize).setBackgroundColor(bgColor);
    }

    // =================================================================
    // CÓDIGO MODIFICADO
    // =================================================================
    private Cell crearCeldaSpan(String texto, int rowspan, int colspan, float fontSize) {
        String contenido = (texto != null) ? texto : "";
        Paragraph paragraph = new Paragraph(contenido).setFontSize(fontSize);

        // Condición: aplicar leading solo si el texto tiene más de una palabra.
        if (contenido.trim().contains(" ")) {
            paragraph.setMultipliedLeading(1.2f); // Agrega un 20% de espacio extra entre líneas.
        }

        return new Cell(rowspan, colspan).add(paragraph)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(2f);
    }
    
    public int CambiarGrupoPorID(Connection conexionExistente, String grupo) {
        if (grupo != null && !grupo.trim().isEmpty()) {
            int cambio = 0;
            String SQL = "SELECT id_grupo FROM grupos WHERE grupo = ?;";
            try (PreparedStatement ps = conexionExistente.prepareStatement(SQL)) {
                ps.setString(1, grupo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cambio = rs.getInt("id_grupo");
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error al buscar ID de grupo: " + ex.toString());
            }
            return cambio;
        }
        return 0;
    }
}