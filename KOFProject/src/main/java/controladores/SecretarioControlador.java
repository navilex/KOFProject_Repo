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

import modelos.Secretario;

/**
 *
 * @author Ivan De la Rosa
 */
public class SecretarioControlador 
{
    public void MostrarSecretario(Connection conexionExistente, JTable Secretarios_tabla)
    {
        //Conexion ObjConexion = new Conexion();
        DefaultTableModel MiModeloTabla = new DefaultTableModel();
        //TableRowSorter<TableModel> OrdenarTabla = new TableRowSorter();
        
        //TablaAlumnos.setRowSorter(OrdenarTabla);
        
        String SQL = "SELECT * FROM secretarios;";
        
        MiModeloTabla.addColumn("ID");
        MiModeloTabla.addColumn("Nombre");
        MiModeloTabla.addColumn("Apellido");
        MiModeloTabla.addColumn("Correo");
        MiModeloTabla.addColumn("Teléfono");
        MiModeloTabla.addColumn("Domicilio");
        MiModeloTabla.addColumn("Usuario");
        MiModeloTabla.addColumn("Clave");
        
        Secretarios_tabla.setModel(MiModeloTabla);
        
        String[] datos = new String[8];
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
                
                MiModeloTabla.addRow(datos);
            }
            
            Secretarios_tabla.setModel(MiModeloTabla);
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se pudo mostrar: " + ex.toString());
        }
    }   
    
    public void SeleccionarSecretario(JTextField paramID, JTextField paramNombre, JTextField paramApellido, JTextField paramCorreo, JTextField paramTelefono, JTextField paramDomicilio, JTextField paramUsuario, JTextField paramClave, JTable paramTabla)
    {
        try
        {
            int fila = paramTabla.getSelectedRow();
            
            if (fila >= 0)
            {
                paramID.setText(paramTabla.getValueAt(fila, 0).toString());
                paramNombre.setText(paramTabla.getValueAt(fila, 1).toString());
                paramApellido.setText(paramTabla.getValueAt(fila, 2).toString());
                paramCorreo.setText(paramTabla.getValueAt(fila, 3).toString());
                paramTelefono.setText(paramTabla.getValueAt(fila, 4).toString());
                paramDomicilio.setText(paramTabla.getValueAt(fila, 5).toString());
                paramUsuario.setText(paramTabla.getValueAt(fila, 6).toString());
                paramClave.setText(paramTabla.getValueAt(fila, 7).toString());
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
    
    public void MaximoSecretario(Connection conexionExistente, JTextField paramNombre, JTextField paramApellido, JTextField paramCorreo, JTextField paramTelefono, JTextField paramDomicilio, JTextField paramUsuario, JTextField paramClave)
    {   
        int contador = 0;
        
        String SQL = "SELECT * FROM secretarios;";
        
        String[] datos = new String[8];
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
                
                contador ++;
            }
            
            if(contador < 3)
            {
                this.InsertarSecretario(conexionExistente, paramNombre, paramApellido, paramCorreo, paramTelefono, paramDomicilio, paramUsuario, paramClave);
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Solo puede insertar 3 secretarios como máximo");
            }
            
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se pudo mostrar: " + ex.toString());
        }
    }   
    
    public void InsertarSecretario(Connection conexionExistente, JTextField paramNombre, JTextField paramApellido, JTextField paramCorreo, JTextField paramTelefono, JTextField paramDomicilio, JTextField paramUsuario, JTextField paramClave)
    {        
        Secretario ObjSecretario = new Secretario();
        ObjSecretario.setNombre(paramNombre.getText());
        ObjSecretario.setApellido(paramApellido.getText());
        ObjSecretario.setCorreo(paramCorreo.getText());
        ObjSecretario.setTelefono(paramTelefono.getText());
        ObjSecretario.setDomicilio(paramDomicilio.getText());
        ObjSecretario.setUsuario(paramUsuario.getText());
        ObjSecretario.setClave(paramClave.getText());
        
        //Conexion ObjConexion = new Conexion();
        
        String Consulta = "INSERT INTO secretarios (nombre, apellido, correo, telefono, domicilio, usuario, clave) VALUES (?, ?, ?, ?, ?, ?, ?);";
        
        try
        {
            CallableStatement cs = conexionExistente.prepareCall(Consulta);
            
            cs.setString(1, ObjSecretario.getNombre());
            cs.setString(2, ObjSecretario.getApellido());
            cs.setString(3, ObjSecretario.getCorreo());
            cs.setString(4, ObjSecretario.getTelefono());
            cs.setString(5, ObjSecretario.getDomicilio());
            cs.setString(6, ObjSecretario.getUsuario());
            cs.setString(7, ObjSecretario.getClave());
            
            cs.execute();
            
            this.RegistrarSecretario(conexionExistente);
            
            JOptionPane.showMessageDialog(null, "Se inserto correctamente");
        }
        catch (HeadlessException | SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error: " + ex.toString());
        }
    }
    
    public void RegistrarSecretario(Connection conexionExistente)
    {
        String Consulta = "{CALL crear_secretarios()}";

        try
        {
            CallableStatement cs = conexionExistente.prepareCall(Consulta);
            cs.execute();

            JOptionPane.showMessageDialog(null, "Se agrego usuario correctamente");
        }
        catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error: " + ex.toString());
        }    
    }
    
    public void ModificarSecretario(Connection conexionExistente, JTextField paramID, JTextField paramNombre, JTextField paramApellido, JTextField paramCorreo, JTextField paramTelefono, JTextField paramDomicilio, JTextField paramUsuario, JTextField paramClave)
    {
        Secretario ObjSecretario = new Secretario();
        ObjSecretario.setNombre(paramNombre.getText());
        ObjSecretario.setApellido(paramApellido.getText());
        ObjSecretario.setCorreo(paramCorreo.getText());
        ObjSecretario.setTelefono(paramTelefono.getText());
        ObjSecretario.setDomicilio(paramDomicilio.getText());
        ObjSecretario.setUsuario(paramUsuario.getText());
        ObjSecretario.setClave(paramClave.getText());
        ObjSecretario.setId(Integer.parseInt(paramID.getText()));
        
        //Conexion ObjConexion = new Conexion();
        
        String Consulta = "UPDATE secretarios SET nombre =?, apellido =?, correo =?, telefono =?, domicilio =?, usuario =?, clave=? WHERE id_secretario =?;";
        
        try
        {
            CallableStatement cs = conexionExistente.prepareCall(Consulta);
            
            cs.setString(1, ObjSecretario.getNombre());
            cs.setString(2, ObjSecretario.getApellido());
            cs.setString(3, ObjSecretario.getCorreo());
            cs.setString(4, ObjSecretario.getTelefono());
            cs.setString(5, ObjSecretario.getDomicilio());
            cs.setString(6, ObjSecretario.getUsuario());
            cs.setString(7, ObjSecretario.getClave());
            cs.setInt(8, ObjSecretario.getId());
            
            cs.execute();
            
            JOptionPane.showMessageDialog(null, "Modificacion exitosa");
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "Error al modificar: " + ex.toString());
        }
    }
    
    public void EliminarSecretario(Connection conexionExistente, JTextField paramID)
    {
        Secretario ObjSecretario = new Secretario();
        ObjSecretario.setId(Integer.parseInt(paramID.getText()));
        
        //Conexion ObjConexion = new Conexion();
        
        String Consulta = "DELETE FROM secretarios WHERE id_secretario =?;";
                
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
