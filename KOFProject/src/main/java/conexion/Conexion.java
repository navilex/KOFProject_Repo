/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // --- NUEVO ---
import java.sql.ResultSet;          // --- NUEVO ---
import java.sql.Statement;        // --- NUEVO ---
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Ivan De la Rosa
 */
public class Conexion {

    private static Connection conexion;

    public String user; // Este debe ser el 'usuario' de la app (ej. 'agarcia')
    public String password;
    boolean bandera = false;

    public void ObtenerUsuario(String Usuario, String Clave) {
        user = Usuario;
        password = Clave;
    }

    public String RegresarUsuario() {
        return user;
    }

    public Connection IniciarConexion() {
        try {
            Class.forName("org.postgresql.Driver");

            // 1. Se establece la conexión con la BD
            // (Asume que 'user' y 'password' son los de la BD, 
            // que coinciden con los de la app)
            conexion = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Calificaciones_alumnos", user, password);
            bandera = true;
            
            // --- INICIO DE LA MODIFICACIÓN ---
            
            // 2. Una vez conectados, buscamos el ID del secretario
            //    Usamos 'user' (ej. 'agarcia') para encontrar su 'id_secretario'
            int idSecretarioApp = -1; // Valor por defecto si no se encuentra
            String queryId = "SELECT id_secretario FROM secretarios WHERE usuario = ?";

            try (PreparedStatement pstmt = conexion.prepareStatement(queryId)) {
                pstmt.setString(1, user); // 'user' es el campo de la clase
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        idSecretarioApp = rs.getInt("id_secretario");
                    }
                }
            } // PreparedStatement y ResultSet se cierran solos aquí

            // 3. Si encontramos un ID válido, lo configuramos en la sesión de PG
            if (idSecretarioApp != -1) {
                try (Statement stmt = conexion.createStatement()) {
                    // Esta es la línea clave para tu bitácora
                    stmt.execute("SET myapp.current_user_id = '" + idSecretarioApp + "'");
                    
                    // Opcional: imprimir en consola para depurar
                    System.out.println("Sesión de BD configurada para id_secretario: " + idSecretarioApp);
                }
            } else {
                // El usuario de BD se conectó, pero no existe en la tabla 'secretarios'
                // Esto podría ser un problema, pero la conexión sigue activa.
                System.err.println("Advertencia: Usuario '" + user + "' autenticado pero no encontrado en la tabla 'secretarios'.");
            }

            // --- FIN DE LA MODIFICACIÓN ---

            //JOptionPane.showMessageDialog(null, "Se pudo realizar la conexion");
            
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Error: No se encontro el driver de PostgreSQL: " + ex.toString());
        } catch (SQLException ex) {
            //JOptionPane.showMessageDialog(null, "Error al conectarse: " + ex.getMessage() + "\nEstado: " + ex.getSQLState() + "\nCodigo Error: " + ex.getErrorCode() +"\nContacte con el administrador");

            String sqlState = ex.getSQLState();

            if ("08001".equals(sqlState) || "08006".equals(sqlState)) {
                JOptionPane.showMessageDialog(null, "Error al conectarse, no se pudo conectar al servidor");
            } else if ("28000".equals(sqlState) || "28P01".equals(sqlState)) { // 28P01 es "password authentication failed"
                JOptionPane.showMessageDialog(null, "Credenciales de acceseo inválidas");
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
        }

        return conexion;
    }

    public boolean ValidarConexion() {
        return bandera;
    }
}