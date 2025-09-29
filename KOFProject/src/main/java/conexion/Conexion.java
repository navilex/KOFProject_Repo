/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Ivan De la Rosa
 */
public class Conexion 
{
    private static Connection conexion;
    
    public String user;
    public String password;
    boolean bandera = false;
    
    public void ObtenerUsuario(String Usuario, String Clave)
    {
        user = Usuario;
        password = Clave;
    }
    
    public String RegresarUsuario()
    {
        return user;
    }
    
    public Connection IniciarConexion()
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            
            conexion = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Calificaciones_alumnos", user, password);
            bandera = true;
            //JOptionPane.showMessageDialog(null, "Se pudo realizar la conexion");
        }
        catch (ClassNotFoundException ex) 
        {
            JOptionPane.showMessageDialog(null, "Error: No se encontro el driver de PostgreSQL: " + ex.toString());
        }
        catch(SQLException ex)
        {
            //JOptionPane.showMessageDialog(null, "Error al conectarse: " + ex.getMessage() + "\nEstado: " + ex.getSQLState() + "\nCodigo Error: " + ex.getErrorCode() +"\nContacte con el administrador");
            
            String sqlState = ex.getSQLState();
            
            if("08001".equals(sqlState) || "08006".equals(sqlState))
            {
                JOptionPane.showMessageDialog(null, "Error al conectarse, no se pudo conectar al servidor");
            }
            else if("28000".equals(sqlState))
            {
                JOptionPane.showMessageDialog(null, "Credenciales de acceseo inválidas");
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        }
        
        return conexion;
    }
    
    public boolean ValidarConexion()
    {
        return bandera;
    }
}
