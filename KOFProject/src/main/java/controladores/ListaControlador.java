/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
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
                            notas = new String[11];
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
                            notas = new String[9];
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
        float[] anchoColumna = {72f, 14f, 14f};
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();

        try {
            String path = "src/main/resources/11.png";
            ImageData imageData = ImageDataFactory.create(path);
            Image image = new Image(imageData);
            image.setAutoScale(true);

            Cell imageCell = new Cell(4, 1).add(image);
            imageCell.setBorder(null);
            table.addCell(imageCell);

        } catch (MalformedURLException ex) {
            System.out.println("Error: No se encontró la imagen en la ruta especificada. " + ex.getMessage());
            table.addCell(new Cell(4, 1).setBorder(null));
        }

        table.addCell(crearCelda("NOMBRE DE LA ESCUELA:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 8f).setBorder(null));
        table.addCell(crearCelda("PRIMARIA ARCOIRIS", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 8f).setBorder(null));
        table.addCell(crearCelda("CICLO ESCOLAR:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 8f).setBorder(null));
        table.addCell(crearCelda("2025-2026", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 8f).setBorder(null));
        table.addCell(crearCelda("GRADO Y GRUPO:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 8f).setBorder(null));
        table.addCell(crearCelda(nombreGrupo, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 8f).setBorder(null));
        table.addCell(crearCelda("MAESTRO(A):", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 8f).setBorder(null));
        table.addCell(crearCelda(nombreMaestro, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 8f).setBorder(null));

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

        // Arrays para calcular los promedios de grupo por columna
        double[] sumasColumnas = new double[totalDataCols];
        int[] conteosColumnas = new int[totalDataCols];

        String[] materiasEncabezado;
        if (esSuperior) {
            materiasEncabezado = new String[]{"\nESPAÑOL", "\nINGLÉS", "\nARTES", "MATEMÁTICAS", "TECNOLOGÍA", "CIENCIASNAT.", "\nGEOGRAFÍA", "\nHISTORIA", "\nÉTICA", "\nED.FÍSICA"};
        } else {
            materiasEncabezado = new String[]{"\nESPAÑOL", "\nINGLÉS", "\nARTES", "MATEMÁTICAS", "TECNOLOGÍA", "CON.MEDIO", "\n\nÉTICA", "\nED.FÍSICA"};
        }

        table.addCell(new Cell().setBorder(null));
        table.addCell(new Cell().setBorder(null));
        table.addCell(new Cell().setBorder(null));
        table.addCell(crearCeldaSpan(mesesDelTrimestre[0].toUpperCase(), 1, numColsMensual, 8f));
        table.addCell(crearCeldaSpan(mesesDelTrimestre[1].toUpperCase(), 1, numColsMensual, 8f));
        table.addCell(crearCeldaSpan(mesesDelTrimestre[2].toUpperCase(), 1, numColsMensual, 8f));
        table.addCell(crearCeldaSpan("PROMEDIO TRIMESTRAL", 1, numMaterias + 2, 8f));
        
        table.addCell(crearCeldaSpan("No. LISTA", 2, 1, 6f));
        table.addCell(crearCeldaSpan("SEXO", 2, 1, 6f));
        table.addCell(crearCeldaSpan("NOMBRE DEL ALUMNO", 2, 1, 6f));

        for (int i = 0; i < 4; i++) {
            table.addCell(crearCeldaSpan("LENGUAJES", 1, 3, 6f).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaSpan("SABERES Y PENS-MAT", 1, 3, 6f).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaSpan("\nETICA, NAT Y SOC.", 1, esSuperior ? 3 : 1, 6f).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            table.addCell(crearCeldaSpan("HUMANO Y COM.", 1, 1, 6f).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            if (i < 3) {
                table.addCell(new Cell());
            }
        }
        
        table.addCell(crearCeldaSpan("\n\n\n\nPROMEDIO", 2, 1, 6f).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
        table.addCell(crearCeldaSpan("\n\nPROMEDIO TRIMESTRAL", 2, 1, 6f).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));

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
            if (i < 3) {
                table.addCell(crearCeldaConColor("\nPROMEDIO", 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setVerticalAlignment(VerticalAlignment.MIDDLE));
            }
        }

        // --- FILAS DE DATOS ---
        for (AlumnoReporte alumno : listaAlumnos) {
            table.addCell(crearCelda(String.valueOf(alumno.numeroLista), 8f));
            table.addCell(crearCelda(alumno.sexo, 8f));
            table.addCell(crearCelda(alumno.nombreCompleto, 8f).setTextAlignment(TextAlignment.LEFT));

            int columnaActual = 0;

            for (String mes : mesesDelTrimestre) {
                String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                for (int j = 0; j < numMaterias; j++) {
                    String notaStr = (notas != null && j < notas.length) ? notas[j] : "";
                    double nota = safeParseDouble(notaStr);
                    table.addCell(crearCelda(formatearCalificacion(nota), 6f));
                    if (nota > 0) {
                        sumasColumnas[columnaActual] += nota;
                        conteosColumnas[columnaActual]++;
                    }
                    columnaActual++;
                }
                int promIndex = esSuperior ? 10 : 8;
                String promStr = (notas != null && promIndex < notas.length) ? notas[promIndex] : "";
                double prom = safeParseDouble(promStr);
                table.addCell(crearCeldaConColor(formatearCalificacion(prom), 6f, AMARILLO_CLARO));
                if (prom > 0) {
                    sumasColumnas[columnaActual] += prom;
                    conteosColumnas[columnaActual]++;
                }
                columnaActual++;
            }

            for (int j = 0; j < numMaterias; j++) {
                double sumaMateria = 0;
                int mesesConNota = 0;
                for (String mes : mesesDelTrimestre) {
                    String[] notas = alumno.calificacionesPorMes.get(mes.toUpperCase());
                    if (notas != null && j < notas.length && notas[j] != null) {
                        sumaMateria += safeParseDouble(notas[j]);
                        mesesConNota++;
                    }
                }
                double promedioMateria = (mesesConNota > 0) ? sumaMateria / mesesConNota : 0;
                table.addCell(crearCelda(formatearCalificacion(promedioMateria), 6f));
                if (promedioMateria > 0) {
                    sumasColumnas[columnaActual] += promedioMateria;
                    conteosColumnas[columnaActual]++;
                }
                columnaActual++;
            }

            double promedioFinal1 = calcularPromedioFinal(alumno, mesesDelTrimestre, numMaterias);
            double promedioFinal2 = calcularPromedioDePromedios(alumno, mesesDelTrimestre, esSuperior);
            table.addCell(crearCeldaConColor(formatearCalificacion(promedioFinal1), 6f, AMARILLO_CLARO));
            if (promedioFinal1 > 0) {
                sumasColumnas[columnaActual] += promedioFinal1;
                conteosColumnas[columnaActual]++;
            }
            columnaActual++;
            
            table.addCell(crearCeldaConColor(formatearCalificacion(promedioFinal2), 6f, AMARILLO_CLARO));
             if (promedioFinal2 > 0) {
                sumasColumnas[columnaActual] += promedioFinal2;
                conteosColumnas[columnaActual]++;
            }
        }

        // --- FILA FINAL DE PROMEDIO DEL GRUPO ---
        table.addCell(crearCeldaSpan("PROMEDIO", 1, 3, 8f));
        for (int i = 0; i < totalDataCols; i++) {
            double promedioColumna = (conteosColumnas[i] > 0) ? sumasColumnas[i] / conteosColumnas[i] : 0;
            table.addCell(crearCeldaConColor(formatearCalificacion(promedioColumna), 8f, AMARILLO_CLARO));
        }

        return table;
    }

    // --- MÉTODOS AUXILIARES Y DE BD ---

    private Cell crearCelda(String contenido, TextAlignment hAlign, VerticalAlignment vAlign, boolean isBold, Color bgColor, float fontSize) {
        Paragraph p = new Paragraph(contenido != null ? contenido : "").setFontSize(fontSize);
        if (isBold) {
            p.setBold();
        }
        Cell cell = new Cell().add(p);
        cell.setTextAlignment(hAlign);
        cell.setVerticalAlignment(vAlign);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        return cell;
    }

    private double safeParseDouble(String numero) {
        if (numero == null || numero.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(numero);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String formatearCalificacion(double calificacion) {
        if (calificacion <= 0) {
            return "";
        }
        if (calificacion == 10.0) {
            return "10";
        }
        return String.format(java.util.Locale.US, "%.1f", calificacion);
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
            if (notas != null && promIndex < notas.length) {
                double prom = safeParseDouble(notas[promIndex]);
                if (prom > 0) {
                    sumaPromedios += prom;
                    countPromedios++;
                }
            }
        }
        return (countPromedios > 0) ? sumaPromedios / countPromedios : 0;
    }
    
    private Cell crearCelda(String texto, float fontSize) {
        String contenido = (texto != null) ? texto : "";
        Paragraph paragraph = new Paragraph(contenido).setFontSize(fontSize);

        if (contenido.trim().contains(" ")) {
            paragraph.setMultipliedLeading(1.2f);
        }

        return new Cell().add(paragraph)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(2f);
    }

    private Cell crearCeldaConColor(String texto, float fontSize, Color bgColor) {
        return crearCelda(texto, fontSize).setBackgroundColor(bgColor);
    }

    private Cell crearCeldaSpan(String texto, int rowspan, int colspan, float fontSize) {
        String contenido = (texto != null) ? texto : "";
        Paragraph paragraph = new Paragraph(contenido).setFontSize(fontSize);

        if (contenido.trim().contains(" ")) {
            paragraph.setMultipliedLeading(1.2f);
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