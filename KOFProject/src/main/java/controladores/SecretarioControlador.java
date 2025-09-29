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
        MiModeloTabla.addColumn("Usuario");
        MiModeloTabla.addColumn("Clave");
        
        Secretarios_tabla.setModel(MiModeloTabla);
        
        String[] datos = new String[5];
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
                
                MiModeloTabla.addRow(datos);
            }
            
            Secretarios_tabla.setModel(MiModeloTabla);
        }
        catch(SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "No se pudo mostrar: " + ex.toString());
        }
    }   
    
    public void SeleccionarSecretario(JTextField paramID, JTextField paramNombre, JTextField paramApellido, JTextField paramUsuario, JTextField paramClave, JTable paramTabla)
    {
        try
        {
            int fila = paramTabla.getSelectedRow();
            
            if (fila >= 0)
            {
                paramID.setText(paramTabla.getValueAt(fila, 0).toString());
                paramNombre.setText(paramTabla.getValueAt(fila, 1).toString());
                paramApellido.setText(paramTabla.getValueAt(fila, 2).toString());
                paramUsuario.setText(paramTabla.getValueAt(fila, 3).toString());
                paramClave.setText(paramTabla.getValueAt(fila, 4).toString());
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
    
    public boolean ValidarDatos(Connection conexionExistente, JTextField paramNombre, JTextField paramApellido, JTextField paramUsuario)
    {
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
        
        //Usuarios----------------------------------------------------------------------------
        if(!(paramUsuario.getText() != null && paramUsuario.getText().matches("^((?!director).)*$")))
        {
            JOptionPane.showMessageDialog(null, "Nombre de usuario inválido");
            return false;
        }
        
        return true;
    }
    
    public void MaximoSecretario(Connection conexionExistente, JTextField paramNombre, JTextField paramApellido, JTextField paramUsuario, JTextField paramClave)
    {   
        int contador = 0;
        
        String SQL = "SELECT * FROM secretarios;";
        
        String[] datos = new String[5];
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
                
                contador ++;
            }
            
            if(contador < 3)
            {
                this.InsertarSecretario(conexionExistente, paramNombre, paramApellido, paramUsuario, paramClave);
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
    
    public void InsertarSecretario(Connection conexionExistente, JTextField paramNombre, JTextField paramApellido, JTextField paramUsuario, JTextField paramClave)
    {  
        if(this.ValidarDatos(conexionExistente, paramNombre, paramApellido, paramUsuario))
        {
            Secretario ObjSecretario = new Secretario();
            ObjSecretario.setNombre(paramNombre.getText());
            ObjSecretario.setApellido(paramApellido.getText());
            ObjSecretario.setUsuario(paramUsuario.getText());
            ObjSecretario.setClave(paramClave.getText());

            //Conexion ObjConexion = new Conexion();

            String Consulta = "INSERT INTO secretarios (nombre, apellido, usuario, clave) VALUES (?, ?, ?, ?);";

            try
            {
                CallableStatement cs = conexionExistente.prepareCall(Consulta);

                cs.setString(1, ObjSecretario.getNombre());
                cs.setString(2, ObjSecretario.getApellido());
                cs.setString(3, ObjSecretario.getUsuario());
                cs.setString(4, ObjSecretario.getClave());

                cs.execute();

                this.GestionarSecretario(conexionExistente);

                JOptionPane.showMessageDialog(null, "Se inserto correctamente");
            }
            catch (HeadlessException | SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Ha ocurrido un error: " + ex.toString());
            }
        }
    }
    
    public void ModificarSecretario(Connection conexionExistente, JTextField paramID, JTextField paramNombre, JTextField paramApellido, JTextField paramUsuario, JTextField paramClave)
    {
        if(this.ValidarDatos(conexionExistente, paramNombre, paramApellido, paramUsuario))
        {
            Secretario ObjSecretario = new Secretario();
            ObjSecretario.setNombre(paramNombre.getText());
            ObjSecretario.setApellido(paramApellido.getText());
            ObjSecretario.setUsuario(paramUsuario.getText());
            ObjSecretario.setClave(paramClave.getText());
            ObjSecretario.setId(Integer.parseInt(paramID.getText()));

            //Conexion ObjConexion = new Conexion();

            String Consulta = "UPDATE secretarios SET nombre =?, apellido =?, usuario =?, clave=? WHERE id_secretario =?;";

            try
            {
                CallableStatement cs = conexionExistente.prepareCall(Consulta);

                cs.setString(1, ObjSecretario.getNombre());
                cs.setString(2, ObjSecretario.getApellido());
                cs.setString(3, ObjSecretario.getUsuario());
                cs.setString(4, ObjSecretario.getClave());
                cs.setInt(5, ObjSecretario.getId());

                cs.execute();

                this.GestionarSecretario(conexionExistente);

                JOptionPane.showMessageDialog(null, "Modificacion exitosa");
            }
            catch(SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Error al modificar: " + ex.toString());
            }
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
                    
                    this.GestionarSecretario(conexionExistente);

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
    
    public void GestionarSecretario(Connection conexionExistente)
    {
        String Consulta = "{CALL gestionar_secretarios()}";

        try
        {
            CallableStatement cs = conexionExistente.prepareCall(Consulta);
            cs.execute();

            JOptionPane.showMessageDialog(null, "Operación exitosa");
        }
        catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error: " + ex.toString());
        }    
    }
}