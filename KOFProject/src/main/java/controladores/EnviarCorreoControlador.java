/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import java.io.File; // <--- Importado
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler; // <--- Importado
import javax.activation.DataSource; // <--- Importado
import javax.activation.FileDataSource; // <--- Importado
import javax.mail.BodyPart; // <--- Importado
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart; // <--- Importado
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart; // <--- Importado
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart; // <--- Importado
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Ivan De la Rosa
 */
public class EnviarCorreoControlador 
{
    private static String emailFrom = "navi.lex2003@gmail.com";
    private static String passwordFrom = "xakp mvgs qcqt toya";
    private String emailTo;
    private String subject;
    private String content;

    private Properties mProperties;
    private Session mSession;
    private MimeMessage mCorreo;

    public EnviarCorreoControlador() 
    {
        mProperties = new Properties();
    }
    
    // --- Firma del método modificada para aceptar un File ---
    public void createEmail(JTextField paramDestinatario_tb, JTextField paramAsunto_tb, JTextArea paramContenido_tb, File archivoAdjunto) 
    {
        emailTo = paramDestinatario_tb.getText().trim();
        subject = paramAsunto_tb.getText().trim();
        content = paramContenido_tb.getText().trim();
        
         // Simple mail transfer protocol
        mProperties.put("mail.smtp.host", "smtp.gmail.com");
        mProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        mProperties.setProperty("mail.smtp.starttls.enable", "true");
        mProperties.setProperty("mail.smtp.port", "587");
        mProperties.setProperty("mail.smtp.user",emailFrom);
        mProperties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        mProperties.setProperty("mail.smtp.auth", "true");
        
        mSession = Session.getDefaultInstance(mProperties);
        
        
        try 
        {
            mCorreo = new MimeMessage(mSession);
            mCorreo.setFrom(new InternetAddress(emailFrom));
            mCorreo.setRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            mCorreo.setSubject(subject);
            
            // --- INICIO DE MODIFICACIÓN: Crear mensaje Multipart ---
            
            // 1. Crear la parte del texto del mensaje
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(content);
            
            // 2. Crear el contenedor multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart); // Añadir la parte del texto

            // 3. Añadir el archivo adjunto (si existe)
            if (archivoAdjunto != null) {
                BodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(archivoAdjunto);
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(archivoAdjunto.getName());
                multipart.addBodyPart(attachmentBodyPart); // Añadir la parte del adjunto
            }
            
            // 4. Establecer el multipart como el contenido del correo
            mCorreo.setContent(multipart);
            
            // --- FIN DE MODIFICACIÓN ---
        } 
        catch (AddressException ex) 
        {
            Logger.getLogger(EnviarCorreoControlador.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (MessagingException ex) 
        {
            Logger.getLogger(EnviarCorreoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendEmail() {
    try 
    {
        Transport mTransport = mSession.getTransport("smtp");
        mTransport.connect(emailFrom, passwordFrom);
        mTransport.sendMessage(mCorreo, mCorreo.getRecipients(Message.RecipientType.TO));
        mTransport.close();
            
        JOptionPane.showMessageDialog(null, "Correo enviado");
        } 
        catch (NoSuchProviderException ex) 
        {
            Logger.getLogger(EnviarCorreoControlador.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (MessagingException ex) 
        {
            Logger.getLogger(EnviarCorreoControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}