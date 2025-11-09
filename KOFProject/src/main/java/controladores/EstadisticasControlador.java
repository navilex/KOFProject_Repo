/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
//import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ivan De la Rosa
 */
public class EstadisticasControlador {

    private static final Logger logger = Logger.getLogger(EstadisticasControlador.class.getName());

    // --- CONSTANTES DE DISEÑO ---
    private static final Border NO_BORDER = Border.NO_BORDER;
    private static final Border DEFAULT_BORDER = new SolidBorder(0.5f);
    private static final float FONT_SIZE_NORMAL = 9f;
    private static final float FONT_SIZE_PEQUEÑO = 8f;
    private static final float FONT_SIZE_TITULO = 10f;

    /**
     * Constructor vacío.
     */
    public EstadisticasControlador() {
        // Constructor
    }

    /**
     * Clase simple para almacenar los conteos de género por grupo.
     */
    private class ConteoGenero {
        int hombres = 0;
        int mujeres = 0;
    }
    
    /**
     * Clase para almacenar los datos básicos del grupo desde la BD.
     */
    private class InfoGrupo {
        final int idGrupo;
        final int grado;
        final String grupo;

        InfoGrupo(int idGrupo, int grado, String grupo) {
            this.idGrupo = idGrupo;
            this.grado = grado;
            this.grupo = grupo;
        }
    }


    /**
     * Método principal para generar el PDF de estadísticas.
     * Recibe la conexión desde el formulario.
     */
    public void generarEstadistica(Connection miConexion) {
        String ruta = "Reportes/Estadistica_Basica.pdf";

        File directorio = new File("Reportes");
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        try {
            
            if (miConexion == null || miConexion.isClosed()) {
                JOptionPane.showMessageDialog(null, "Error: La conexión a la base de datos no es válida.");
                return;
            }

            PdfWriter miPdfWriter = new PdfWriter(ruta);
            PdfDocument miPdfDocument = new PdfDocument(miPdfWriter);
            miPdfDocument.setDefaultPageSize(PageSize.A4);
            Document miDocument = new Document(miPdfDocument);

            // 1. Encabezado
            miDocument.add(crearTablaEncabezado());
            miDocument.add(new Paragraph("\n"));

            // 2. Título principal 
            miDocument.add(new Paragraph("ESTADÍSTICA BÁSICA POR CENTRO DE TRABAJO")
                    .setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(FONT_SIZE_TITULO));
            miDocument.add(new Paragraph("Fecha de Impresion: Feb 15, 2023, 9:10 am")
                    .setTextAlignment(TextAlignment.RIGHT).setFontSize(FONT_SIZE_PEQUEÑO));
            miDocument.add(new Paragraph("\n"));

            // 3. Información de la escuela
            miDocument.add(crearTablaInfoEscuela());
            miDocument.add(new Paragraph("\n"));

            // 4. Tabla principal de estadísticas (Pasamos la conexión)
            miDocument.add(crearTablaEstadisticas(miConexion));
            miDocument.add(new Paragraph("\n"));
            miDocument.add(new Paragraph("\n"));
            miDocument.add(new Paragraph("\n"));
            miDocument.add(new Paragraph("\n"));
            
            // 5. Firmas 
            miDocument.add(crearTablaFirmas());
            miDocument.add(new Paragraph("\n"));
            miDocument.add(new Paragraph("\n"));

            // 6. Pie de página
            miDocument.add(crearTablaPieDePagina());

            // --- Cierre del documento ---
            miDocument.close();
            JOptionPane.showMessageDialog(null, "Estadística generada exitosamente en: " + ruta);

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "Error al generar PDF: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "Error al generar PDF: " + ex.toString());
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error de SQL: " + ex.getMessage(), ex);
            JOptionPane.showMessageDialog(null, "Error de Base de Datos: " + ex.toString());
        }
    }

    // --- MÉTODOS AUXILIARES DE DISEÑO (Sin cambios) ---
    
    private Cell crearCelda(String contenido, TextAlignment hAlign, VerticalAlignment vAlign, boolean isBold, Color bgColor, float fontSize, Border border) {
        Paragraph p = new Paragraph(contenido).setFontSize(fontSize);
        p.setTextAlignment(hAlign);
        if (isBold) p.setBold();
        Cell cell = new Cell().add(p).setTextAlignment(hAlign).setVerticalAlignment(vAlign).setPadding(2f);
        if (bgColor != null) cell.setBackgroundColor(bgColor);
        cell.setBorder(border);
        return cell;
    }

    private Cell crearCeldaSpan(String contenido, int rowspan, int colspan, TextAlignment hAlign, VerticalAlignment vAlign, boolean isBold, Color bgColor, float fontSize, Border border) {
        Paragraph p = new Paragraph(contenido).setFontSize(fontSize);
        p.setTextAlignment(hAlign);
        if (isBold) p.setBold();
        Cell cell = new Cell(rowspan, colspan).add(p).setTextAlignment(hAlign).setVerticalAlignment(vAlign).setPadding(2f);
        if (bgColor != null) cell.setBackgroundColor(bgColor);
        cell.setBorder(border);
        return cell;
    }
    
    private Cell celdaSinBorde(String c, TextAlignment h, boolean bold, float size) {
        return crearCelda(c, h, VerticalAlignment.MIDDLE, bold, null, size, NO_BORDER);
    }
    
    private Cell celdaConBorde(String c, TextAlignment h, boolean bold, float size) {
        return crearCelda(c, h, VerticalAlignment.MIDDLE, bold, null, size, DEFAULT_BORDER);
    }
    
    private Cell celdaSpanConBorde(String c, int colspan, TextAlignment h, boolean bold, float size) {
        return crearCeldaSpan(c, 1, colspan, h, VerticalAlignment.MIDDLE, bold, null, size, DEFAULT_BORDER);
    }

    // --- MÉTODOS DE CREACIÓN DE TABLAS (Info, Encabezado, Firmas, Pie - Sin cambios) ---
    
    private Table crearTablaEncabezado() {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
        table.useAllAvailableWidth();
        
        try 
        {
            String path = "src/main/resources/SEPBN.png";
            ImageData imageData = ImageDataFactory.create(path);
            Image image = new Image(imageData);
            image.setAutoScale(true);

            Cell imageCell = new Cell(4, 1).add(image);
            imageCell.setBorder(null);
            table.addCell(imageCell);

        } 
        catch (MalformedURLException ex) 
        {
            System.out.println("Error: No se encontró la imagen en la ruta especificada. " + ex.getMessage());
            table.addCell(new Cell(4, 1).setBorder(null));
        }
        
        try 
        {
            String path = "src/main/resources/SEPC.png";
            ImageData imageData = ImageDataFactory.create(path);
            Image image = new Image(imageData);
            image.setAutoScale(true);

            Cell imageCell = new Cell(4, 1).add(image);
            imageCell.setBorder(null);
            table.addCell(imageCell);

        } 
        catch (MalformedURLException ex) 
        {
            System.out.println("Error: No se encontró la imagen en la ruta especificada. " + ex.getMessage());
            table.addCell(new Cell(4, 1).setBorder(null));
        }
        
        //table.addCell(celdaSinBorde("GOBIERNO DEL ESTADO\n2021-2027", TextAlignment.CENTER, true, FONT_SIZE_NORMAL));
        //table.addCell(celdaSinBorde("SECRETARÍA DE\nEDUCACIÓN\nGUERRERO", TextAlignment.CENTER, true, FONT_SIZE_NORMAL));
        return table;
    }

    private Table crearTablaInfoEscuela() {
        Table container = new Table(UnitValue.createPercentArray(new float[]{65f, 35f}));
        container.useAllAvailableWidth();
        container.setBorder(NO_BORDER);

        Table leftPanel = new Table(UnitValue.createPercentArray(new float[]{18f, 82f}));
        leftPanel.useAllAvailableWidth().setBorder(NO_BORDER);
        leftPanel.addCell(celdaSinBorde("Nombre:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("PRIMARIA ARCOIRIS", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("C.C.T.:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("12PJN0113T", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("Domicilio:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("CALLE VICENTE GUERRERO", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("Localidad:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("ACAPULCO DE JUAREZ", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("Municipio:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        leftPanel.addCell(celdaSinBorde("ACAPULCO DE JUAREZ", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));

        Table rightPanel = new Table(UnitValue.createPercentArray(new float[]{35f, 65f}));
        rightPanel.useAllAvailableWidth().setBorder(NO_BORDER);
        rightPanel.addCell(celdaSinBorde("Turno:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("100", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("Zona CCT:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("12FZP50190", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("Ciclo Escolar:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("2025-2026", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("Id. Docto:", TextAlignment.LEFT, true, FONT_SIZE_NORMAL));
        rightPanel.addCell(celdaSinBorde("", TextAlignment.LEFT, false, FONT_SIZE_NORMAL));

        container.addCell(new Cell().add(leftPanel).setBorder(NO_BORDER));
        container.addCell(new Cell().add(rightPanel).setBorder(NO_BORDER).setVerticalAlignment(VerticalAlignment.TOP));
        
        return container;
    }

    // =========================================================================
    // --- LÓGICA DE CONTEO (MODIFICADA PARA CAUSA 2) ---
    // =========================================================================
    
    /**
     * Paso 1: Obtiene los conteos de género desde la tabla 'alumnos' y los
     * almacena en un Mapa.
     */
    private Map<Integer, ConteoGenero> obtenerConteoPorGenero(Connection conexion) throws SQLException {
        Map<Integer, ConteoGenero> mapaDeConteos = new HashMap<>();
        
        String SQL_ALUMNOS = "SELECT id_grupo, genero FROM alumnos";
        
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ALUMNOS);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int idGrupo = rs.getInt("id_grupo");
                String genero = rs.getString("genero");
                
                // Si el género es nulo, saltar a la siguiente iteración
                if (genero == null) {
                    continue;
                }

                // Busca el grupo en el mapa, si no existe, lo crea
                ConteoGenero conteo = mapaDeConteos.get(idGrupo);
                if (conteo == null) {
                    conteo = new ConteoGenero();
                    mapaDeConteos.put(idGrupo, conteo);
                }

                // --- INICIO DE LA MODIFICACIÓN ---
                // Ahora comprueba "Masculino" Y "Hombre"
                if ("Masculino".equalsIgnoreCase(genero) || "Hombre".equalsIgnoreCase(genero)) {
                    conteo.hombres++;
                } 
                // Ahora comprueba "Femenino" Y "Mujer"
                else if ("Femenino".equalsIgnoreCase(genero) || "Mujer".equalsIgnoreCase(genero)) {
                    conteo.mujeres++;
                }
                // --- FIN DE LA MODIFICACIÓN ---
            }
        }
        return mapaDeConteos;
    }

    /**
     * Paso 2: Construye la tabla del PDF usando los conteos del mapa.
     */
    private Table crearTablaEstadisticas(Connection conexion) throws SQLException {
        Table table = new Table(UnitValue.createPercentArray(new float[]{15f, 25f, 20f, 20f, 20f}));
        table.useAllAvailableWidth();
        
        // --- Encabezados ---
        table.addCell(celdaConBorde("GRADO", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO));
        table.addCell(celdaConBorde("GRUPO", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO));
        table.addCell(celdaConBorde("HOMBRES", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO));
        table.addCell(celdaConBorde("MUJERES", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO));
        table.addCell(celdaConBorde("TOTAL", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO));

        // --- PASO 1: Obtener el mapa de conteos (Hombres/Mujeres) ---
        Map<Integer, ConteoGenero> conteos = obtenerConteoPorGenero(conexion);

        // --- PASO 2: Obtener la lista de todos los grupos ---
        List<InfoGrupo> listaDeGrupos = new ArrayList<>();
        String SQL_GRUPOS = "SELECT id_grupo, Grado, Grupo FROM Grupos ORDER BY Grado, Grupo";
        
        try (PreparedStatement ps = conexion.prepareStatement(SQL_GRUPOS);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                listaDeGrupos.add(new InfoGrupo(
                    rs.getInt("id_grupo"),
                    rs.getInt("Grado"),
                    rs.getString("Grupo")
                ));
            }
        }

        if (listaDeGrupos.isEmpty()) {
            logger.warning("No se encontraron datos en la tabla 'Grupos'.");
            return table;
        }

        // --- Variables de control para el GRAN TOTAL ---
        int granTotalHombres = 0;
        int granTotalMujeres = 0;
        int granTotalAlumnos = 0;
        
        // --- PASO 3: Iterar sobre la LISTA DE GRUPOS y construir la tabla ---
        for (InfoGrupo g : listaDeGrupos) {
            
            // Buscar el conteo para este grupo en el mapa
            ConteoGenero conteo = conteos.get(g.idGrupo);
            
            int hombres = 0;
            int mujeres = 0;
            int total = 0;

            if (conteo != null) {
                // Si se encontró un conteo, usar esos valores
                hombres = conteo.hombres;
                mujeres = conteo.mujeres;
                total = hombres + mujeres; 
            }

            // Limpia el nombre del grupo (Ej: "1A" -> "A")
            String nombreGrupo = g.grupo.replaceAll(String.valueOf(g.grado), "").trim();

            // --- 1. Añadir la fila de DATOS del grupo ---
            table.addCell(celdaConBorde(String.valueOf(g.grado), TextAlignment.CENTER, false, FONT_SIZE_PEQUEÑO));
            table.addCell(celdaConBorde(nombreGrupo, TextAlignment.CENTER, false, FONT_SIZE_PEQUEÑO)); 
            table.addCell(celdaConBorde(String.valueOf(hombres), TextAlignment.CENTER, false, FONT_SIZE_PEQUEÑO));
            table.addCell(celdaConBorde(String.valueOf(mujeres), TextAlignment.CENTER, false, FONT_SIZE_PEQUEÑO));
            table.addCell(celdaConBorde(String.valueOf(total), TextAlignment.CENTER, false, FONT_SIZE_PEQUEÑO));

            // --- 2. Añadir la fila de SUBTOTAL del grupo ---
            table.addCell(celdaConBorde("SUBTOTAL", TextAlignment.LEFT, true, FONT_SIZE_PEQUEÑO)); 
            table.addCell(celdaConBorde("", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); // Celda de Grupo VACÍA
            table.addCell(celdaConBorde(String.valueOf(hombres), TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); 
            table.addCell(celdaConBorde(String.valueOf(mujeres), TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); 
            table.addCell(celdaConBorde(String.valueOf(total), TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); 

            // --- Acumular el GRAN TOTAL ---
            granTotalHombres += hombres;
            granTotalMujeres += mujeres;
            granTotalAlumnos += total;
        }

        // --- Total General --- 
        table.addCell(celdaConBorde("TOTAL GENERAL", TextAlignment.LEFT, true, FONT_SIZE_PEQUEÑO)); 
        table.addCell(celdaConBorde("", TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); // Celda de Grupo VACÍA
        table.addCell(celdaConBorde(String.valueOf(granTotalHombres), TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); 
        table.addCell(celdaConBorde(String.valueOf(granTotalMujeres), TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); 
        table.addCell(celdaConBorde(String.valueOf(granTotalAlumnos), TextAlignment.CENTER, true, FONT_SIZE_PEQUEÑO)); 

        return table;
    }
    
    // =========================================================================
    // --- FIN DEL MÉTODO MODIFICADO ---
    // =========================================================================
    
    private Table crearTablaFirmas() {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
        table.useAllAvailableWidth();

        table.addCell(celdaSinBorde("SUPERVISOR (A)", TextAlignment.CENTER, true, FONT_SIZE_NORMAL));
        table.addCell(celdaSinBorde("DIRECTOR (A)", TextAlignment.CENTER, true, FONT_SIZE_NORMAL));

        table.addCell(celdaSinBorde("\n\n\n_________________________", TextAlignment.CENTER, false, FONT_SIZE_NORMAL));
        table.addCell(celdaSinBorde("\n\n\n_________________________", TextAlignment.CENTER, false, FONT_SIZE_NORMAL));

        table.addCell(celdaSinBorde("", TextAlignment.CENTER, false, FONT_SIZE_NORMAL)); 
        table.addCell(celdaSinBorde("CAROLINA ASTUDILLO HERNANDEZ", TextAlignment.CENTER, false, FONT_SIZE_NORMAL));

        return table;
    }

    private Table crearTablaPieDePagina() {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
        table.useAllAvailableWidth();
        table.addCell(celdaSinBorde("Fecha de Impresion: Feb 15, 2023, 9:10 am", TextAlignment.LEFT, false, FONT_SIZE_PEQUEÑO));
        table.addCell(celdaSinBorde("Pag. 1/1", TextAlignment.RIGHT, false, FONT_SIZE_PEQUEÑO));
        return table;
    }
}