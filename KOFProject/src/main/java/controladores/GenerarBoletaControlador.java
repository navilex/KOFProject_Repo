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
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;

/**
 *
 * @author Ivan De la Rosa
 */
public class GenerarBoletaControlador 
{
    public void GenerarBoleta() throws FileNotFoundException {
        // --- CORRECCIÓN 1: Orientación Horizontal (Landscape) ---
        String ruta = "Reportes/boleta.pdf";
        PdfWriter MiPdfWriter = new PdfWriter(ruta);
        PdfDocument MiPdfDocument = new PdfDocument(MiPdfWriter);
        // Usamos A4.rotate() para la orientación horizontal
        MiPdfDocument.setDefaultPageSize(PageSize.A4.rotate()); 
        Document MiDocument = new Document(MiPdfDocument);
        
        // El documento se crea con márgenes por defecto. Puedes ajustarlos con MiDocument.setMargins(...)

        // 1. Datos de Identificación
        MiDocument.add(crearTablaIdentificacion());
        MiDocument.add(new Paragraph("\n")); 

        // 2. Tabla de Calificaciones (la más compleja)
        MiDocument.add(crearTablaCalificaciones());
        MiDocument.add(new Paragraph("\n"));

        // 3. Tabla de Inasistencias y Firmas
        MiDocument.add(crearTablaInasistencias());
        MiDocument.add(new Paragraph("\n"));

        // 4. Tabla de Niveles de Desempeño
        MiDocument.add(crearTablaNivelesDesempeno());
        
        MiDocument.close();
        JOptionPane.showMessageDialog(null, "Boleta generada exitosamente en: " + ruta);
    }
    
// --- MÉTODOS AUXILIARES ---

    private Cell crearCelda(String contenido, TextAlignment align, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(contenido))
                .setTextAlignment(align)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3f);
        if (isHeader) {
            cell.setBold();
        }
        return cell;
    }

    private Cell crearCeldaSpan(String contenido, int rowspan, int colspan, TextAlignment align, boolean isHeader) {
        Cell cell = new Cell(rowspan, colspan).add(new Paragraph(contenido))
                .setTextAlignment(align)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(3f);
        if (isHeader) {
            cell.setBold();
        }
        return cell;
    }
    
// --- TABLA 1: IDENTIFICACIÓN (2 COLUMNAS) ---

    private Table crearTablaIdentificacion() {
        // 2 Columnas con proporciones: 35% y 65% del ancho total
        float[] anchoColumna = {35f, 65f}; 
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();

        table.addCell(crearCelda("NOMBRE DE LA ESCUELA:", TextAlignment.LEFT, true));
        table.addCell(crearCelda("___________________________________", TextAlignment.LEFT, false));

        table.addCell(crearCelda("SECCION:", TextAlignment.LEFT, true));
        table.addCell(crearCelda("PRIMARIA", TextAlignment.LEFT, false));

        table.addCell(crearCelda("GRADO Y GRUPO:", TextAlignment.LEFT, true));
        table.addCell(crearCelda("PRIMERO \"A\"", TextAlignment.LEFT, false));
        
        table.addCell(crearCelda("ALUMNO (A):", TextAlignment.LEFT, true));
        table.addCell(crearCelda("___________________________________", TextAlignment.LEFT, false));

        return table;
    }

// --- TABLA 2: CALIFICACIONES (16 COLUMNAS) ---

    private Table crearTablaCalificaciones() {
        // --- CORRECCIÓN 2: 16 Columnas para la complejidad visual del ejemplo ---
        // 1 (Línea) + 4 (Campos) + 7 (Meses) + 4 (Periodos) = 16
        // Proporciones de ancho relativas (suman 100f)
        float[] anchoColumna = {
            20f, // Línea / Materia (Col 1)
            5f, 5f, 5f, 5f, // 4 Campos (4 * 5 = 20)
            4f, 4f, 4f, 4f, 4f, 4f, 4f, // 7 Meses (7 * 4 = 28)
            8f, 8f, 8f, 8f // 4 Periodos (4 * 8 = 32)
        }; 
        
        Table table = new Table(anchoColumna); 
        table.useAllAvailableWidth();

        // Fila 1: Títulos principales con Colspan
        // La columna "FORMACIÓN" abarca 3 filas (Rowspan 3) y 1 columna.
        table.addCell(crearCeldaSpan("FORMACIÓN", 3, 1, TextAlignment.CENTER, true)); 
        
        // Encabezados de Campos
        table.addCell(crearCeldaSpan("CAMPOS", 1, 4, TextAlignment.CENTER, true)); 
        
        // Encabezados de Calificaciones y Periodos
        table.addCell(crearCeldaSpan("CALIFICACIONES MENSUALES", 1, 7, TextAlignment.CENTER, true)); 
        table.addCell(crearCeldaSpan("PERIODOS", 1, 4, TextAlignment.CENTER, true)); 

        // Fila 2: Sub-Campos y Meses/Trimestres
        // Campos (Columnas 2 a 5, Colspan 1)
        table.addCell(crearCeldaSpan("PENS. LENGUAJES", 1, 1, TextAlignment.CENTER, true));
        table.addCell(crearCeldaSpan("SABERS Y CIENTIFICO", 1, 1, TextAlignment.CENTER, true));
        table.addCell(crearCeldaSpan("DE LO ÉTICA HUMANO Y COMUNITARI LO", 1, 1, TextAlignment.CENTER, true));
        table.addCell(crearCeldaSpan("NATURALEZA Y SOC.", 1, 1, TextAlignment.CENTER, true));
        
        // Meses de Calificaciones (Columnas 6 a 12, Colspan 1)
        table.addCell(crearCelda("SEPTIEMBRE", TextAlignment.CENTER, true));
        table.addCell(crearCelda("OCTUBRE", TextAlignment.CENTER, true));
        table.addCell(crearCelda("NOV/DIC", TextAlignment.CENTER, true));
        table.addCell(crearCelda("ENERO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("FEBRERO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("MARZO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("ABRIL", TextAlignment.CENTER, true));
        
        // Trimestres (Columnas 13 a 16, Colspan 1)
        table.addCell(crearCelda("1 TRIM", TextAlignment.CENTER, true));
        table.addCell(crearCelda("2 TRIM", TextAlignment.CENTER, true));
        table.addCell(crearCelda("3 TRIM", TextAlignment.CENTER, true));
        table.addCell(crearCelda("PROMEDIO FINAL", TextAlignment.CENTER, true));

        // Fila 3: CALIFICACIONES (Encabezados de las materias)
        table.addCell(crearCelda("ESPAÑOL", TextAlignment.LEFT, true));
        table.addCell(crearCelda("MATEMÁTICAS", TextAlignment.LEFT, true));
        table.addCell(crearCelda("CONOCIMIENTO DEL MEDIO", TextAlignment.LEFT, true));
        table.addCell(crearCelda("FORM. CIVICA Y ÉTICA", TextAlignment.LEFT, true));
        
        // Calificación para cada mes y trimestre (11 celdas + 1 PF)
        for(int i = 0; i < 11; i++){
            table.addCell(crearCelda("CALIF.", TextAlignment.CENTER, true));
        }
        table.addCell(crearCelda("PF", TextAlignment.CENTER, true));

        // --- Filas de Datos (Simulando la estructura compleja) ---
        
        // 1. PROM. ACADÉMICA (La fila que envuelve a varias materias)
        //table.addCell(crearCelda("PROM. ACADÉMICA", TextAlignment.CENTER, true).setRowspan(4)); // Rowspan para las 4 materias debajo
        
        // 4 Materias con datos (se rellenan las 15 columnas restantes)
        table.addCell(crearCelda("ESPAÑOL", TextAlignment.LEFT, false));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("9.0", TextAlignment.CENTER, false));

        table.addCell(crearCelda("MATEMÁTICAS", TextAlignment.LEFT, false));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("9.2", TextAlignment.CENTER, false));
        
        table.addCell(crearCelda("CONOCIMIENTO DEL MEDIO", TextAlignment.LEFT, false));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("9.5", TextAlignment.CENTER, false));
        
        table.addCell(crearCelda("FORM. CIVICA Y ÉTICA", TextAlignment.LEFT, false));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("9.8", TextAlignment.CENTER, false));
        
        // 2. Otras Materias con Rowspan 1
        table.addCell(crearCelda("EDUCACIÓN FÍSICA", TextAlignment.LEFT, true));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("-", TextAlignment.CENTER, false));

        table.addCell(crearCelda("ARTES", TextAlignment.LEFT, true));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("-", TextAlignment.CENTER, false));

        table.addCell(crearCelda("INGLÉS", TextAlignment.LEFT, true));
        for (int i = 0; i < 15; i++) table.addCell(crearCelda("-", TextAlignment.CENTER, false));

        /*
        // Última fila: Promedio Final Global (coloca el valor en el PF)
        Cell promedioFinalCell = crearCelda("PROMEDIO FINAL", TextAlignment.LEFT, true).setColspan(14);
        table.addCell(promedioFinalCell); 
        table.addCell(crearCelda("9.3", TextAlignment.CENTER, true).setColspan(2)); 
        */

        return table;
    }

// --- TABLA 3: INASISTENCIAS Y FIRMAS (11 COLUMNAS) ---

    private Table crearTablaInasistencias() {
        // 11 columnas: 1 para el título (15%), 10 para los meses (8.5% c/u).
        float[] anchoColumna = {
            15f, 
            8.5f, 8.5f, 8.5f, 8.5f, 8.5f, 8.5f, 8.5f, 8.5f, 8.5f, 8.5f 
        }; 
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();
        
        // Fila de Encabezados de Meses
        table.addCell(crearCelda("MES", TextAlignment.CENTER, true));
        table.addCell(crearCelda("AGOSTO DIAGNÓSTICO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("SEPTIEMBRE", TextAlignment.CENTER, true));
        table.addCell(crearCelda("OCTUBRE", TextAlignment.CENTER, true));
        table.addCell(crearCelda("NOV/DIC", TextAlignment.CENTER, true));
        table.addCell(crearCelda("ENERO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("FEBRERO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("MARZO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("ABRIL", TextAlignment.CENTER, true));
        table.addCell(crearCelda("MAYO", TextAlignment.CENTER, true));
        table.addCell(crearCelda("JUNIO", TextAlignment.CENTER, true));

        // Fila de Datos de Inasistencias
        table.addCell(crearCelda("INASISTENCIAS", TextAlignment.CENTER, true));
        for (int i = 0; i < 10; i++) {
            table.addCell(crearCelda("", TextAlignment.CENTER, false).setHeight(15f));
        }
        
        // Filas para FIRMA DEL PADRE O TUTOR
        // La celda de título abarca 6 filas
        table.addCell(crearCeldaSpan("FIRMA DEL PADRE O TUTOR", 6, 1, TextAlignment.CENTER, true)); 
        
        /*
        // Firmas (Cada mes ocupa 10 columnas)
        table.addCell(crearCelda("AGOSTO DIAGNÓSTICO", TextAlignment.CENTER, true).setColspan(10));
        table.addCell(crearCelda("", TextAlignment.CENTER, false).setColspan(10).setHeight(15f));
        table.addCell(crearCelda("SEPTIEMBRE", TextAlignment.CENTER, true).setColspan(10));
        table.addCell(crearCelda("", TextAlignment.CENTER, false).setColspan(10).setHeight(15f));
        table.addCell(crearCelda("OCTUBRE", TextAlignment.CENTER, true).setColspan(10));
        table.addCell(crearCelda("", TextAlignment.CENTER, false).setColspan(10).setHeight(15f));
        */
        return table;
    }

// --- TABLA 4: NIVELES DE DESEMPEÑO (2 COLUMNAS) ---
    
    private Table crearTablaNivelesDesempeno() {
        // 2 columnas: 20% y 80% del ancho total
        float[] anchoColumna = {20f, 80f};
        Table table = new Table(anchoColumna);
        table.useAllAvailableWidth();
        
        table.addCell(crearCeldaSpan("NIVELES DE DESEMPEÑO", 1, 2, TextAlignment.CENTER, true));

        table.addCell(crearCelda("NIVEL I\nEQUIVALE A 5", TextAlignment.CENTER, true));
        table.addCell(crearCelda("El estudiante tiene carencias fundamentales en valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, false));

        table.addCell(crearCelda("NIVEL II\nEQUIVALE A 6 Y 7", TextAlignment.CENTER, true));
        table.addCell(crearCelda("El estudiante tiene dificultades para demostrar valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, false));

        table.addCell(crearCelda("NIVEL III\nEQUIVALE A 8 Y 9", TextAlignment.CENTER, true));
        table.addCell(crearCelda("El estudiante ha demostrado los valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, false));

        table.addCell(crearCelda("NIVEL IV\nEQUIVALE A 10", TextAlignment.CENTER, true));
        table.addCell(crearCelda("El estudiante ha demostrado los valores y principios para desarrollar una convivencia sana y pacifica, dentro y fuera del aula.", TextAlignment.LEFT, false));

        return table;
    }
}
