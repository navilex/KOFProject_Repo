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
import javax.swing.JOptionPane;

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
     * Método público principal que genera el reporte de calificaciones mensuales.
     */
    public void generarReporte() {
        try {
            String ruta = "Reportes/ReporteCalificacionesMensuales.pdf";
            PdfWriter writer = new PdfWriter(ruta);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate()); // Hoja horizontal
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20); // Márgenes reducidos

            // --- 1. Encabezado superior (GRADO Y GRUPO, MES, etc.) ---
            document.add(crearEncabezadoSuperior());

            // --- 2. Tabla principal de calificaciones ---
            document.add(crearTablaCalificaciones());

            document.close();
            JOptionPane.showMessageDialog(null, "Reporte de calificaciones generado en: " + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error al generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODOS AUXILIARES PARA CREAR CELDAS ---
    private Cell crearCelda(String texto, float fontSize) {
        return new Cell().add(new Paragraph(texto).setFontSize(fontSize))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(2f);
    }

    private Cell crearCeldaConColor(String texto, float fontSize, Color bgColor) {
        return crearCelda(texto, fontSize).setBackgroundColor(bgColor);
    }

    private Cell crearCeldaSpan(String texto, int rowspan, int colspan, float fontSize) {
        return new Cell(rowspan, colspan).add(new Paragraph(texto).setFontSize(fontSize))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(2f);
    }

    // --- TABLA 1: ENCABEZADO SUPERIOR ---
    private Table crearEncabezadoSuperior() {
        float[] anchos = {25f, 25f, 25f, 25f};
        Table table = new Table(anchos);
        table.useAllAvailableWidth();
        table.addCell(crearCelda("GRADO Y GRUPO: ________________", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: ________________", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: ________________", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("MES: ________________", 8f).setTextAlignment(TextAlignment.LEFT).setBorder(null));
        table.addCell(crearCelda("", 8f).setBorder(null)); // Celda vacía para espaciar
        table.addCell(crearCelda("", 8f).setBorder(null));
        table.addCell(crearCelda("", 8f).setBorder(null));
        // Encabezado del trimestre
        Cell trimestreCell = crearCelda("TRIMESTRE _________ 2025", 8f)
                .setTextAlignment(TextAlignment.RIGHT).setBorder(null)
                .setBorderBottom(new com.itextpdf.layout.border.SolidBorder(Color.RED, 1));
        table.addCell(trimestreCell);

        return table;
    }

    // --- TABLA 2: TABLA PRINCIPAL DE CALIFICACIONES ---
    private Table crearTablaCalificaciones() {
        // 3 info + 3*9 mensuales + 8 trimestrales + 2 promedios = 40 columnas
        float[] anchos = new float[40];
        anchos[0] = 3f; // No. LISTA
        anchos[1] = 4f; // SEXO
        anchos[2] = 15f; // NOMBRE
        // Las 37 columnas restantes se reparten el espacio
        for (int i = 3; i < anchos.length; i++) {
            anchos[i] = 2.1f;
        }

        Table table = new Table(anchos);
        table.useAllAvailableWidth();

        // --- FILA 1 DE ENCABEZADOS (Campos Formativos) ---
        table.addCell(crearCeldaSpan("No. LISTA", 2, 1, 6f));
        table.addCell(crearCeldaSpan("SEXO", 2, 1, 6f));
        table.addCell(crearCeldaSpan("NOMBRE DEL ALUMNO", 2, 1, 6f));

        // Repetir 3 veces para los bloques mensuales
        for (int i = 0; i < 3; i++) {
            // ** Textos verticales centrados en ambos ejes **
            table.addCell(crearCeldaSpan("\nLENGUAJES", 1, 3, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaSpan("\nSABERES Y PENS-MAT", 1, 3, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaSpan("ETICA, NAT Y SOC.", 1, 1, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaSpan("HUMANO Y COM.", 1, 1, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell()); // Celda vacía sobre la columna de Promedio mensual
        }

        // Encabezados para el bloque trimestral
        table.addCell(crearCeldaSpan("\nLENGUAJES", 1, 3, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaSpan("\nSABERES Y PENS-MAT", 1, 3, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaSpan("ETICA, NAT Y SOC.", 1, 1, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaSpan("HUMANO Y COM.", 1, 1, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));

        table.addCell(crearCeldaSpan("\n\n\nPROMEDIO", 2, 1, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaSpan("\nPROMEDIO TRIMESTRAL", 2, 1, 6f).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));

        // --- FILA 2 DE ENCABEZADOS (Materias) ---
        // Repetir 3 veces para los bloques mensuales
        for (int i = 0; i < 3; i++) {
            table.addCell(crearCeldaConColor("\nESPAÑOL", 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("\nINGLES", 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("\nARTES", 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("MATEMATICAS", 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("\nTECNOLOGIA", 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("CON. DEL MEDIO", 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("F. CIVICA Y ETICA", 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("\nED. FISICA", 5f, VERDE_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
            table.addCell(crearCeldaConColor("\nPROMEDIO", 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        }

        // Materias para el bloque trimestral
        table.addCell(crearCeldaConColor("\nESPAÑOL", 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("\nINGLES", 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("\nARTES", 5f, AZUL_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("MATEMATICAS", 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("\nTECNOLOGIA", 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("CON. DEL MEDIO", 5f, ROSA_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("F. CIVICA Y ETICA", 5f, AMARILLO_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));
        table.addCell(crearCeldaConColor("\nED. FISICA", 5f, VERDE_CLARO).setRotationAngle(Math.PI / 2).setTextAlignment(TextAlignment.CENTER));

        // --- FILAS DE DATOS ---
        for (int i = 1; i <= 4; i++) {
            table.addCell(crearCelda(String.valueOf(i), 8f)); // No. LISTA
            table.addCell(crearCelda("", 8f)); // SEXO
            table.addCell(crearCelda("", 8f).setTextAlignment(TextAlignment.LEFT)); // NOMBRE

            // Celdas para calificaciones (3 bloques mensuales de 9 + 1 trimestral de 8)
            for (int j = 0; j < 27; j++) table.addCell(crearCelda("", 6f));
            for (int j = 0; j < 8; j++) table.addCell(crearCelda("100", 6f));

            // Promedios
            table.addCell(crearCeldaConColor("100", 6f, AMARILLO_CLARO));
            table.addCell(crearCeldaConColor("0.0", 6f, AMARILLO_CLARO));
        }

        // --- FILA FINAL DE PROMEDIO ---
        table.addCell(crearCeldaSpan("PROMEDIO", 1, 3, 8f));
        // Celdas para promedios por materia
        for (int j = 0; j < 35; j++) table.addCell(crearCelda("##", 8f)); // 3*9 + 8
        // Celda promedio final
        table.addCell(crearCeldaSpan("#####", 1, 2, 8f));

        return table;
    }
}