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

    /**
     * Método público principal que genera el reporte.
     * @param conexionExistente Conexión a la base de datos.
     * @param paramGrupo Componente JTextField para el nombre del grupo.
     * @param paramPrimerMes Componente JTextField para el primer mes.
     * @param paramSegundoMes Componente JTextField para el segundo mes.
     * @param paramTercerMes Componente JTextField para el tercer mes.
     */
    public void generarReporte(Connection conexionExistente, JTextField paramGrupo, JTextField paramPrimerMes, JTextField paramSegundoMes, JTextField paramTercerMes) {
        // 1. Extraer el texto de los componentes JTextField
        String nombreGrupo = paramGrupo.getText();
        String[] mesesDelTrimestre = {
            paramPrimerMes.getText(),
            paramSegundoMes.getText(),
            paramTercerMes.getText()
        };

        // 2. Validar que los campos no estén vacíos
        if (nombreGrupo.trim().isEmpty() || mesesDelTrimestre[0].trim().isEmpty() || 
            mesesDelTrimestre[1].trim().isEmpty() || mesesDelTrimestre[2].trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos los campos (Grupo y los tres meses) son obligatorios.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. El resto del flujo continúa
        int idGrupo = CambiarGrupoPorID(conexionExistente, nombreGrupo);
        if (idGrupo == 0) {
            JOptionPane.showMessageDialog(null, "El grupo '" + nombreGrupo + "' no fue encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<AlumnoReporte> listaAlumnos = ObtenerDatosParaReporte(conexionExistente, idGrupo, mesesDelTrimestre);
        if (listaAlumnos == null || listaAlumnos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron alumnos o calificaciones para el grupo y meses seleccionados.", "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String ruta = "Reportes/Reporte_Grupo_" + nombreGrupo + ".pdf";
            PdfWriter writer = new PdfWriter(ruta);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20);

            document.add(crearEncabezadoSuperior(nombreGrupo, mesesDelTrimestre));
            document.add(crearTablaCalificaciones(listaAlumnos, mesesDelTrimestre, idGrupo));

            document.close();
            JOptionPane.showMessageDialog(null, "Reporte de calificaciones generado en: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error al generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // --- METODOS DE OBTENCION DE DATOS ---

    private List<AlumnoReporte> ObtenerDatosParaReporte(Connection conexion, int idGrupo, String[] meses) {
        List<AlumnoReporte> alumnosData = new ArrayList<>();
        
        String nombreTabla = (idGrupo >= 5) ? "calificaciones_superior" : "calificaciones_inferior";
        String colCiencias = (idGrupo >= 5) ? "ciencias_naturales" : "conocimiento_del_medio";

        String sqlAlumnos = "SELECT id_alumno, nombre, apellido, genero FROM alumnos WHERE id_grupo = ? ORDER BY apellido, nombre;";
        
        try (PreparedStatement psAlumnos = conexion.prepareStatement(sqlAlumnos)) {
            psAlumnos.setInt(1, idGrupo);
            ResultSet rsAlumnos = psAlumnos.executeQuery();
            
            int numeroLista = 1;
            while (rsAlumnos.next()) {
                int idAlumno = rsAlumnos.getInt("id_alumno");
                String nombreCompleto = rsAlumnos.getString("apellido") + " " + rsAlumnos.getString("nombre");
                String sexo = rsAlumnos.getString("genero").substring(0, 1);

                String sqlCalificaciones = String.format(
                    "SELECT mes, espanol, ingles, artes, matematicas, tecnologia, %s, civica_y_etica, educacion_fisica, promedio " +
                    "FROM %s WHERE id_alumno = ? AND UPPER(mes) IN (?, ?, ?)", colCiencias, nombreTabla
                );

                Map<String, String[]> calificacionesDelAlumno = new java.util.HashMap<>();
                try (PreparedStatement psCalificaciones = conexion.prepareStatement(sqlCalificaciones)) {
                    psCalificaciones.setInt(1, idAlumno);
                    psCalificaciones.setString(2, meses[0].toUpperCase());
                    psCalificaciones.setString(3, meses[1].toUpperCase());
                    psCalificaciones.setString(4, meses[2].toUpperCase());
                    
                    ResultSet rsCalificaciones = psCalificaciones.executeQuery();
                    while(rsCalificaciones.next()) {
                        String mes = rsCalificaciones.getString("mes").toUpperCase();
                        String[] notas = new String[9];
                        notas[0] = rsCalificaciones.getString("espanol");
                        notas[1] = rsCalificaciones.getString("ingles");
                        notas[2] = rsCalificaciones.getString("artes");
                        notas[3] = rsCalificaciones.getString("matematicas");
                        notas[4] = rsCalificaciones.getString("tecnologia");
                        notas[5] = rsCalificaciones.getString(colCiencias);
                        notas[6] = rsCalificaciones.getString("civica_y_etica");
                        notas[7] = rsCalificaciones.getString("educacion_fisica");
                        notas[8] = rsCalificaciones.getString("promedio");
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

    private Table crearEncabezadoSuperior(String nombreGrupo, String[] meses) {
        float[] anchos = {25f, 25f, 25f, 25f};
        Table table = new Table(anchos);
        table.useAllAvailableWidth();
        table.addCell(crearCelda("GRADO Y GRUPO: " + nombreGrupo, 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: " + meses[0], 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: " + meses[1], 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: " + meses[2], 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("", 8f).setBorder(null));
        table.addCell(crearCelda("", 8f).setBorder(null));
        table.addCell(crearCelda("", 8f).setBorder(null));
        Cell trimestreCell = crearCelda("TRIMESTRE _________ 2025", 8f)
                .setTextAlignment(TextAlignment.RIGHT).setBorder(null)
                .setBorderBottom(new com.itextpdf.layout.border.SolidBorder(Color.BLACK, 1));
        table.addCell(trimestreCell);
        return table;
    }

    private Table crearTablaCalificaciones(List<AlumnoReporte> listaAlumnos, String[] mesesDelTrimestre, int idGrupo) {
    // --- Definición de anchos y creación de la tabla (sin cambios) ---
    float[] anchos = new float[40];
    anchos[0] = 3f; anchos[1] = 4f; anchos[2] = 15f;
    for (int i = 3; i < anchos.length; i++) anchos[i] = 2.1f;

    Table table = new Table(anchos);
    table.useAllAvailableWidth();
    
    String materiaCiencias = (idGrupo >= 5) ? "CIENCIAS NAT." : "CON. DEL MEDIO";
    String[] materiasEncabezado = {"ESPAÑOL", "INGLES", "ARTES", "MATEMATICAS", "TECNOLOGIA", materiaCiencias, "F. CIVICA Y ETICA", "ED. FISICA"};

    // --- Creación de encabezados (sin cambios) ---
    table.addCell(crearCeldaSpan("No. LISTA", 2, 1, 6f));
    table.addCell(crearCeldaSpan("SEXO", 2, 1, 6f));
    table.addCell(crearCeldaSpan("NOMBRE DEL ALUMNO", 2, 1, 6f));
    for (int i = 0; i < 3; i++) {
        table.addCell(crearCeldaSpan("\nLENGUAJES", 1, 3, 6f).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaSpan("\nSABERES Y PENS-MAT", 1, 3, 6f).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaSpan("ETICA, NAT Y SOC.", 1, 1, 6f).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaSpan("HUMANO Y COM.", 1, 1, 6f).setRotationAngle(Math.PI / 2));
        table.addCell(new Cell());
    }
    table.addCell(crearCeldaSpan("\nLENGUAJES", 1, 3, 6f).setRotationAngle(Math.PI / 2));
    table.addCell(crearCeldaSpan("\nSABERES Y PENS-MAT", 1, 3, 6f).setRotationAngle(Math.PI / 2));
    table.addCell(crearCeldaSpan("ETICA, NAT Y SOC.", 1, 1, 6f).setRotationAngle(Math.PI / 2));
    table.addCell(crearCeldaSpan("HUMANO Y COM.", 1, 1, 6f).setRotationAngle(Math.PI / 2));
    table.addCell(crearCeldaSpan("\n\n\nPROMEDIO", 2, 1, 6f).setRotationAngle(Math.PI / 2));
    table.addCell(crearCeldaSpan("\nPROMEDIO TRIMESTRAL", 2, 1, 6f).setRotationAngle(Math.PI / 2));
    for (int i = 0; i < 3; i++) {
        table.addCell(crearCeldaConColor("\n"+materiasEncabezado[0], 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor("\n"+materiasEncabezado[1], 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor("\n"+materiasEncabezado[2], 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor(materiasEncabezado[3], 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor("\n"+materiasEncabezado[4], 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor(materiasEncabezado[5], 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor(materiasEncabezado[6], 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor("\n"+materiasEncabezado[7], 5f, VERDE_CLARO).setRotationAngle(Math.PI / 2));
        table.addCell(crearCeldaConColor("\nPROMEDIO", 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2));
    }
    for (int i=0; i < materiasEncabezado.length; i++) {
         table.addCell(crearCeldaConColor("\n"+materiasEncabezado[i], 5f, i < 3 ? AZUL_CLARO : i < 6 ? ROSA_CLARO : i < 7 ? AMARILLO_CLARO : VERDE_CLARO).setRotationAngle(Math.PI / 2));
    }

    // --- Filas de datos de cada alumno ---
    for (AlumnoReporte alumno : listaAlumnos) {
        table.addCell(crearCelda(String.valueOf(alumno.numeroLista), 8f));
        table.addCell(crearCelda(alumno.sexo, 8f));
        table.addCell(crearCelda(alumno.nombreCompleto, 8f).setTextAlignment(TextAlignment.LEFT));
        
        double[] promediosMensuales = new double[3];
        
        for (int i = 0; i < mesesDelTrimestre.length; i++) {
            String mes = mesesDelTrimestre[i].toUpperCase();
            String[] notas = alumno.calificacionesPorMes.get(mes);
            if (notas != null) {
                for (int j = 0; j < 8; j++) {
                    table.addCell(crearCelda(notas[j], 6f));
                }
                table.addCell(crearCelda(notas[8], 6f));
                promediosMensuales[i] = safeParseDouble(notas[8]);
            } else {
                for (int j = 0; j < 9; j++) table.addCell(crearCelda("", 6f));
            }
        }

        double sumaPromediosTrimestrales = 0;
        int materiasConPromedio = 0;
        for (int i = 0; i < 8; i++) {
            double sumaMateria = 0;
            int mesesConNota = 0;
            for (String mes : mesesDelTrimestre) {
                String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                if (notas != null) {
                    sumaMateria += safeParseDouble(notas[i]);
                    mesesConNota++;
                }
            }
            double promedioMateria = (mesesConNota > 0) ? sumaMateria / mesesConNota : 0;
            // CORRECCIÓN: Se añade Locale.US para forzar el punto decimal
            table.addCell(crearCelda(String.format(java.util.Locale.US, "%.1f", promedioMateria), 6f));
            if(promedioMateria > 0) {
                sumaPromediosTrimestrales += promedioMateria;
                materiasConPromedio++;
            }
        }
        
        double promedioFinal1 = (materiasConPromedio > 0) ? sumaPromediosTrimestrales / materiasConPromedio : 0;
        
        double sumaPromedios = 0;
        int countPromedios = 0;
        for(double prom : promediosMensuales) {
            if(prom > 0) {
               sumaPromedios += prom;
               countPromedios++;
            }
        }
        double promedioFinal2 = (countPromedios > 0) ? sumaPromedios / countPromedios : 0;

        // CORRECCIÓN: Se añade Locale.US para forzar el punto decimal
        table.addCell(crearCeldaConColor(String.format(java.util.Locale.US, "%.1f", promedioFinal1), 6f, AMARILLO_CLARO));
        table.addCell(crearCeldaConColor(String.format(java.util.Locale.US, "%.1f", promedioFinal2), 6f, AMARILLO_CLARO));
    }

    // --- Fila final de promedio del grupo ---
    table.addCell(crearCeldaSpan("PROMEDIO", 1, 3, 8f));

    for (int col = 0; col < 37; col++) {
        double sumaColumna = 0;
        int alumnosConNota = 0;

        for (AlumnoReporte alumno : listaAlumnos) {
            double valorCelda = 0;
            
            if (col < 27) { 
                int mesIndex = col / 9;
                int materiaIndex = col % 9;
                String[] notas = alumno.calificacionesPorMes.get(mesesDelTrimestre[mesIndex].toUpperCase());
                if (notas != null) {
                    valorCelda = safeParseDouble(notas[materiaIndex]);
                }
            } else if (col < 35) {
                int materiaIndex = col - 27;
                double sumaMateriaTrimestre = 0;
                int mesesConNota = 0;
                for (String mes : mesesDelTrimestre) {
                    String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                    if (notas != null) {
                        sumaMateriaTrimestre += safeParseDouble(notas[materiaIndex]);
                        mesesConNota++;
                    }
                }
                if (mesesConNota > 0) valorCelda = sumaMateriaTrimestre / mesesConNota;
            }

            if (valorCelda > 0) {
                sumaColumna += valorCelda;
                alumnosConNota++;
            }
        }

        double promedioColumna = (alumnosConNota > 0) ? sumaColumna / alumnosConNota : 0;
        // CORRECCIÓN: Se añade Locale.US para forzar el punto decimal
        table.addCell(crearCelda(promedioColumna > 0 ? String.format(java.util.Locale.US, "%.1f", promedioColumna) : "", 8f));
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
    
    private Cell crearCelda(String texto, float fontSize) {
        return new Cell().add(new Paragraph(texto).setFontSize(fontSize)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(2f);
    }

    private Cell crearCeldaConColor(String texto, float fontSize, Color bgColor) {
        return crearCelda(texto, fontSize).setBackgroundColor(bgColor);
    }

    private Cell crearCeldaSpan(String texto, int rowspan, int colspan, float fontSize) {
        return new Cell(rowspan, colspan).add(new Paragraph(texto).setFontSize(fontSize)).setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE).setPadding(2f);
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