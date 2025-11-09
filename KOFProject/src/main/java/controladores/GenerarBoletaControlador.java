/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Image;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.Map;

/**
 *
 * @author Ivan De la Rosa
 */
public class GenerarBoletaControlador {

    public void GenerarBoleta(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido) throws FileNotFoundException {
        
        String[] datosIdentificacion = ObtenerDatosIdentificacion(conexionExistente, paramCURP, paramNombre, paramApellido);
        
        if (datosIdentificacion == null) {
            JOptionPane.showMessageDialog(null, "No se pudo generar la boleta porque no se encontraron los datos del alumno.");
            return;
        }
        
        String nombreAlumno = datosIdentificacion[0];
        String grupoAlumno = datosIdentificacion[1];
        int idGrupo = Integer.parseInt(datosIdentificacion[2]);
        int idAlumno = Integer.parseInt(datosIdentificacion[3]);
        
        Map<String, String[]> todasLasCalificaciones = ExtraerCalificacion(conexionExistente, idAlumno, idGrupo);

        String[] trimestre1Meses = {"SEPTIEMBRE", "OCTUBRE", "NOV/DIC"};
        String[] trimestre2Meses = {"ENERO", "FEBRERO", "MARZO"};
        String[] trimestre3Meses = {"ABRIL", "MAYO", "JUNIO"};
        
        String ruta = "Reportes/boleta_" + nombreAlumno.replace(" ", "_") + ".pdf";
        PdfWriter MiPdfWriter = new PdfWriter(ruta);
        PdfDocument MiPdfDocument = new PdfDocument(MiPdfWriter);
        MiPdfDocument.setDefaultPageSize(PageSize.A4.rotate());
        Document MiDocument = new Document(MiPdfDocument);

        MiDocument.add(crearTablaIdentificacion(nombreAlumno, grupoAlumno));
        MiDocument.add(new Paragraph("\n"));

        MiDocument.add(crearTablaCalificaciones(todasLasCalificaciones, idGrupo, trimestre1Meses, trimestre2Meses, trimestre3Meses));
        MiDocument.add(new Paragraph("\n"));
        MiDocument.add(crearTablaInasistencias(todasLasCalificaciones, trimestre1Meses, trimestre2Meses, trimestre3Meses));
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
        if (isBold) p.setBold();
        Cell cell = new Cell().add(p).setTextAlignment(hAlign).setVerticalAlignment(vAlign).setPadding(3f);
        if (bgColor != null) cell.setBackgroundColor(bgColor);
        return cell;
    }

    private Cell crearCeldaSpan(String contenido, int rowspan, int colspan, TextAlignment hAlign, VerticalAlignment vAlign, boolean isBold, Color bgColor, float fontSize) {
        Paragraph p = new Paragraph(contenido).setFontSize(fontSize);
        p.setTextAlignment(hAlign);
        if (isBold) p.setBold();
        Cell cell = new Cell(rowspan, colspan).add(p).setTextAlignment(hAlign).setVerticalAlignment(vAlign).setPadding(3f);
        if (bgColor != null) cell.setBackgroundColor(bgColor);
        return cell;
    }

    // --- MÉTODOS DE CREACIÓN DE TABLAS (PDF) ---

    private Table crearTablaIdentificacion(String nombreAlumno, String grupoAlumno) {
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

        table.addCell(crearCelda("NOMBRE DE LA ESCUELA:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f).setBorder(null));
        table.addCell(crearCelda("PRIMARIA ARCOIRIS", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        table.addCell(crearCelda("CICLO ESCOLAR:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f).setBorder(null));
        table.addCell(crearCelda("2025-2026", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        table.addCell(crearCelda("GRADO Y GRUPO:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f).setBorder(null));
        
        //table.addCell(crearCelda(grupoAlumno, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        
        if(grupoAlumno.equals("1A"))
        {
            table.addCell(crearCelda("Primero A", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("1B"))
        {
            table.addCell(crearCelda("Primero B", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("2A"))
        {
            table.addCell(crearCelda("Segundo A", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("2B"))
        {
            table.addCell(crearCelda("Segundo B", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("3A"))
        {
            table.addCell(crearCelda("Tercero A", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("3B"))
        {
            table.addCell(crearCelda("Tercero B", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("4A"))
        {
            table.addCell(crearCelda("Cuarto A", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("4B"))
        {
            table.addCell(crearCelda("Cuarto B", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("5A"))
        {
            table.addCell(crearCelda("Quinto A", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("5B"))
        {
            table.addCell(crearCelda("Quinto B", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("6A"))
        {
            table.addCell(crearCelda("Sexto A", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        else if (grupoAlumno.equals("6B"))
        {
            table.addCell(crearCelda("Sexto B", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));
        }
        
        table.addCell(crearCelda("ALUMNO (A):", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f).setBorder(null));
        table.addCell(crearCelda(nombreAlumno, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f).setBorder(null));

        return table;
    }

    private Table crearTablaCalificaciones(Map<String, String[]> todasLasCalificaciones, int idGrupo, String[] trimestre1Meses, String[] trimestre2Meses, String[] trimestre3Meses) {
        if (todasLasCalificaciones == null) {
            todasLasCalificaciones = new java.util.HashMap<>();
        }

        boolean esSuperior = (idGrupo >= 7);
        String[] materias;
        if (esSuperior) {
            materias = new String[] { "ESPAÑOL", "INGLÉS", "ARTES", "MATEMÁTICAS", "TECNOLOGÍA", "CIENCIA NATURALES", "GEOGRAFÍA", "HISTORIA", "CÍVICA Y ÉTICA", "ED. FISICA" };
        } else {
            materias = new String[] { "ESPAÑOL", "INGLÉS", "ARTES", "MATEMÁTICAS", "TECNOLOGÍA", "CON. DEL MEDIO", "CÍVICA Y ÉTICA", "ED. FISICA" };
        }

        String[] mesesColumnasPDF = {"DIAGNOSTICO", "SEPTIEMBRE", "OCTUBRE", "NOV/DIC", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO"};

        float[] anchosContenedor = {75f, 25f};
        Table tablaContenedora = new Table(anchosContenedor);
        tablaContenedora.useAllAvailableWidth();

        float[] anchosPrincipal = {20f, 20f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f};
        Table tablaPrincipal = new Table(anchosPrincipal);
        tablaPrincipal.useAllAvailableWidth();
        float alturaFila = 20f;

        tablaPrincipal.addCell(crearCeldaSpan("CAMPOS DE FORMACIÓN ACADÉMICA", 2, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPrincipal.addCell(crearCeldaSpan("CALIFICACIONES MENSUALES", 1, 10, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));

        for (String mes : new String[]{"DIAGNO", "SEPTIEMBRE", "OCTUBRE", "NOV/DIC", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO"}) {
            Color colorDeCelda;
            switch (mes) {
                case "DIAGNO":
                    colorDeCelda = GRIS_CLARO;
                    break;
                case "SEPTIEMBRE":
                case "OCTUBRE":
                case "NOV/DIC":
                    colorDeCelda = NARANJA_CLARO;
                    break;
                case "ENERO":
                case "FEBRERO":
                case "MARZO":
                    colorDeCelda = AMARILLO_CLARO;
                    break;
                case "ABRIL":
                case "MAYO":
                case "JUNIO":
                    colorDeCelda = VERDE_CLARO;
                    break;
                default:
                    colorDeCelda = null; // Sin color por defecto
                    break;
            }
            tablaPrincipal.addCell(crearCelda(mes, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, colorDeCelda, 8f));
        }
        // =================================================================

        // --- LENGUAJES ---
        tablaPrincipal.addCell(crearCeldaSpan("LENGUAJES", 3, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f));
        for (int i = 0; i < 3; i++) {
            tablaPrincipal.addCell(crearCelda(materias[i], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f).setHeight(alturaFila));
            for (String mesActual : mesesColumnasPDF) {
                String[] notasDelMes = todasLasCalificaciones.get(mesActual);
                double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[i]) : 0;
                tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
        }

        // --- SABERES Y PENS. CIENTÍFICO ---
        tablaPrincipal.addCell(crearCeldaSpan("SABERES Y PENS. CIENTÍFICO", 3, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f));
        for (int i = 3; i < 6; i++) {
            tablaPrincipal.addCell(crearCelda(materias[i], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f).setHeight(alturaFila));
            for (String mesActual : mesesColumnasPDF) {
                String[] notasDelMes = todasLasCalificaciones.get(mesActual);
                double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[i]) : 0;
                tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
        }

        // --- ÉTICA, NATURALEZA Y SOC. ---
        int rowspanEtica = esSuperior ? 3 : 1; 
        tablaPrincipal.addCell(crearCeldaSpan("ÉTICA, NATURALEZA Y SOC.", rowspanEtica, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 8f));
        if (esSuperior) {
            tablaPrincipal.addCell(crearCelda(materias[6], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f).setHeight(alturaFila));
            for (String mesActual : mesesColumnasPDF) {
                String[] notasDelMes = todasLasCalificaciones.get(mesActual);
                double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[6]) : 0;
                tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
            tablaPrincipal.addCell(crearCelda(materias[7], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f).setHeight(alturaFila));
            for (String mesActual : mesesColumnasPDF) {
                String[] notasDelMes = todasLasCalificaciones.get(mesActual);
                double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[7]) : 0;
                tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
        }
        int civicaIndex = esSuperior ? 8 : 6;
        tablaPrincipal.addCell(crearCelda(materias[civicaIndex], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f).setHeight(alturaFila));
        for (String mesActual : mesesColumnasPDF) {
            String[] notasDelMes = todasLasCalificaciones.get(mesActual);
            double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[8]) : 0;
            tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        }

        // --- HUMANO Y COMUNITARIO ---
        tablaPrincipal.addCell(crearCeldaSpan("HUMANO Y COMUNITARIO", 1, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_CLARO, 9f));
        int edFisicaIndex = esSuperior ? 9 : 7;
        tablaPrincipal.addCell(crearCelda(materias[edFisicaIndex], TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_CLARO, 9f).setHeight(alturaFila));
        for (String mesActual : mesesColumnasPDF) {
            String[] notasDelMes = todasLasCalificaciones.get(mesActual);
            double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[9]) : 0;
            tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        }

        // --- PROMEDIO MENSUAL ---
        tablaPrincipal.addCell(crearCeldaSpan("PROM. MENSUAL", 1, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f).setHeight(alturaFila));
        for (String mesActual : mesesColumnasPDF) {
            String[] notasDelMes = todasLasCalificaciones.get(mesActual);
            double cal = (notasDelMes != null) ? safeParseDouble(notasDelMes[11]) : 0;
            tablaPrincipal.addCell(crearCelda(formatearCalificacion(cal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f).setHeight(alturaFila));
        }

        // --- TABLA DE PERIODOS (DERECHA) ---
        float[] anchosPeriodos = {25f, 25f, 25f, 25f};
        Table tablaPeriodos = new Table(anchosPeriodos);
        tablaPeriodos.useAllAvailableWidth();
        tablaPeriodos.addCell(crearCeldaSpan("PERIODOS", 1, 4, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPeriodos.addCell(crearCelda("1er/TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 7f));
        tablaPeriodos.addCell(crearCelda("2º/TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 7f));
        tablaPeriodos.addCell(crearCelda("3er/TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_CLARO, 7f));
        tablaPeriodos.addCell(crearCelda("FINAL", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 7f));

        List<Integer> indicesParaPromediar = new ArrayList<>();
        if (esSuperior) {
            for (int i = 0; i <= 9; i++) indicesParaPromediar.add(i);
        } else {
            for (int i = 0; i <= 5; i++) indicesParaPromediar.add(i);
            indicesParaPromediar.add(8);
            indicesParaPromediar.add(9);
        }
        indicesParaPromediar.add(11);

        for (int dataIndex : indicesParaPromediar) {
            double prom1 = calcularPromedioTrimestral(trimestre1Meses, todasLasCalificaciones, dataIndex);
            double prom2 = calcularPromedioTrimestral(trimestre2Meses, todasLasCalificaciones, dataIndex);
            double prom3 = calcularPromedioTrimestral(trimestre3Meses, todasLasCalificaciones, dataIndex);

            double promFinal = 0;
            int trimestresConNota = 0;
            if (prom1 > 0) { promFinal += prom1; trimestresConNota++; }
            if (prom2 > 0) { promFinal += prom2; trimestresConNota++; }
            if (prom3 > 0) { promFinal += prom3; trimestresConNota++; }
            if (trimestresConNota > 0) promFinal /= trimestresConNota;

            tablaPeriodos.addCell(crearCelda(formatearCalificacion(prom1), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            tablaPeriodos.addCell(crearCelda(formatearCalificacion(prom2), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            tablaPeriodos.addCell(crearCelda(formatearCalificacion(prom3), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            tablaPeriodos.addCell(crearCelda(formatearCalificacion(promFinal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f).setHeight(alturaFila));
        }

        tablaContenedora.addCell(new Cell().add(tablaPrincipal).setBorder(null).setPadding(1f));
        tablaContenedora.addCell(new Cell().add(tablaPeriodos).setBorder(null).setPadding(1f));

        return tablaContenedora;
    }

    private Table crearTablaInasistencias(Map<String, String[]> todasLasCalificaciones, String[] trimestre1Meses, String[] trimestre2Meses, String[] trimestre3Meses) {
        float[] anchosContenedorInasistencias = {75f, 25f}; 
        Table tablaContenedora = new Table(anchosContenedorInasistencias);
        tablaContenedora.useAllAvailableWidth();

        float[] anchoColumna = {20f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f};
        Table tablaInasistencias = new Table(anchoColumna);
        tablaInasistencias.useAllAvailableWidth();

        // Celda inicial vacía
        tablaInasistencias.addCell(crearCeldaSpan("", 1, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));

        // Mes de Diagnóstico
        tablaInasistencias.addCell(crearCelda("D", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, GRIS_CLARO, 9f));

        // Meses del Trimestre 1 (Naranja)
        for (String mes : new String[]{"S", "O", "N/D"}) {
            float fontSize = mes.equals("N/D") ? 7f : 9f;
            tablaInasistencias.addCell(crearCelda(mes, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, fontSize));
        }

        // Meses del Trimestre 2 (Amarillo)
        for (String mes : new String[]{"E", "F", "M"}) {
            tablaInasistencias.addCell(crearCelda(mes, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        }

        // Meses del Trimestre 3 (Verde)
        for (String mes : new String[]{"A", "M", "J"}) {
            tablaInasistencias.addCell(crearCelda(mes, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_CLARO, 9f));
        }
        // =================================================================

        // Fila de Inasistencias
        tablaInasistencias.addCell(crearCelda("INASISTENCIAS", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        float alturaFilaInasistencias = 20f;
        for (int i = 0; i < 10; i++) {
            tablaInasistencias.addCell(crearCelda("0", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
        }

        // Tabla de Calificaciones (derecha)
        float[] anchosCalificaciones = {25f, 25f, 25f, 25f};
        Table tablaCalificaciones = new Table(anchosCalificaciones);
        tablaCalificaciones.useAllAvailableWidth();

        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("PF", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_PALIDO, 9f));

        int dataIndexPromedio = 11;
        double prom1 = calcularPromedioTrimestral(trimestre1Meses, todasLasCalificaciones, dataIndexPromedio);
        double prom2 = calcularPromedioTrimestral(trimestre2Meses, todasLasCalificaciones, dataIndexPromedio);
        double prom3 = calcularPromedioTrimestral(trimestre3Meses, todasLasCalificaciones, dataIndexPromedio);

        double promFinal = 0;
        int trimestresConNota = 0;
        if (prom1 > 0) { promFinal += prom1; trimestresConNota++; }
        if (prom2 > 0) { promFinal += prom2; trimestresConNota++; }
        if (prom3 > 0) { promFinal += prom3; trimestresConNota++; }
        if (trimestresConNota > 0) promFinal /= trimestresConNota;

        tablaCalificaciones.addCell(crearCelda(formatearCalificacion(prom1), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
        tablaCalificaciones.addCell(crearCelda(formatearCalificacion(prom2), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
        tablaCalificaciones.addCell(crearCelda(formatearCalificacion(prom3), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
        tablaCalificaciones.addCell(crearCelda(formatearCalificacion(promFinal), TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));

        // Ensamblaje final
        tablaContenedora.addCell(new Cell().add(tablaInasistencias).setBorder(null).setPadding(1f));
        tablaContenedora.addCell(new Cell().add(tablaCalificaciones).setBorder(null).setPadding(1f));

        return tablaContenedora;
    }

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
    
    // --- MÉTODOS DE EXTRACCIÓN Y FORMATO DE DATOS ---
    
    /**
     * Convierte de forma segura un String a double. Devuelve 0.0 si el String es nulo,
     * vacío o no es un número válido.
     */
    private double safeParseDouble(String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(numero);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Formatea una calificación numérica: 10 como entero, el resto con un decimal y punto.
     */
    private String formatearCalificacion(double calificacion) {
        if (calificacion <= 0) {
            return "";
        }
        if (calificacion == 10.0) {
            return "10";
        }
        return String.format(java.util.Locale.US, "%.1f", calificacion);
    }

    private String[] ObtenerDatosIdentificacion(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido) {
        int idAlumno = EncontrarAlumno(conexionExistente, paramCURP, paramNombre, paramApellido);
        if (idAlumno == 0) return null;
        String SQL = "SELECT a.nombre, a.apellido, g.grupo, a.id_grupo, a.id_alumno " +
                     "FROM alumnos a JOIN grupos g ON a.id_grupo = g.id_grupo WHERE a.id_alumno = ?;";
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
            JOptionPane.showMessageDialog(null, "Error al obtener los datos de identificación: " + ex.toString());
        }
        return null;
    }
    
    public int EncontrarAlumno(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido) {
        String curp = paramCURP.getText().trim();
        String nombre = paramNombre.getText().trim();
        String apellido = paramApellido.getText().trim();
    
        String SQL = "SELECT id_alumno FROM alumnos WHERE curp = ? OR (nombre = ? AND apellido = ?);";
        
        try (PreparedStatement ps = conexionExistente.prepareStatement(SQL)) {
            ps.setString(1, curp);
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_alumno");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error al buscar alumno: " + ex.toString());
        }
        
        return 0;
    }
    
    public Map<String, String[]> ExtraerCalificacion(Connection conexionExistente, int idAlumno, int idGrupo) {
        if (idAlumno == 0) return null;

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
        columnas.add("inasistencias");
        columnas.add("promedio");

        String SQL = "SELECT " + String.join(", ", columnas) + " FROM " + nombreTabla + " WHERE id_alumno = ?;";
        Map<String, String[]> mapDeCalificaciones = new java.util.HashMap<>();

        try (PreparedStatement ps = conexionExistente.prepareStatement(SQL)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mesActual = rs.getString("mes").toUpperCase().replace(" ", "");
                    String[] datosDeEsteMes = new String[12];
                    
                    datosDeEsteMes[0] = rs.getString("espanol");
                    datosDeEsteMes[1] = rs.getString("ingles");
                    datosDeEsteMes[2] = rs.getString("artes");
                    datosDeEsteMes[3] = rs.getString("matematicas");
                    datosDeEsteMes[4] = rs.getString("tecnologia");
                    
                    if (esSuperior) {
                        datosDeEsteMes[5] = rs.getString("ciencias_naturales");
                        datosDeEsteMes[6] = rs.getString("geografia");
                        datosDeEsteMes[7] = rs.getString("historia");
                    } else {
                        datosDeEsteMes[5] = rs.getString("conocimiento_del_medio");
                        datosDeEsteMes[6] = null;
                        datosDeEsteMes[7] = null;
                    }
                    
                    datosDeEsteMes[8] = rs.getString("civica_y_etica");
                    datosDeEsteMes[9] = rs.getString("educacion_fisica");
                    datosDeEsteMes[10] = rs.getString("inasistencias");
                    datosDeEsteMes[11] = rs.getString("promedio");
                    
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
            if (calificaciones != null && calificaciones.containsKey(mes)) {
                String[] notasDelMes = calificaciones.get(mes);
                if (notasDelMes != null && dataIndex < notasDelMes.length && notasDelMes[dataIndex] != null) {
                    double nota = safeParseDouble(notasDelMes[dataIndex]);
                    // Solo se promedian calificaciones válidas (mayores a 0)
                    if (nota > 0) {
                        suma += nota;
                        contador++;
                    }
                }
            }
        }
        return contador > 0 ? suma / contador : 0.0;
    }
}