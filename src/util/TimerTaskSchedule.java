/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import datos.DatosDocumento;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import modelo.NotaSimpleCaixa;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 *
 * @author TECH ID SOLUTIONS
 */
public class TimerTaskSchedule {
    private static String direccion=System.getProperty("user.dir");
    /**
     * 
     * @param asunto
     * @param texto 
     */
    public static void enviarCorreoNotificacion(String asunto, String texto){
        try {
             Properties props = new Properties();
             props.setProperty("mail.smtp.host", "smtp.gmail.com");
             props.setProperty("mail.smtp.starttls.enable", "true");
             props.setProperty("mail.smtp.port","587");
             props.setProperty("mail.smtp.user", "techidbpo@gmail.com");
             props.setProperty("mail.smtp.auth", "true");
             props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
             javax.mail.Session session = javax.mail.Session.getDefaultInstance(props);
             MimeMessage message = new MimeMessage(session);
             message.setFrom(new InternetAddress("techidbpo@gmail.com"));
             InternetAddress listaDirecciones[] = new InternetAddress[4];
             listaDirecciones[0] = new InternetAddress("bpo@tidinternationalgroup.com");
             listaDirecciones[1] = new InternetAddress("angel.diaz@tidinternationalgroup.com");
             listaDirecciones[2] = new InternetAddress("josepineroe@gmail.com");
             listaDirecciones[3] = new InternetAddress("japestrada@nauta.cu");
             message.addRecipients(Message.RecipientType.TO, listaDirecciones);
             message.setSubject(asunto);
             message.setText(texto);
             Transport t = session.getTransport("smtp");
             t.connect("techidbpo@gmail.com","t3ch1dbp0");
             t.sendMessage(message,message.getAllRecipients());
             t.close();
         } catch (MessagingException ex) {
             
         }
     } 
    
    /**
     * 
     * @param channelSftp
     * @param directorio
     * @param nombreArchivo
     * @param estado
     * @param tipo 
     */
    private static void generarTraza(ChannelSftp channelSftp, String directorio, String nombreArchivo, String estado, String tipo){
            try {
                String nombreArchivoTrazas = "trazas_descargados_GrupoBC.dat";
                if (tipo.equals("Subidos"))
                    nombreArchivoTrazas = "trazas_subidos_GrupoBC.dat";
                else if (tipo.equals("Directorios"))
                    nombreArchivoTrazas = "registro_directorios_procesados.dat";
                String fuente = "/home/BPO/Historico/".concat(nombreArchivoTrazas);
                String destino = direccion.concat("/Enviados/").concat(nombreArchivoTrazas); 
                channelSftp.get(fuente, destino);
                BufferedWriter bw;
                try {
                    bw = new BufferedWriter(new FileWriter(destino, true));
                    String fechaAccion = new Date().toString();
                    bw.write(nombreArchivo.concat("\t\t").concat(directorio).concat("\t\t").concat(estado).concat("\t\t").concat(fechaAccion).concat("\n"));
                    bw.close();
                } catch (IOException ex) {
                } 
                channelSftp.put(destino, "/home/BPO/Historico/".concat(nombreArchivoTrazas));
                File fichero = new File(destino);
                fichero.delete();
           } catch (SftpException ex) {
           }
    }
    
    /**
     * 
     * @param channelSftp
     * @param directorio
     * @param nombreArchivo
     * @param estado
     * @param tipo 
     */
    private static void generarTrazaOCR(ChannelSftp channelSftp, String nombreArchivo, String estado, String tipo){
            try {
                String nombreArchivoTrazas = "trazas_descargados_GrupoBCOCR.dat";
                if (tipo.equals("Subidos"))
                    nombreArchivoTrazas = "trazas_subidos_GrupoBCOCR.dat";
                String fuente = "/home/BPO/Historico/".concat(nombreArchivoTrazas);
                String destino =  direccion.concat("/Enviados/").concat(nombreArchivoTrazas); 
                channelSftp.get(fuente, destino);
                BufferedWriter bw;
                try {
                    bw = new BufferedWriter(new FileWriter(destino, true));
                    String fechaAccion = new Date().toString();
                    bw.write(nombreArchivo.concat("\t\t").concat(estado).concat("\t\t").concat(fechaAccion).concat("\n"));
                    bw.close();
                } catch (IOException ex) {
                    
                } 
                channelSftp.put(destino, "/home/BPO/Historico/".concat(nombreArchivoTrazas));
                File fichero = new File(destino);
                fichero.delete();
           } catch (SftpException ex) {
           }
    } 
    
    /**
     * 
     * @param conexion
     * @param directorio
     * @param nombreArchivo
     * @param estado 
     */
    private static void generarTrazaBD(Connection conexion, String directorio, String nombreArchivo, String estado){
        DatosDocumento datosDocumento = new DatosDocumento();
        datosDocumento.insertar(conexion, nombreArchivo, estado, directorio, new Date().toString());
    } 
    
    /**
     * 
     * @param channelSftp
     * @param directorio
     * @return 
     */
    private static Boolean existeDirectorio(ChannelSftp channelSftp, String directorio){
        String destino = direccion.concat("/Enviados/").concat("registro_directorios_procesados.dat"); 
        String fuente = "/home/BPO/Historico/".concat("registro_directorios_procesados.dat");
         try {
             channelSftp.get(fuente, destino);
         } catch (SftpException ex) {
             Logger.getLogger(TimerTaskSchedule.class.getName()).log(Level.SEVERE, null, ex);
         }
        File archivoTrazas = new File(destino);
        Scanner scanner = null;
        String datosDocumento[];
        try {
            scanner = new Scanner(archivoTrazas);
            while (scanner.hasNextLine()){
                try {
                    datosDocumento = scanner.nextLine().split("\t\t");
                    if (datosDocumento[1].equals(directorio)){
                        archivoTrazas.delete();
                        return true;
                    }
                } catch (Exception ex) {
                }
            }
        } catch (FileNotFoundException ex) {
        }
        archivoTrazas.delete();
        return false;
    } 
    
    /**
     * 
     * @param elemento
     * @return 
     */
    private static Boolean esDirectorio(String elemento){
        String partes[] = elemento.split("\\.");
        return partes.length == 1;
    }
    
    private static Boolean estanAmbosXML(String nombreXML, Vector listaArchivosProcesadosB1){
        for (int i = 0; i < listaArchivosProcesadosB1.size(); i++){
            ChannelSftp.LsEntry archivo = (ChannelSftp.LsEntry)listaArchivosProcesadosB1.elementAt(i);
            if (archivo.getFilename().equals(nombreXML))
                return true;
        }
        return false;
    }
    
    /**
     * 
     */
    public void setTimerTaskSchedule(/*final ChannelSftp channelSftp, final ChannelSftp channelSftpTech*/) {
        Timer timer;
        timer = new Timer();
        

            TimerTask task = new TimerTask() {
                @Override
                public void run(){
                    try {
                        //Connection conexion = SincronizarRepoGrupoBC.NuevaConexion();
                        //Session sessionGrupoBC = MetodosGenerales.connectFTPbySSHGrupoBC();
                        Session sessionTech = MetodosGenerales.connectFTPbySSHTech();
                        
                        //ChannelSftp channelSftpGrupoBC = null;
                        ChannelSftp channelSftpTech = null;
                        try {
                            if (sessionTech != null && sessionTech.isConnected()) {
                                //channelSftpGrupoBC = (ChannelSftp)sessionGrupoBC.openChannel("sftp");
                                //channelSftpGrupoBC.connect();
                                channelSftpTech = (ChannelSftp)sessionTech.openChannel("sftp");
                                if(channelSftpTech!=null)
                                    channelSftpTech.connect();
                            }
                        } catch (JSchException ex) {
                            System.out.println(ex.getMessage());
                        } 
                            //System.out.println(conexion);
                        Boolean descargaDocumentos = false;
                        String direccionRutaGrupoFTP = "/NotaSimple/TechId/Enviar/";
                        String direccionRutaTechFTP = "/home/BPO/EnviadosWS/NotaSimpleOCR/";
                        String directorioTemp = "Enviados/";
                        
                        //channelSftpTech.put("/home/adiaz/bpo/ocr/Enviados/".concat("2018-05-1916957_000000001.pdf"), "/home/BPO/EnviadosOCR/Procesados/".concat("2018-05-1916957_000000001.pdf"));
                        //channelSftpTech.rm("/home/BPO/EnviadosOCR/".concat("2018-05-1916957_000000001.pdf"));
                         Vector listaArchivosPDF=null;           
                        if(channelSftpTech!=null)
                            listaArchivosPDF =  channelSftpTech.ls(direccionRutaTechFTP);
                        Integer cantidadDocumentosDescargado = 0;
                        //System.out.println("Descargando archivos para OCR: FTP TECH...");
                       if(listaArchivosPDF!=null)
                       {
                            if (listaArchivosPDF.size() >= 3){
                            descargaDocumentos = true;
                            for (int j = 0; j < listaArchivosPDF.size(); j++) {
                                ChannelSftp.LsEntry archivo = (ChannelSftp.LsEntry)listaArchivosPDF.elementAt(j);
                                if (!esDirectorio(archivo.getFilename()) &&  !archivo.getFilename().equals(".") && !archivo.getFilename().equals("..") ){
                                    channelSftpTech.get(direccionRutaTechFTP.concat(archivo.getFilename()), directorioTemp.concat(archivo.getFilename()));
                                    //System.out.println(archivo.getFilename());
                                    cantidadDocumentosDescargado++;
                                    /*
                                    String partesNombre[] = archivo.getFilename().split("\\.");
                                    if (partesNombre[1].equalsIgnoreCase("pdf")){
                                        //generarTrazaBD(conexion, directorio.getFilename(), archivo.getFilename(), "Descargado de GrupoBC");
                                        generarTrazaOCR(channelSftpTech, archivo.getFilename(), "Descargado de GrupoBC", "Descargados");
                                    }
                                    */
                                    channelSftpTech.put( direccion.concat("/Enviados/").concat(archivo.getFilename()), "/home/BPO/ConvirtiendoWS/NotaSimpleOCR/".concat(archivo.getFilename()));
                                    channelSftpTech.put( direccion.concat("/Enviados/").concat(archivo.getFilename()), "/home/BPO/PendientesOCR/".concat(archivo.getFilename()));
                                    //channelSftpGrupoBC.put("/home/adiaz/bpo/ocr/Enviados/".concat(archivo.getFilename()), "/NotaSimple/TechId/Enviar/Procesados/".concat(archivo.getFilename()));
                                    channelSftpTech.rm(direccionRutaTechFTP.concat(archivo.getFilename()));
                                }
                            }
                        }
                       }
                       
                        /*
                        if (descargaDocumentos){
                            enviarCorreoNotificacion("BPO OCR:Documentos descargados", "TECH ID Solutions: Se han descargado " + cantidadDocumentosDescargado + " NOTAS SIMPLES desde Grupo BC para OCR, gracias");
                        }
                        */
                       // System.out.println("Descarga finalizada:" + new Date().toString());
                        

                        System.out.println("Inicio OCR: " + new Date());
                        File archivos = new File( direccion.concat("/Enviados/"));
                        File listaArchivos[] = archivos.listFiles();
                        ArrayList<NotaSimpleCaixa> listaNotaSimpleCaixa = new ArrayList<>();
                        try {
                             ITesseract iTesseract = new Tesseract();
                             iTesseract.setLanguage("spa");
                             for (File archivo : listaArchivos) {
                                 String text = iTesseract.doOCR(archivo);
                                 text = MetodosGenerales.limpiarTexto(text);
                                 text = text.replaceAll("&", "&amp;");
                                 listaNotaSimpleCaixa.add(new NotaSimpleCaixa(text, archivo.getName()));
                                  //System.out.println("Procesado:     " + archivo.getName());
                             }
                         } catch (TesseractException ex) {
                             System.out.println(ex.getMessage());
                         } 
                         String result = MetodosGenerales.generarXMLNotaSimpleCaixa(listaNotaSimpleCaixa, channelSftpTech);
                         /*
                         if (!result.equals("Documentos no procesados:\n"))
                             System.out.println(result);
                        */ 
                         System.out.println("Fin OCR: " + new Date());
                         if (listaNotaSimpleCaixa.size() > 0)
                             enviarCorreoNotificacion("BPO OCR:OCR TERMINADO - DOCS PENDIENTES DE REVISION", "TECH ID Solutions: existen documentos pendientes de revisión.");
                         
                        //Subir archivos
                        /*
                        Integer cantidadDocumentosSubidos = 0;
                        System.out.println("Subiendo archivos OCR: FTP Grupo BC...");
                        String rutaProcesadosTechFTP = "/home/BPO/ProcesadosOCR/";
                        String rutaProcesadosTechFTPB1 = "/home/BPO/ProcesadosOCRB1/";
                        String rutaProcesadosGrupoFTP = "/NotaSimple/TechId/Recibir/";
                        
                        Vector listaArchivosProcesados =  channelSftpTech.ls(rutaProcesadosTechFTP);
                        Vector listaArchivosProcesadosB1 =  channelSftpTech.ls(rutaProcesadosTechFTPB1);
                        String nombreArchivoB1;
                        if (listaArchivosProcesados.size() > 2){
                            for (int i = 0; i < listaArchivosProcesados.size(); i++) {
                                ChannelSftp.LsEntry archivo = (ChannelSftp.LsEntry)listaArchivosProcesados.elementAt(i);
                                if (!archivo.getFilename().equals(".") && !archivo.getFilename().equals("..") && !esDirectorio(archivo.getFilename())){
                                    //Verificar si los dos XMLs No*****.xml y B1*****.xml están en nuestro servidor.
                                    if (estanAmbosXML("B1".concat(archivo.getFilename().substring(2, archivo.getFilename().length())), listaArchivosProcesadosB1)){
                                        nombreArchivoB1 = "B1".concat(archivo.getFilename().substring(2, archivo.getFilename().length())) ;
                                        channelSftpTech.get(rutaProcesadosTechFTP.concat(archivo.getFilename()), directorioTemp.concat(archivo.getFilename()));
                                        channelSftpTech.get(rutaProcesadosTechFTPB1.concat(nombreArchivoB1), directorioTemp.concat(nombreArchivoB1));
                                        
                                        System.out.println(archivo.getFilename());
                                        System.out.println(nombreArchivoB1);
                                        
                                        cantidadDocumentosSubidos++;
                                        //generarTrazaBD(conexion, "Procesados", archivo.getFilename(), "Subido al Grupo BC");
                                        generarTrazaOCR(channelSftpTech, archivo.getFilename(), "Subido al Grupo BC", "Subidos");
                                        generarTrazaOCR(channelSftpTech, nombreArchivoB1, "Subido al Grupo BC", "Subidos");
                                        
                                        channelSftpGrupoBC.put("/home/adiaz/bpo/ocr/Enviados/".concat(archivo.getFilename()), rutaProcesadosGrupoFTP.concat(archivo.getFilename()));
                                        channelSftpGrupoBC.put("/home/adiaz/bpo/ocr/Enviados/".concat(nombreArchivoB1), rutaProcesadosGrupoFTP.concat(nombreArchivoB1));
                                        
                                        channelSftpTech.put("/home/adiaz/bpo/ocr/Enviados/".concat(archivo.getFilename()), "/home/BPO/SubidosOCR/".concat(archivo.getFilename()));
                                        channelSftpTech.put("/home/adiaz/bpo/ocr/Enviados/".concat(nombreArchivoB1), "/home/BPO/SubidosOCR/".concat(nombreArchivoB1));
                                        try {
                                            String [] cmd = {"rm", "/home/adiaz/bpo/ocr/Enviados/".concat(archivo.getFilename())}; 
                                            Runtime.getRuntime().exec(cmd);
                                            String [] cmdB1 = {"rm", "/home/adiaz/bpo/ocr/Enviados/".concat(nombreArchivoB1)}; 
                                            Runtime.getRuntime().exec(cmdB1);
                                            
                                        } catch (IOException ioe) {
                                            System.out.println (ioe);
                                        }
                                        channelSftpTech.rm(rutaProcesadosTechFTP.concat(archivo.getFilename()));
                                        channelSftpTech.rm(rutaProcesadosTechFTPB1.concat(nombreArchivoB1));
                                    }
                                }
                            }
                        }
                        if (cantidadDocumentosSubidos > 0){
                            enviarCorreoNotificacion("BPO OCR:Documentos subidos", "TECH ID Solutions: Se han subido " + cantidadDocumentosSubidos*2 + " NOTAS SIMPLES OCR para Grupo BC, gracias");
                        }
                        System.out.println("Subida finalizada:" + new Date().toString());
                        */
                        //conexion.close();
                        //channelSftpGrupoBC.disconnect();
                        if(channelSftpTech!=null)
                            channelSftpTech.disconnect();
                        //sessionGrupoBC.disconnect();
                        if(sessionTech!=null)
                            sessionTech.disconnect();
                    } catch (Exception ex) { //SftpException
                        System.out.println(ex.getMessage());
                    }
                    
                }
            };
         // Comienza dentro de 0ms y luego lanzamos la tarea cada 1000ms   60000
           // timer.schedule(task, 1000, 3600000); cada una hora
           //timer.schedule(task, 1000, 600000); //Cada 10 minutos
           timer.schedule(task, 1000, 1800000); //Cada 30 minutos
    }
    
    
}
