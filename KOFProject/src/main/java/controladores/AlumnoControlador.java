/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import java.awt.HeadlessException;
import java.sql.CallableStatement;
import java.sql.Connection;
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
        MiModeloTabla.addColumn("Nombre");
        MiModeloTabla.addColumn("Apellido");
        MiModeloTabla.addColumn("Edad");
        MiModeloTabla.addColumn("Genero");
        MiModeloTabla.addColumn("Domicilio");
        MiModeloTabla.addColumn("Nombre Padre");
        MiModeloTabla.addColumn("Apellido Padre");
        MiModeloTabla.addColumn("Correo Padre");
        MiModeloTabla.addColumn("Telefono Padre");
        MiModeloTabla.addColumn("Grupo");
        MiModeloTabla.addColumn("Grado");
        
        Alumnos_tabla.setModel(MiModeloTabla);
        
        String[] datos = new String[12];
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
                datos[11]=rs.getString(12);
                
                MiModeloTabla.addRow(datos);
            }
            
            Alumnos_tabla.setModel(MiModeloTabla);
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se pudo mostrar: " + ex.toString());
        }
    }
    
    public void SeleccionarAlumno(JTextField paramID, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramDomicilio, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, JTextField paramGrupo, JTextField paramGrado, JTable paramTabla)
    {
        try
        {
            int fila = paramTabla.getSelectedRow();
            
            if (fila >= 0)
            {
                paramID.setText(paramTabla.getValueAt(fila, 0).toString());
                paramNombre.setText(paramTabla.getValueAt(fila, 1).toString());
                paramApellido.setText(paramTabla.getValueAt(fila, 2).toString());
                paramEdad.setText(paramTabla.getValueAt(fila, 3).toString());
                paramGenero.setText(paramTabla.getValueAt(fila, 4).toString());
                paramDomicilio.setText(paramTabla.getValueAt(fila, 5).toString());
                paramNombreP.setText(paramTabla.getValueAt(fila, 6).toString());
                paramApellidoP.setText(paramTabla.getValueAt(fila, 7).toString());
                paramCorreoP.setText(paramTabla.getValueAt(fila, 8).toString());
                paramTelefonoP.setText(paramTabla.getValueAt(fila, 9).toString());
                paramGrupo.setText(paramTabla.getValueAt(fila, 10).toString());
                paramGrado.setText(paramTabla.getValueAt(fila, 11).toString());
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
    
    public boolean ValidarDatos(Connection conexionExistente, JTextField paramEdad, JTextField paramGrado, JTextField paramCorreoP, JTextField paramTelefonoP)
    {
        int edad = Integer.parseInt(paramEdad.getText());
        int grado = Integer.parseInt(paramGrado.getText());

        int edad_minimo = grado + 5;
        int edad_maximo = grado + 6;
        
        String SQL = "SELECT correo_padre, telefono_padre FROM alumnos;";
        
        String[] datos = new String[2];
        Statement st;
        

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
        
        return true;
    }
    
    public void InsertarAlumno(Connection conexionExistente, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramDomicilio, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, JTextField paramGrupo, JTextField paramGrado)
    {
        if(this.ValidarDatos(conexionExistente, paramEdad, paramGrado, paramCorreoP, paramTelefonoP) == true)
        {
            Alumno ObjAlumno = new Alumno();
            ObjAlumno.setNombre(paramNombre.getText());
            ObjAlumno.setApellido(paramApellido.getText());
            ObjAlumno.setEdad(Integer.parseInt(paramEdad.getText()));
            ObjAlumno.setGenero(paramGenero.getText());
            ObjAlumno.setDomicilio(paramDomicilio.getText());
            ObjAlumno.setNombre_padre(paramNombreP.getText());
            ObjAlumno.setApellido_padre(paramApellidoP.getText());
            ObjAlumno.setCorreo_padre(paramCorreoP.getText());
            ObjAlumno.setTelefono_padre(paramTelefonoP.getText());
            ObjAlumno.setGrupo(paramGrupo.getText());
            ObjAlumno.setGrado(Integer.parseInt(paramGrado.getText()));

            //Conexion ObjConexion = new Conexion();

            String Consulta = "INSERT INTO alumnos (nombre, apellido, edad, genero, domicilio, nombre_padre, apellido_padre, correo_padre, telefono_padre, grupo, grado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

            try
            {
                CallableStatement cs = conexionExistente.prepareCall(Consulta);

                cs.setString(1, ObjAlumno.getNombre());
                cs.setString(2, ObjAlumno.getApellido());
                cs.setInt(3, ObjAlumno.getEdad());
                cs.setString(4, ObjAlumno.getGenero());
                cs.setString(5, ObjAlumno.getDomicilio());
                cs.setString(6, ObjAlumno.getNombre_padre());
                cs.setString(7, ObjAlumno.getApellido_padre());
                cs.setString(8, ObjAlumno.getCorreo_padre());
                cs.setString(9, ObjAlumno.getTelefono_padre());
                cs.setString(10, ObjAlumno.getGrupo());
                cs.setInt(11, ObjAlumno.getGrado());

                cs.execute();

                JOptionPane.showMessageDialog(null, "Se insertó correctamente");
            }
            catch (HeadlessException | SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Ha ocurrido un error" );
            }
        }
    }
    
    public void ModificarAlumno(Connection conexionExistente, JTextField paramID, JTextField paramNombre, JTextField paramApellido, JTextField paramEdad, JTextField paramGenero, JTextField paramDomicilio, JTextField paramNombreP, JTextField paramApellidoP, JTextField paramCorreoP, JTextField paramTelefonoP, JTextField paramGrupo, JTextField paramGrado)
    {
        if(this.ValidarDatos(conexionExistente, paramEdad, paramGrado, paramCorreoP, paramTelefonoP) == true)
        {
            Alumno ObjAlumno = new Alumno();
            ObjAlumno.setId(Integer.parseInt(paramID.getText()));
            ObjAlumno.setNombre(paramNombre.getText());
            ObjAlumno.setApellido(paramApellido.getText());
            ObjAlumno.setEdad(Integer.parseInt(paramEdad.getText()));
            ObjAlumno.setGenero(paramGenero.getText());
            ObjAlumno.setDomicilio(paramDomicilio.getText());
            ObjAlumno.setNombre_padre(paramNombreP.getText());
            ObjAlumno.setApellido_padre(paramApellidoP.getText());
            ObjAlumno.setCorreo_padre(paramCorreoP.getText());
            ObjAlumno.setTelefono_padre(paramTelefonoP.getText());
            ObjAlumno.setGrupo(paramGrupo.getText());
            ObjAlumno.setGrado(Integer.parseInt(paramGrado.getText()));

            //Conexion ObjConexion = new Conexion();

            String Consulta = "UPDATE alumnos SET nombre =?, apellido =?, edad =?, genero =?, domicilio =?, nombre_padre =?, apellido_padre =?, correo_padre =?, telefono_padre =?, grupo =?, grado =? WHERE id_alumno =?;";

            try
            {
                CallableStatement cs = conexionExistente.prepareCall(Consulta);

                cs.setString(1, ObjAlumno.getNombre());
                cs.setString(2, ObjAlumno.getApellido());
                cs.setInt(3, ObjAlumno.getEdad());
                cs.setString(4, ObjAlumno.getGenero());
                cs.setString(5, ObjAlumno.getDomicilio());
                cs.setString(6, ObjAlumno.getNombre_padre());
                cs.setString(7, ObjAlumno.getApellido_padre());
                cs.setString(8, ObjAlumno.getCorreo_padre());
                cs.setString(9, ObjAlumno.getTelefono_padre());
                cs.setString(10, ObjAlumno.getGrupo());
                cs.setInt(11, ObjAlumno.getGrado());
                cs.setInt(12, ObjAlumno.getId());

                cs.execute();

                JOptionPane.showMessageDialog(null, "Modificación exitosa");
            }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Error al modificar");
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
