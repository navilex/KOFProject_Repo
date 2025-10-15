/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.Map;

/**
 *
 * @author Ivan De la Rosa
 */
public class GenerarBoletaControlador {

    public void GenerarBoleta(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido) throws FileNotFoundException {
        
        // PASO 1: Obtener los datos de identificación del alumno ANTES de crear el PDF.
        String[] datosIdentificacion = ObtenerDatosIdentificacion(conexionExistente, paramCURP, paramNombre, paramApellido);
        
        // Si no se encontraron los datos del alumno, detenemos la generación de la boleta.
        if (datosIdentificacion == null) {
            JOptionPane.showMessageDialog(null, "No se pudo generar la boleta porque no se encontraron los datos del alumno.");
            return;
        }
        
        // Desempaquetamos los 4 datos obtenidos
        String nombreAlumno = datosIdentificacion[0];
        String grupoAlumno = datosIdentificacion[1];
        int idGrupo = Integer.parseInt(datosIdentificacion[2]);
        int idAlumno = Integer.parseInt(datosIdentificacion[3]);
        
        // --- Creación del Documento PDF ---
        String ruta = "Reportes/boleta_" + nombreAlumno.replace(" ", "_") + ".pdf"; // Nombre de archivo dinámico
        PdfWriter MiPdfWriter = new PdfWriter(ruta);
        PdfDocument MiPdfDocument = new PdfDocument(MiPdfWriter);
        MiPdfDocument.setDefaultPageSize(PageSize.A4.rotate());
        Document MiDocument = new Document(MiPdfDocument);

        // PASO 2: Pasar los datos obtenidos a la Tabla 1 de Identificación.
        MiDocument.add(crearTablaIdentificacion(nombreAlumno, grupoAlumno));
        MiDocument.add(new Paragraph("\n"));

        // El resto del proceso continúa...
        // Pasamos el idAlumno y idGrupo para que el método decida qué hacer
        MiDocument.add(crearTablaCalificaciones(conexionExistente, idAlumno, idGrupo));
        MiDocument.add(new Paragraph("\n"));
        MiDocument.add(crearTablaInasistencias()); // Este método también necesitaría una lógica similar si las inasistencias se guardan por separado
        MiDocument.add(new Paragraph("\n"));
        MiDocument.add(new Paragraph("\n"));
        MiDocument.add(crearTablaNivelesDesempeno());
        MiDocument.add(new Paragraph("\n"));
        MiDocument.add(crearTablaFirmasPadres());

        MiDocument.close();
        JOptionPane.showMessageDialog(null, "Boleta generada exitosamente en: " + ruta);
    }

    // --- MÉTODOS AUXILIARES DE DISEÑO (iTextPDF) ---

    private static final Color AZUL_CLARO = new DeviceRgb(214, 233, 248);
    private static final Color ROSA_CLARO = new DeviceRgb(245, 222, 239);
    private static final Color AMARILLO_CLARO = new DeviceRgb(252, 243, 215);
    private static final Color VERDE_CLARO = new DeviceRgb(213, 232, 213);
    private static final Color GRIS_CLARO = new DeviceRgb(220, 220, 220);
    private static final Color NARANJA_CLARO = new DeviceRgb(255, 229, 204);
    private static final Color VERDE_AGUA_CLARO = new DeviceRgb(204, 255, 204);
    private static final Color AZUL_PALIDO = new DeviceRgb(204, 229, 255);

    private Cell crearCelda(String contenido, TextAlignment hAlign, VerticalAlignment vAlign, boolean isBold, Color bgColor, float fontSize) {
        Paragraph p = new Paragraph(contenido).setFontSize(fontSize);
        p.setTextAlignment(hAlign);
        if (isBold) {
            p.setBold();
        }
        Cell cell = new Cell().add(p)
                .setTextAlignment(hAlign)
                .setVerticalAlignment(vAlign)
                .setPadding(3f);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        return cell;
    }

    private Cell crearCeldaSpan(String contenido, int rowspan, int colspan, TextAlignment hAlign, VerticalAlignment vAlign, boolean isBold, Color bgColor, float fontSize) {
        Paragraph p = new Paragraph(contenido).setFontSize(fontSize);
        p.setTextAlignment(hAlign);
        if (isBold) {
            p.setBold();
        }
        Cell cell = new Cell(rowspan, colspan).add(p)
                .setTextAlignment(hAlign)
                .setVerticalAlignment(vAlign)
                .setPadding(3f);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        return cell;
    }

    // --- MÉTODOS DE CREACIÓN DE TABLAS (PDF) ---
    //TABLA 1

    private Table crearTablaIdentificacion(String nombreAlumno, String grupoAlumno) {
        float[] anchoColumna = {50f, 50f};
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();

        table.addCell(crearCelda("NOMBRE DE LA ESCUELA:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda("PRIMARIA ARCOIRIS", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));
        table.addCell(crearCelda("GRADO Y GRUPO:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda(grupoAlumno, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));
        table.addCell(crearCelda("ALUMNO (A):", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda(nombreAlumno, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));

        return table;
    }

    //TABLA 2
    private Table crearTablaCalificaciones(Connection conexionExistente, int idAlumno, int idGrupo) {
        Map<String, String[]> todasLasCalificaciones = ExtraerCalificacion(conexionExistente, idAlumno, idGrupo);

        if (todasLasCalificaciones == null) {
            JOptionPane.showMessageDialog(null, "No se encontraron calificaciones para el alumno seleccionado. Se generará una boleta vacía.");
            todasLasCalificaciones = new java.util.HashMap<>();
        }

        // El nombre de la materia de ciencias ahora es dinámico
        String materiaCiencias = (idGrupo >= 5) ? "CIENCIASNATURALES" : "CON. DEL MEDIO";
        
        String[] mesesColumnasPDF = {"DIAGNOSTICO", "SEPTIEMBRE", "OCTUBRE", "NOV/DIC", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO"};
        String[] materias = {
            "ESPAÑOL", "INGLES", "ARTES", "MATEMATICAS", "TECNOLOGIA", 
            materiaCiencias, // <-- Aquí se usa la variable
            "FORM. CIVICA Y ETICA", "ED. FISICA"
        };

        String[] trimestre1Meses = {"SEPTIEMBRE", "OCTUBRE", "NOV/DIC"};
        String[] trimestre2Meses = {"ENERO", "FEBRERO", "MARZO"};
        String[] trimestre3Meses = {"ABRIL", "MAYO", "JUNIO"};
        
        float[] anchosContenedor = {75f, 25f};
        Table tablaContenedora = new Table(anchosContenedor);
        tablaContenedora.useAllAvailableWidth();

        float[] anchosPrincipal = {20f, 20f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f};
        Table tablaPrincipal = new Table(anchosPrincipal);
        tablaPrincipal.useAllAvailableWidth();
        float alturaFila = 20f;
        
        tablaPrincipal.addCell(crearCeldaSpan("CAMPOS DE FORMACIÓN ACADÉMICA", 2, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPrincipal.addCell(crearCeldaSpan("CALIFICACIONES MENSUALES", 1, 10, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPrincipal.addCell(crearCelda("DIAGNO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("SEPTIEMBRE", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("OCTUBRE", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("NOV/DIC", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("ENERO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("FEBRERO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("MARZO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("ABRIL", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("MAYO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPrincipal.addCell(crearCelda("JUNIO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));

        tablaPrincipal.addCell(crearCeldaSpan("PENS. Y LENGUAJES", 3, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f));
        for (int i = 0; i < 3; i++) {
            tablaPrincipal.addCell(crearCelda(materias[i], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f).setHeight(alturaFila));
            for (String mesActual : mesesColumnasPDF) {
                String[] notasDelMes = todasLasCalificaciones.get(mesActual);
                tablaPrincipal.addCell(crearCelda(notasDelMes != null ? notasDelMes[i] : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
        }
        
        tablaPrincipal.addCell(crearCeldaSpan("SABERES Y CIENTIFICO", 3, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f));
        for (int i = 3; i < 6; i++) {
            tablaPrincipal.addCell(crearCelda(materias[i], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f).setHeight(alturaFila));
            for (String mesActual : mesesColumnasPDF) {
                String[] notasDelMes = todasLasCalificaciones.get(mesActual);
                tablaPrincipal.addCell(crearCelda(notasDelMes != null ? notasDelMes[i] : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
        }
        
        tablaPrincipal.addCell(crearCeldaSpan("DE LA ÉTICA, HUMANO Y COMUNITARIO", 2, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 8f));
        tablaPrincipal.addCell(crearCelda(materias[6], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f).setHeight(alturaFila));
        for (String mesActual : mesesColumnasPDF) {
            String[] notasDelMes = todasLasCalificaciones.get(mesActual);
            tablaPrincipal.addCell(crearCelda(notasDelMes != null ? notasDelMes[6] : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        }
        tablaPrincipal.addCell(crearCelda(materias[7], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_CLARO, 9f).setHeight(alturaFila));
        for (String mesActual : mesesColumnasPDF) {
            String[] notasDelMes = todasLasCalificaciones.get(mesActual);
            tablaPrincipal.addCell(crearCelda(notasDelMes != null ? notasDelMes[7] : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        }
        
        tablaPrincipal.addCell(crearCeldaSpan("PROM. MENSUAL", 1, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f).setHeight(alturaFila));
        for (String mesActual : mesesColumnasPDF) {
            String[] notasDelMes = todasLasCalificaciones.get(mesActual);
            tablaPrincipal.addCell(crearCelda(notasDelMes != null ? notasDelMes[9] : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f).setHeight(alturaFila));
        }
        
        float[] anchosPeriodos = {25f, 25f, 25f, 25f};
        Table tablaPeriodos = new Table(anchosPeriodos);
        tablaPeriodos.useAllAvailableWidth();

        tablaPeriodos.addCell(crearCeldaSpan("PERIODOS", 1, 4, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPeriodos.addCell(crearCelda("1er/TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 7f));
        tablaPeriodos.addCell(crearCelda("2º/TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 7f));
        tablaPeriodos.addCell(crearCelda("3er/TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 7f));
        tablaPeriodos.addCell(crearCelda("FINAL", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 7f));

        for (int i = 0; i < 9; i++) {
            int dataIndex = (i < 8) ? i : 9;
            
            double prom1 = calcularPromedioTrimestral(trimestre1Meses, todasLasCalificaciones, dataIndex);
            double prom2 = calcularPromedioTrimestral(trimestre2Meses, todasLasCalificaciones, dataIndex);
            double prom3 = calcularPromedioTrimestral(trimestre3Meses, todasLasCalificaciones, dataIndex);
            
            double promFinal = 0;
            int trimestresConNota = 0;
            if (prom1 > 0) { promFinal += prom1; trimestresConNota++; }
            if (prom2 > 0) { promFinal += prom2; trimestresConNota++; }
            if (prom3 > 0) { promFinal += prom3; trimestresConNota++; }
            if (trimestresConNota > 0) {
                promFinal /= trimestresConNota;
            }

            tablaPeriodos.addCell(crearCelda(prom1 > 0 ? String.format("%.1f", prom1) : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            tablaPeriodos.addCell(crearCelda(prom2 > 0 ? String.format("%.1f", prom2) : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            tablaPeriodos.addCell(crearCelda(prom3 > 0 ? String.format("%.1f", prom3) : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            tablaPeriodos.addCell(crearCelda(promFinal > 0 ? String.format("%.1f", promFinal) : "", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f).setHeight(alturaFila));
        }

        tablaContenedora.addCell(new Cell().add(tablaPrincipal).setBorder(null).setPadding(1f));
        tablaContenedora.addCell(new Cell().add(tablaPeriodos).setBorder(null).setPadding(1f));

        return tablaContenedora;
    }

    //TABLA 3
    private Table crearTablaInasistencias() {
        // Esta tabla también debería ser dinámica si las inasistencias se guardan en la base de datos.
        // Por ahora, se mantiene estática como en el código original.
        float[] anchosContenedorInasistencias = {75f, 25f}; 
        Table tablaContenedora = new Table(anchosContenedorInasistencias);
        tablaContenedora.useAllAvailableWidth();

        float[] anchoColumna = {20f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f};
        Table tablaInasistencias = new Table(anchoColumna);
        tablaInasistencias.useAllAvailableWidth();
        
        tablaInasistencias.addCell(crearCeldaSpan("", 1, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaInasistencias.addCell(crearCelda("D", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, GRIS_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("S", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("O", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("N\nD", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 7f));
        tablaInasistencias.addCell(crearCelda("E", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("F", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("M", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("A", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("M", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("J", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));

        tablaInasistencias.addCell(crearCelda("INASISTENCIAS", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        float alturaFilaInasistencias = 20f;
        for (int i = 0; i < 10; i++) {
            tablaInasistencias.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
        }
        
        float[] anchosCalificaciones = {25f, 25f, 25f, 25f};
        Table tablaCalificaciones = new Table(anchosCalificaciones);
        tablaCalificaciones.useAllAvailableWidth();

        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("PF", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_PALIDO, 9f));
        
        for (int i = 0; i < 1; i++) {
            for (int k = 0; k < 4; k++) {
                tablaCalificaciones.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
            }
        }
        
        tablaContenedora.addCell(new Cell().add(tablaInasistencias).setBorder(null).setPadding(1f));
        tablaContenedora.addCell(new Cell().add(tablaCalificaciones).setBorder(null).setPadding(1f));

        return tablaContenedora;
    }

    //TABLA 4
    private Table crearTablaNivelesDesempeno() {
        float[] anchoColumna = {65f, 35f}; 
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();

        table.addCell(crearCeldaSpan("NIVELES DE DESEMPEÑO", 1, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda("NIVEL I = EQUIVALE A 5", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        table.addCell(crearCelda("El estudiante tiene carencias fundamentales en valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f));
        table.addCell(crearCelda("NIVEL II = EQUIVALE A 6 Y 7", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        table.addCell(crearCelda("El estudiante tiene dificultades para demostrar valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f));
        table.addCell(crearCelda("NIVEL III = EQUIVALE A 8 Y 9", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        table.addCell(crearCelda("El estudiante ha demostrado los valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f));
        table.addCell(crearCelda("NIVEL IV = EQUIVALE A 10", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        table.addCell(crearCelda("El estudiante ha demostrado los valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f));

        return table;
    }
    
    //TABLA 5
    private Table crearTablaFirmasPadres() {
        float[] anchosContenedor = {50f, 50f};
        Table tablaPrincipal = new Table(anchosContenedor);
        tablaPrincipal.useAllAvailableWidth();
        tablaPrincipal.setBorder(null);

        float[] anchosPanel = {30f, 70f}; 
        Table panelIzquierdo = new Table(anchosPanel);
        panelIzquierdo.useAllAvailableWidth();

        panelIzquierdo.addCell(crearCelda("MES", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        panelIzquierdo.addCell(crearCelda("FIRMA DEL PADRE O TUTOR", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));

        String[] mesesIzquierdos = {"AGOSTO DIAGNOSTICO", "SEPTIEMBRE", "OCTUBRE", "NOV/ DIC", "ENERO"};
        for (String mes : mesesIzquierdos) {
            panelIzquierdo.addCell(crearCelda(mes, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
            panelIzquierdo.addCell(crearCelda("_______________________________________", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
        }

        Table panelDerecho = new Table(anchosPanel);
        panelDerecho.useAllAvailableWidth();

        panelDerecho.addCell(crearCelda("MES", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        panelDerecho.addCell(crearCelda("FIRMA DEL PADRE O TUTOR", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));

        String[] mesesDerechos = {"FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO"};
        for (String mes : mesesDerechos) {
            panelDerecho.addCell(crearCelda(mes, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
            panelDerecho.addCell(crearCelda("_______________________________________", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
        }

        tablaPrincipal.addCell(new Cell().add(panelIzquierdo).setBorder(null));
        tablaPrincipal.addCell(new Cell().add(panelDerecho).setBorder(null));

        return tablaPrincipal;
    }
    
    // --- MÉTODOS DE EXTRACCIÓN DE DATOS (BD) ---
    
    private String[] ObtenerDatosIdentificacion(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido) {
        int idAlumno = EncontrarAlumno(conexionExistente, paramCURP, paramNombre, paramApellido);

        if (idAlumno == 0) {
            return null;
        }

        String SQL = "SELECT a.nombre, a.apellido, g.grupo, a.id_grupo, a.id_alumno " +
                       "FROM alumnos a " +
                       "JOIN grupos g ON a.id_grupo = g.id_grupo " +
                       "WHERE a.id_alumno = ?;";
        
        try (PreparedStatement ps = conexionExistente.prepareStatement(SQL)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido");
                    String nombreGrupo = rs.getString("grupo");
                    String idGrupo = rs.getString("id_grupo");
                    String idAlumnoStr = rs.getString("id_alumno");
                    return new String[]{nombreCompleto, nombreGrupo, idGrupo, idAlumnoStr};
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al obtener los datos de identificación del alumno: " + ex.toString());
        }
        
        return null;
    }
    
    public int EncontrarAlumno(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido) {
        int id_deseado = 0;
        String SQL = "SELECT id_alumno, curp, nombre, apellido FROM alumnos;";
        
        try (Statement st = conexionExistente.createStatement();
             ResultSet rs = st.executeQuery(SQL)) {
            
            while (rs.next()) {
                String curpDb = rs.getString("curp");
                String nombreDb = rs.getString("nombre");
                String apellidoDb = rs.getString("apellido");

                if (paramCURP.getText() != null && !paramCURP.getText().isEmpty() && paramCURP.getText().equals(curpDb)) {
                    id_deseado = rs.getInt("id_alumno");
                    return id_deseado;
                } else if (paramNombre.getText() != null && paramApellido.getText() != null &&
                           !paramNombre.getText().isEmpty() && !paramApellido.getText().isEmpty() &&
                           paramNombre.getText().equals(nombreDb) && paramApellido.getText().equals(apellidoDb)) {
                    id_deseado = rs.getInt("id_alumno");
                    return id_deseado;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al buscar alumno: " + ex.toString());
        }
        
        return 0;
    }
    
    public Map<String, String[]> ExtraerCalificacion(Connection conexionExistente, int idAlumno, int idGrupo) {
        if (idAlumno == 0) {
            return null;
        }

        String nombreTabla;
        String nombreColumnaCiencias;

        if (idGrupo >= 5) {
            nombreTabla = "calificaciones_superior";
            nombreColumnaCiencias = "ciencias_naturales";
        } else {
            nombreTabla = "calificaciones_inferior";
            nombreColumnaCiencias = "conocimiento_del_medio";
        }

        String SQL = "SELECT mes, espanol, ingles, artes, matematicas, tecnologia, " +
                       nombreColumnaCiencias + ", civica_y_etica, educacion_fisica, " +
                       "inasistencias, promedio FROM " + nombreTabla + " WHERE id_alumno = ?;";

        Map<String, String[]> mapDeCalificaciones = new java.util.HashMap<>();

        try (PreparedStatement ps = conexionExistente.prepareStatement(SQL)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mesActual = rs.getString("mes").toUpperCase().replace(" ", "");
                    String[] datosDeEsteMes = new String[10]; 
                    datosDeEsteMes[0] = rs.getString("espanol");
                    datosDeEsteMes[1] = rs.getString("ingles");
                    datosDeEsteMes[2] = rs.getString("artes");
                    datosDeEsteMes[3] = rs.getString("matematicas");
                    datosDeEsteMes[4] = rs.getString("tecnologia");
                    datosDeEsteMes[5] = rs.getString(nombreColumnaCiencias);
                    datosDeEsteMes[6] = rs.getString("civica_y_etica");
                    datosDeEsteMes[7] = rs.getString("educacion_fisica");
                    datosDeEsteMes[8] = rs.getString("inasistencias");
                    datosDeEsteMes[9] = rs.getString("promedio");
                    mapDeCalificaciones.put(mesActual, datosDeEsteMes);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al extraer el historial de calificaciones: " + ex.toString());
        }
        
        return mapDeCalificaciones;
    }
    
    private double calcularPromedioTrimestral(String[] meses, Map<String, String[]> calificaciones, int dataIndex) {
        double suma = 0;
        int contador = 0;
        for (String mes : meses) {
            if (calificaciones.containsKey(mes)) {
                String[] notasDelMes = calificaciones.get(mes);
                try {
                    suma += Double.parseDouble(notasDelMes[dataIndex]);
                    contador++;
                } catch (NumberFormatException | NullPointerException e) {
                    // Si la nota es nula o no es un número válido, no se cuenta.
                }
            }
        }
        return contador > 0 ? suma / contador : 0.0;
    }
}