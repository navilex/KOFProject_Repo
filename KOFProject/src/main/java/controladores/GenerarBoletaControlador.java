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
import javax.swing.JOptionPane;

/**
 *
 * @author Ivan De la Rosa
 */
public class GenerarBoletaControlador {

    public void GenerarBoleta() throws FileNotFoundException {
        String ruta = "Reportes/boleta.pdf";
        PdfWriter MiPdfWriter = new PdfWriter(ruta);
        PdfDocument MiPdfDocument = new PdfDocument(MiPdfWriter);
        // Usamos A4.rotate() para la orientación horizontal
        MiPdfDocument.setDefaultPageSize(PageSize.A4.rotate());
        Document MiDocument = new Document(MiPdfDocument);

        // 1. Datos de Identificación
        MiDocument.add(crearTablaIdentificacion());
        MiDocument.add(new Paragraph("\n"));

        // 2. Tabla de Calificaciones (la más compleja)
        MiDocument.add(crearTablaCalificaciones());
        MiDocument.add(new Paragraph("\n"));

        // 3. Tabla de Inasistencias y Firmas
        MiDocument.add(crearTablaInasistencias()); // <-- Esta es la tabla modificada
        MiDocument.add(new Paragraph("\n"));

        // 4. Tabla de Niveles de Desempeño
        MiDocument.add(crearTablaNivelesDesempeno());
        MiDocument.add(new Paragraph("\n"));
        
        // 5. Tabla de Firmas del Padre o Tutor (Nueva)
        MiDocument.add(crearTablaFirmasPadres());

        MiDocument.close();
        JOptionPane.showMessageDialog(null, "Boleta generada exitosamente en: " + ruta);
    }

    // --- MÉTODOS AUXILIARES ---

    // Colores para las celdas, basados en la imagen
    private static final Color AZUL_CLARO = new DeviceRgb(214, 233, 248);
    private static final Color ROSA_CLARO = new DeviceRgb(245, 222, 239);
    private static final Color AMARILLO_CLARO = new DeviceRgb(252, 243, 215);
    private static final Color VERDE_CLARO = new DeviceRgb(213, 232, 213);
    
    // Nuevos colores para la tabla de inasistencias y periodos
    private static final Color GRIS_CLARO = new DeviceRgb(220, 220, 220); // D
    private static final Color NARANJA_CLARO = new DeviceRgb(255, 229, 204); // S, O, N/D
    private static final Color VERDE_AGUA_CLARO = new DeviceRgb(204, 255, 204); // M, A, M, J
    private static final Color AZUL_PALIDO = new DeviceRgb(204, 229, 255); // PF

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


    // --- TABLA 1: IDENTIFICACIÓN ---
    private Table crearTablaIdentificacion() {
        float[] anchoColumna = {35f, 65f};
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();

        table.addCell(crearCelda("NOMBRE DE LA ESCUELA:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda("___________________________________", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));
        table.addCell(crearCelda("SECCION:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda("PRIMARIA", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));
        table.addCell(crearCelda("GRADO Y GRUPO:", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda("PRIMERO \"A\"", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));
        table.addCell(crearCelda("ALUMNO (A):", TextAlignment.LEFT, VerticalAlignment.MIDDLE, true, null, 10f));
        table.addCell(crearCelda("___________________________________", TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 10f));

        return table;
    }


    // --- TABLA 2: CALIFICACIONES ---
    private Table crearTablaCalificaciones() {
        // --- Contenedor para alinear la tabla principal y la de periodos ---
        float[] anchosContenedor = {75f, 25f};
        Table tablaContenedora = new Table(anchosContenedor);
        tablaContenedora.useAllAvailableWidth();

        // --- TABLA A: TABLA PRINCIPAL (CAMPOS + CALIFICACIONES) ---
        // ** CAMBIO: Anchos ajustados para texto horizontal **
        float[] anchosPrincipal = {20f, 20f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f};
        Table tablaPrincipal = new Table(anchosPrincipal);
        tablaPrincipal.useAllAvailableWidth();
        
        float alturaFila = 20f;

        // Fila 1 de encabezados
        tablaPrincipal.addCell(crearCeldaSpan("CAMPOS DE FORMACIÓN ACADÉMICA", 2, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPrincipal.addCell(crearCeldaSpan("CALIFICACIONES MENSUALES", 1, 10, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        
        // Fila 2 de encabezados (meses) - ** SIN ROTACIÓN **
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
        
        // --- Filas de datos ---
        
        // Grupo 1: PENS. Y LENGUAJES - ** SIN ROTACIÓN **
        tablaPrincipal.addCell(crearCeldaSpan("PENS. Y LENGUAJES", 3, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f));
        tablaPrincipal.addCell(crearCelda("ESPAÑOL", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        
        tablaPrincipal.addCell(crearCelda("INGLES", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        
        tablaPrincipal.addCell(crearCelda("ARTES", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));

        // Grupo 2: SABERS Y CIENTIFICO - ** SIN ROTACIÓN **
        tablaPrincipal.addCell(crearCeldaSpan("SABERES Y CIENTIFICO", 3, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f));
        tablaPrincipal.addCell(crearCelda("MATEMATICAS", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));

        tablaPrincipal.addCell(crearCelda("TECNOLOGIA", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        
        tablaPrincipal.addCell(crearCelda("CONOCIMIENTO DEL MEDIO", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, ROSA_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));

        // Grupo 3: DE LO ETICA, HUMANO Y COMUNITARIO - ** SIN ROTACIÓN **
        tablaPrincipal.addCell(crearCeldaSpan("DE LO ETICA, HUMANO Y COMUNITARIO", 2, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 8f));
        tablaPrincipal.addCell(crearCelda("FORM. CIVICA Y ETICA", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        
        tablaPrincipal.addCell(crearCelda("ED. FISICA", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_CLARO, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
        
        // Fila Final: Promedio Mensual
        tablaPrincipal.addCell(crearCeldaSpan("PROM. MENSUAL", 1, 2, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f).setHeight(alturaFila));
        for (int i = 0; i < 10; i++) tablaPrincipal.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));


        // --- TABLA B: PERIODOS ---
        float[] anchosPeriodos = {25f, 25f, 25f, 25f};
        Table tablaPeriodos = new Table(anchosPeriodos);
        tablaPeriodos.useAllAvailableWidth();
        
        tablaPeriodos.addCell(crearCeldaSpan("PERIODOS", 1, 4, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        tablaPeriodos.addCell(crearCelda("1er TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPeriodos.addCell(crearCelda("2º TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPeriodos.addCell(crearCelda("3er TRIM", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPeriodos.addCell(crearCelda("PROMEDIO FINAL", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        for (int i = 0; i < 3; i++) tablaPeriodos.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));
        tablaPeriodos.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 8f));

        for (int i = 0; i < 9; i++) {
            for (int k = 0; k < 4; k++) {
                tablaPeriodos.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 8f).setHeight(alturaFila));
            }
        }
        
        // --- Añadir las 2 tablas al contenedor ---
        tablaContenedora.addCell(new Cell().add(tablaPrincipal).setBorder(null).setPadding(1f));
        tablaContenedora.addCell(new Cell().add(tablaPeriodos).setBorder(null).setPadding(1f));

        return tablaContenedora;
    }


    // --- TABLA 3: INASISTENCIAS (Modificada según la imagen) ---
    private Table crearTablaInasistencias() {
        // Tabla contenedora para alinear la tabla de meses y la de calificaciones
        float[] anchosContenedorInasistencias = {75f, 25f}; 
        Table tablaContenedora = new Table(anchosContenedorInasistencias);
        tablaContenedora.useAllAvailableWidth();

        // Tabla de Inasistencias (izquierda)
        float[] anchoColumna = {20f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f, 6.5f}; // Ajustado para 10 meses + 1 columna
        Table tablaInasistencias = new Table(anchoColumna);
        tablaInasistencias.useAllAvailableWidth();

        // Encabezado principal (celda combinada)
        tablaInasistencias.addCell(crearCeldaSpan("", 1, 1, TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f)); // Celda vacía grande

        // Encabezados de meses (abreviados y con color)
        tablaInasistencias.addCell(crearCelda("D", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, GRIS_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("S", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("O", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        
        // NOV/DIC es vertical en la imagen
        Cell novDicCell = crearCelda("N\nD", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 7f);
        tablaInasistencias.addCell(novDicCell);

        tablaInasistencias.addCell(crearCelda("E", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("F", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("M", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("A", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("M", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaInasistencias.addCell(crearCelda("J", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));

        // Fila de Inasistencias
        tablaInasistencias.addCell(crearCelda("INASISTENCIAS", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        float alturaFilaInasistencias = 20f; // Altura para las filas de datos
        for (int i = 0; i < 10; i++) { // 10 columnas de meses
            tablaInasistencias.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
        }
        
        // Tabla de Calificaciones (derecha)
        float[] anchosCalificaciones = {25f, 25f, 25f, 25f};
        Table tablaCalificaciones = new Table(anchosCalificaciones);
        tablaCalificaciones.useAllAvailableWidth();

        // Encabezados de calificaciones
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, NARANJA_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AMARILLO_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("CALIF.", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, VERDE_AGUA_CLARO, 9f));
        tablaCalificaciones.addCell(crearCelda("PF", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, AZUL_PALIDO, 9f));
        
        // Filas de datos (manteniendo la misma altura que las inasistencias)
        for(int i = 0; i < 1; i++) { // Hay 2 filas de datos debajo de los encabezados
            for (int k = 0; k < 4; k++) {
                tablaCalificaciones.addCell(crearCelda("", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setHeight(alturaFilaInasistencias));
            }
        }
        
        // Añadir las dos tablas a la tabla contenedora
        tablaContenedora.addCell(new Cell().add(tablaInasistencias).setBorder(null).setPadding(1f));
        tablaContenedora.addCell(new Cell().add(tablaCalificaciones).setBorder(null).setPadding(1f));

        return tablaContenedora;
    }


     // --- TABLA 4: NIVELES DE DESEMPEÑO ---
    private Table crearTablaNivelesDesempeno() {
        // ** CAMBIO: Ancho de la primera columna reducido **
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
    
    // --- NUEVA TABLA 5: FIRMAS DEL PADRE O TUTOR ---
    private Table crearTablaFirmasPadres() {
        // Tabla contenedora para los dos paneles de firmas (izquierda y derecha)
        float[] anchosContenedor = {50f, 50f}; // Dos columnas al 50%
        Table tablaPrincipal = new Table(anchosContenedor);
        tablaPrincipal.useAllAvailableWidth();
        tablaPrincipal.setBorder(null); // La tabla principal no tiene bordes visibles

        // --- Panel Izquierdo ---
        // Anchos para "MES" y "FIRMA"
        float[] anchosPanel = {30f, 70f}; 
        Table panelIzquierdo = new Table(anchosPanel);
        panelIzquierdo.useAllAvailableWidth();

        // Encabezados del panel izquierdo
        panelIzquierdo.addCell(crearCelda("MES", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        panelIzquierdo.addCell(crearCelda("FIRMA DEL PADRE O TUTOR", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));

        // Datos del panel izquierdo
        String[] mesesIzquierdos = {"AGOSTO DIAGNOSTICO", "SEPTIEMBRE", "OCTUBRE", "NOV/ DIC", "ENERO"};
        for (String mes : mesesIzquierdos) {
            // Celda del mes con más espacio vertical
            panelIzquierdo.addCell(crearCelda(mes, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
            // Celda con la línea para la firma
            panelIzquierdo.addCell(crearCelda("_______________________________________", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
        }

        // --- Panel Derecho ---
        Table panelDerecho = new Table(anchosPanel); // Mismos anchos para consistencia
        panelDerecho.useAllAvailableWidth();

        // Encabezados del panel derecho
        panelDerecho.addCell(crearCelda("MES", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));
        panelDerecho.addCell(crearCelda("FIRMA DEL PADRE O TUTOR", TextAlignment.CENTER, VerticalAlignment.MIDDLE, true, null, 9f));

        // Datos del panel derecho
        String[] mesesDerechos = {"FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO"};
        for (String mes : mesesDerechos) {
            panelDerecho.addCell(crearCelda(mes, TextAlignment.LEFT, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
            panelDerecho.addCell(crearCelda("_______________________________________", TextAlignment.CENTER, VerticalAlignment.MIDDLE, false, null, 9f).setPaddingTop(8f).setPaddingBottom(8f));
        }

        // Añadir los paneles a la tabla principal
        tablaPrincipal.addCell(new Cell().add(panelIzquierdo).setBorder(null)); // La celda contenedora no tiene borde
        tablaPrincipal.addCell(new Cell().add(panelDerecho).setBorder(null)); // La celda contenedora no tiene borde

        return tablaPrincipal;
    }
}