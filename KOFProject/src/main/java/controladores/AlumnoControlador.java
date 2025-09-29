/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import java.awt.HeadlessException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelos.Alumno;
import modelos.Secretario;

/**
 *
 * @author Ivan De la Rosa
 */
public class AlumnoControlador 
{
    public void MostrarAlumno(Connection conexionExistente, JTable Alumnos_tabla)
    {
        //Conexion ObjConexion = new Conexion();
        DefaultTableModel MiModeloTabla = new DefaultTableModel();
        //TableRowSorter<TableModel> OrdenarTabla = new TableRowSorter();
        
        //TablaAlumnos.setRowSorter(OrdenarTabla);
        
        String SQL = "SELECT * FROM alumnos;";
        
        MiModeloTabla.addColumn("ID");
        MiModeloTabla.addColumn("CURP");
        MiModeloTabla.addColumn("Nombre");
        MiModeloTabla.addColumn("Apellido");
        MiModeloTabla.addColumn("Edad");
        MiModeloTabla.addColumn("Genero");
        MiModeloTabla.addColumn("Nombre Padre");
        MiModeloTabla.addColumn("Apellido Padre");
        MiModeloTabla.addColumn("Correo Padre");
        MiModeloTabla.addColumn("Telefono Padre");
        MiModeloTabla.addColumn("Grupo");
        
        Alumnos_tabla.setModel(MiModeloTabla);
        
        String[] datos = new String[11];
        Statement st;
        
        try
        {
            st = conexionExistente.createStatement();
            ResultSet rs = st.executeQuery(SQL);
            
            while(rs.next())
            {
                datos[0]=rs.getString(1);
                datos[1]=rs.getString(2);
                datos[2]=rs.getString(3);
                datos[3]=rs.getString(4);
                datos[4]=rs.getString(5);
                datos[5]=rs.getString(6);
                datos[6]=rs.getString(7);
                datos[7]=rs.getString(8);
                datos[8]=rs.getString(9);
                datos[9]=rs.getString(10);
                datos[10]=rs.getString(11);
                
                datos[10]=this.CambiarIDPorGrupo(conexionExistente, datos[10]);
                
                MiModeloTabla.addRow(datos);
            }
            
            Alumnos_tabla.setModel(MiModeloTabla);
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se pudo mostrar: " + ex.toString());
        }
    }
    
    public String CambiarIDPorGrupo(Connection conexionExistente, String idGrupo)
    {
        String cambio = "";
        
        String SQL = "SELECT id_grupo, grupo FROM grupos;";
         
        String[] datos = new String[2];
        Statement st;
         
        try
        {
            st = conexionExistente.createStatement();
            ResultSet rs = st.executeQuery(SQL);
            
            while(rs.next())
            {
                datos[0]=rs.getString(1);
                datos[1]=rs.getString(2);
                
                if(idGrupo.equals(datos[0]))
                {
                    cambio = datos[1];
                }
            }
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se encontró grupo: " + ex.toString());
        }
        return cambio;
    }
    
    public int CambiarGrupoPorID(Connection conexionExistente, String grupo)
    {
        if(grupo != null && grupo.matches("\\d[A-Z]"))
        {
            int cambio = 0;
        
            String SQL = "SELECT id_grupo, grupo FROM grupos;";

            String[] datos = new String[2];
            Statement st;

            try
            {
                st = conexionExistente.createStatement();
                ResultSet rs = st.executeQuery(SQL);

                while(rs.next())
                {
                    datos[0]=rs.getString(1);
                    datos[1]=rs.getString(2);

                    if(grupo.equals(datos[1]))
                    {
                        cambio = Integer.parseInt(datos[0]);
                    }
                }
            }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "No se encontró grupo: " + ex.toString());
            }
            return cambio;
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Para un grupo solo use combinaciones de un número y una mayúscula \nEjemplo: 1A" );
            return 0;
        }
    }
    
    public void SeleccionarAlumno(JTextField paramID, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, JTextField paramGrupo, JTable paramTabla)
    {
        try
        {
            int fila = paramTabla.getSelectedRow();
            
            if (fila >= 0)
            {
                paramID.setText(paramTabla.getValueAt(fila, 0).toString());
                paramCURP.setText(paramTabla.getValueAt(fila, 1).toString());
                paramNombre.setText(paramTabla.getValueAt(fila, 2).toString());
                paramApellido.setText(paramTabla.getValueAt(fila, 3).toString());
                paramEdad.setText(paramTabla.getValueAt(fila, 4).toString());
                paramGenero.setText(paramTabla.getValueAt(fila, 5).toString());
                paramNombreP.setText(paramTabla.getValueAt(fila, 6).toString());
                paramApellidoP.setText(paramTabla.getValueAt(fila, 7).toString());
                paramCorreoP.setText(paramTabla.getValueAt(fila, 8).toString());
                paramTelefonoP.setText(paramTabla.getValueAt(fila, 9).toString());
                paramGrupo.setText(paramTabla.getValueAt(fila, 10).toString());
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Fila no seleccionada");
            }
        }
        catch(HeadlessException ex)
        {
            JOptionPane.showMessageDialog(null, "No se pudo selecionar: " + ex.toString());
        }
    }
    
    public boolean ValidarDatos(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, int idGrupo)
    {
        //CURP------------------------------------------------------------------------------
        if(!(paramCURP.getText() != null && paramCURP.getText().matches("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[0-9]{2}$")))
        {
            JOptionPane.showMessageDialog(null, "Para registrar una CURP se debe seguir una secuencia en la misma: \n \n"
                    + "1. Las primeras cuatro posiciones deben ser letras mayúsculas.\n"
                    + "2. Le siguen seis dígitos que corresponden a la fecha de nacimiento (AA MM DD).\n"
                    + "3. El séptimo caracter después de la fecha indica el sexo (H para Hombre, M para Mujer).\n"
                    + "4. Le siguen cinco letras mayúsculas que corresponden a la entidad de nacimiento y los apellidos.\n"
                    + "5. Los dos últimos caracteres son una homoclave (dos dígitos).");
            return false;
        }
        
        //Nombres----------------------------------------------------------------------------
        if(!(paramNombre.getText() != null && paramNombre.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]+")))
        {
            JOptionPane.showMessageDialog(null, "Los nombres solo pueden contener letras");
            return false;
        }
        
        //Apellidos---------------------------------------------------------------------------
        if(!(paramApellido.getText() != null && paramApellido.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]+")))
        {
            JOptionPane.showMessageDialog(null, "Los apellidos solo pueden contener letras");
            return false;
        }
        
        //Edad--------------------------------------------------------------------------------
        if(paramEdad.getText() != null && paramEdad.getText().matches("\\d{1,2}"))
        {
            int edad = Integer.parseInt(paramEdad.getText());
            int grado = ObtenerGrado(conexionExistente, idGrupo);

            if (grado == -1) 
            {
                JOptionPane.showMessageDialog(null, "Error: No se encontró el grado para el grupo seleccionado.");
                return false;
            }

            int edad_minimo = grado + 5;
            int edad_maximo = grado + 6;

            if (grado >= 1 && grado <= 6) 
            {
                if (edad < edad_minimo || edad > edad_maximo) 
                {
                    JOptionPane.showMessageDialog(null, "Un alumno de " + grado + "° año no puede tener menos de " + edad_minimo + " años ni más de " + edad_maximo + " años");
                    return false;
                }
            } 
            else 
            {
                JOptionPane.showMessageDialog(null, "No hay grupos mayores de 6 ni menores que 1");
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "La edad solo contiene 2 números enteros como máximo");
            return false;
        }
        
        //Género--------------------------------------------------------------------------------
        if(!(paramGenero.getText() != null && paramGenero.getText().matches("[a-zA-Z]+")))
        {
            JOptionPane.showMessageDialog(null, "El género es Masculino o Femenino");
            return false;
        }
        
        //Nombre Padre-------------------------------------------------------------------------------
        if(!(paramNombreP.getText() != null && paramNombreP.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]+")))
        {
            JOptionPane.showMessageDialog(null, "Los nombres solo pueden contener letras");
            return false;
        }
        
        //Apellido Padre-------------------------------------------------------------------------------
        if(!(paramApellidoP.getText() != null && paramApellidoP.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]+")))
        {
            JOptionPane.showMessageDialog(null, "Los apellidos solo pueden contener letras");
            return false;
        }
        
        //Correo y teléfono padre-----------------------------------------------------------------------
        if((paramCorreoP.getText() != null && paramCorreoP.getText().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) && (paramTelefonoP.getText() != null && paramTelefonoP.getText().matches("\\d+")))
        {
            //Validacion de duplicados
            String correo = paramCorreoP.getText();
            String telefono = paramTelefonoP.getText();
            String SQL_padre = "SELECT COUNT(*) FROM alumnos WHERE correo_padre = ? OR telefono_padre = ?;";

            try (var ps = conexionExistente.prepareStatement(SQL_padre)) 
            {
                ps.setString(1, correo);
                ps.setString(2, telefono);
                try (var rs = ps.executeQuery()) 
                {
                    if (rs.next() && rs.getInt(1) > 0) 
                    {
                        JOptionPane.showMessageDialog(null, "No puede haber correos o teléfonos repetidos.");
                        return false;
                    }
                }
            }
            
            catch (SQLException e) 
            {
                JOptionPane.showMessageDialog(null, "Error al validar los datos: " + e.getMessage());
                return false;
            }
            
            /*
            String SQL = "SELECT correo_padre, telefono_padre FROM alumnos;";

            String[] datos = new String[2];
            Statement st;

            try
            {
                st = conexionExistente.createStatement();
                ResultSet rs = st.executeQuery(SQL);

                while(rs.next())
                {
                    datos[0]=rs.getString(1);
                    datos[1]=rs.getString(2);  

                    if(paramCorreoP.getText().equals(datos[0]) || paramTelefonoP.getText().equals(datos[1]))
                    {
                        JOptionPane.showMessageDialog(null, "No puede haber correos o teléfonos repetidos");
                        return false;
                    }
                }
            }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "No se pudo mostrar: " + ex.toString());
            }
            */
        }
        else
        {
            JOptionPane.showMessageDialog(null, "El correo o el teléfono están mal escritos");
        }
        return true;
    }
    
    public int ObtenerGrado(Connection conexion, int idGrupo) 
    {
        String sql = "SELECT grado FROM grupos WHERE id_grupo = ?;";
        
        try (var ps = conexion.prepareStatement(sql)) 
        {
            ps.setInt(1, idGrupo);
            try (var rs = ps.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return rs.getInt("grado");
                }
            }
        }
        catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se encontró el ID del grupo.");
        }
        return -1; // Retorna -1 si no se encuentra el grupo.
    }
    
    public void FiltrarAlumno(Connection conexionExistente, JTable Alumnos_tabla, JTextField paramGrupo) {
        
        DefaultTableModel MiModeloTabla = new DefaultTableModel();
        
        MiModeloTabla.addColumn("ID");
        MiModeloTabla.addColumn("CURP");
        MiModeloTabla.addColumn("Nombre");
        MiModeloTabla.addColumn("Apellido");
        MiModeloTabla.addColumn("Edad");
        MiModeloTabla.addColumn("Genero");
        MiModeloTabla.addColumn("Nombre Padre");
        MiModeloTabla.addColumn("Apellido Padre");
        MiModeloTabla.addColumn("Correo Padre");
        MiModeloTabla.addColumn("Telefono Padre");
        MiModeloTabla.addColumn("Grupo");
        
        Alumnos_tabla.setModel(MiModeloTabla);

        String SQL = "SELECT * FROM alumnos WHERE id_grupo = ?;";

        try 
        {
            int idGrupo = this.CambiarGrupoPorID(conexionExistente, paramGrupo.getText());

            try (PreparedStatement ps = conexionExistente.prepareStatement(SQL)) 
            {
                // Asigna el valor del parámetro al placeholder
                ps.setInt(1, idGrupo);
                
                try (ResultSet rs = ps.executeQuery()) 
                {
                    String[] datos = new String[11];
                    while (rs.next()) {
                        // Obtiene los datos de cada columna por su nombre, es una práctica más segura
                        // y legible que usar índices numéricos.
                        datos[0] = rs.getString("id_alumno");
                        datos[1] = rs.getString("curp");
                        datos[2] = rs.getString("nombre");
                        datos[3] = rs.getString("apellido");
                        datos[4] = rs.getString("edad");
                        datos[5] = rs.getString("genero");
                        datos[6] = rs.getString("nombre_padre");
                        datos[7] = rs.getString("apellido_padre");
                        datos[8] = rs.getString("correo_padre");
                        datos[9] = rs.getString("telefono_padre");
                        datos[10] = rs.getString("id_grupo");

                        datos[10] = this.CambiarIDPorGrupo(conexionExistente, datos[10]);
                        
                        MiModeloTabla.addRow(datos);
                    }
                }
            }
        } 
        catch (SQLException ex) 
        {
            JOptionPane.showMessageDialog(null, "Error SQL al filtrar alumnos: " + ex.toString());
        } 
        catch (NumberFormatException ex) 
        {
            JOptionPane.showMessageDialog(null, "Error en el formato del grupo. " + ex.toString());
        }
    }
    
    public void InsertarAlumno(Connection conexionExistente, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, JTextField paramGrupo)
    {
        int idGrupo = this.CambiarGrupoPorID(conexionExistente, paramGrupo.getText());
        
        if(this.ValidarDatos(conexionExistente, paramCURP, paramNombre, paramApellido, paramEdad, paramGenero, paramNombreP, paramApellidoP, paramCorreoP, paramTelefonoP, idGrupo) == true)
        {
            Alumno ObjAlumno = new Alumno();
            ObjAlumno.setCURP(paramCURP.getText());
            ObjAlumno.setNombre(paramNombre.getText());
            ObjAlumno.setApellido(paramApellido.getText());
            ObjAlumno.setEdad(Integer.parseInt(paramEdad.getText()));
            ObjAlumno.setGenero(paramGenero.getText());
            ObjAlumno.setNombre_padre(paramNombreP.getText());
            ObjAlumno.setApellido_padre(paramApellidoP.getText());
            ObjAlumno.setCorreo_padre(paramCorreoP.getText());
            ObjAlumno.setTelefono_padre(paramTelefonoP.getText());
            ObjAlumno.setGrupo(this.CambiarGrupoPorID(conexionExistente, paramGrupo.getText()));

            //Conexion ObjConexion = new Conexion();

            String Consulta = "INSERT INTO alumnos (curp, nombre, apellido, edad, genero, nombre_padre, apellido_padre, correo_padre, telefono_padre, id_grupo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

            try
            {
                CallableStatement cs = conexionExistente.prepareCall(Consulta);

                cs.setString(1, ObjAlumno.getCURP());
                cs.setString(2, ObjAlumno.getNombre());
                cs.setString(3, ObjAlumno.getApellido());
                cs.setInt(4, ObjAlumno.getEdad());
                cs.setString(5, ObjAlumno.getGenero());
                cs.setString(6, ObjAlumno.getNombre_padre());
                cs.setString(7, ObjAlumno.getApellido_padre());
                cs.setString(8, ObjAlumno.getCorreo_padre());
                cs.setString(9, ObjAlumno.getTelefono_padre());
                cs.setInt(10, ObjAlumno.getGrupo());

                cs.execute();

                JOptionPane.showMessageDialog(null, "Se insertó correctamente");
            }
            catch (HeadlessException | SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Ha ocurrido un error, revisa los datos" );
            }
        }
    }
    
    public void ModificarAlumno(Connection conexionExistente, JTextField paramID, JTextField paramCURP, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, JTextField paramGrupo)
    {
        //int idGrupo = Integer.parseInt(paramGrupo.getText());
        
        //if(this.ValidarDatos(conexionExistente, paramEdad, idGrupo, paramCorreoP, paramTelefonoP) == true)
        {
            Alumno ObjAlumno = new Alumno();
            ObjAlumno.setId(Integer.parseInt(paramID.getText()));
            ObjAlumno.setCURP(paramCURP.getText());
            ObjAlumno.setNombre(paramNombre.getText());
            ObjAlumno.setApellido(paramApellido.getText());
            ObjAlumno.setEdad(Integer.parseInt(paramEdad.getText()));
            ObjAlumno.setGenero(paramGenero.getText());
            ObjAlumno.setNombre_padre(paramNombreP.getText());
            ObjAlumno.setApellido_padre(paramApellidoP.getText());
            ObjAlumno.setCorreo_padre(paramCorreoP.getText());
            ObjAlumno.setTelefono_padre(paramTelefonoP.getText());
            ObjAlumno.setGrupo(this.CambiarGrupoPorID(conexionExistente, paramGrupo.getText()));

            //Conexion ObjConexion = new Conexion();

            String Consulta = "UPDATE alumnos SET curp =?, nombre =?, apellido =?, edad =?, genero =?, nombre_padre =?, apellido_padre =?, correo_padre =?, telefono_padre =?, id_grupo =?  WHERE id_alumno =?;";

            try
            {
                CallableStatement cs = conexionExistente.prepareCall(Consulta);
                
                cs.setString(1, ObjAlumno.getCURP());
                cs.setString(2, ObjAlumno.getNombre());
                cs.setString(3, ObjAlumno.getApellido());
                cs.setInt(4, ObjAlumno.getEdad());
                cs.setString(5, ObjAlumno.getGenero());
                cs.setString(6, ObjAlumno.getNombre_padre());
                cs.setString(7, ObjAlumno.getApellido_padre());
                cs.setString(8, ObjAlumno.getCorreo_padre());
                cs.setString(9, ObjAlumno.getTelefono_padre());
                cs.setInt(10, ObjAlumno.getGrupo());
                cs.setInt(11, ObjAlumno.getId());

                cs.execute();

                JOptionPane.showMessageDialog(null, "Modificación exitosa");
            }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Error al modificar, revisa los datos");
            }
        }
    }
    
    public void EliminarAlumno(Connection conexionExistente, JTextField paramID)
    {
        Secretario ObjSecretario = new Secretario();
        ObjSecretario.setId(Integer.parseInt(paramID.getText()));
        
        //Conexion ObjConexion = new Conexion();
        
        String Consulta = "DELETE FROM alumnos WHERE id_alumno =?;";
                
        int respuesta = JOptionPane.showConfirmDialog(null, "¿Deseas confirmar la eliminacion?", "Confirmacion requerida", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        
        switch (respuesta) 
        {
            case JOptionPane.OK_OPTION:
                
                try
                {
                    CallableStatement cs = conexionExistente.prepareCall(Consulta);

                    cs.setInt(1, ObjSecretario.getId());
                    cs.execute();

                    JOptionPane.showMessageDialog(null, "Registro borrado");
                }
                catch(SQLException ex)
                {
                    JOptionPane.showMessageDialog(null, "Error al borrar: " + ex.toString());
                }
        
                break;
                
            case JOptionPane.CANCEL_OPTION:
                break;
            case JOptionPane.CLOSED_OPTION:
                break;
            default:
                break;
        }
    }
}
