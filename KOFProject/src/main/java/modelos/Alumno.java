/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

import java.sql.Date;

/**
 *
 * @author Ivan De la Rosa
 */
public class Alumno 
{
    int id;
    String CURP;
    String nombre;
    String apellido;
    int edad;
    String genero;
    String nombre_padre;
    String apellido_padre;
    String correo_padre;
    String telefono_padre;
    int grupo;
    Date fecha;

    public int getId() 
    {
        return id;
    }

    public void setId(int id) 
    {
        this.id = id;
    }
    
     public String getCURP() {
        return CURP;
    }

    public void setCURP(String CURP) {
        this.CURP = CURP;
    }

    public String getNombre() 
    {
        return nombre;
    }

    public void setNombre(String nombre) 
    {
        this.nombre = nombre;
    }

    public String getApellido() 
    {
        return apellido;
    }

    public void setApellido(String apellido) 
    {
        this.apellido = apellido;
    }

    public int getEdad() 
    {
        return edad;
    }

    public void setEdad(int edad) 
    {
        this.edad = edad;
    }

    public String getGenero() 
    {
        return genero;
    }

    public void setGenero(String genero) 
    {
        this.genero = genero;
    }

    public String getNombre_padre() 
    {
        return nombre_padre;
    }

    public void setNombre_padre(String nombre_padre) 
    {
        this.nombre_padre = nombre_padre;
    }

    public String getApellido_padre() 
    {
        return apellido_padre;
    }

    public void setApellido_padre(String apellido_padre) 
    {
        this.apellido_padre = apellido_padre;
    }

    public String getCorreo_padre() 
    {
        return correo_padre;
    }

    public void setCorreo_padre(String correo_padre) 
    {
        this.correo_padre = correo_padre;
    }

    public String getTelefono_padre() 
    {
        return telefono_padre;
    }

    public void setTelefono_padre(String telefono_padre) 
    {
        this.telefono_padre = telefono_padre;
    }
    
    public int getGrupo() 
    {
        return grupo;
    }

    public void setGrupo(int grupo) 
    {
        this.grupo = grupo;
    }
    
    public Date getFecha() 
    {
        return fecha;
    }

    public void setFecha(Date fecha) 
    {
        this.fecha = fecha;
    }

}
