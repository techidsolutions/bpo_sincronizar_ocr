package util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import modelo.ComponenteFormulario;
import modelo.NotaSimpleCaixa;

public class MetodosGenerales {
    private static String direccion=System.getProperty("user.dir");

    /**
     *
     * @param fecha
     * @return
     */
    public static String convertirFechaDDMMYYYY(Date fecha) {
        return (new SimpleDateFormat("dd-MM-yyyy")).format(fecha);
    }

    /**
     *
     * @return
     */
    public static Connection NuevaConexion() {
        Connection con = null;
        try {
            ConexionBD cmBD = new ConexionBD("127.0.0.1", "bpo", "bpo12345*");//bpo1234*
            con = cmBD.Conexion();
        } catch (SQLException e) {
        }
        return con;
    }

    /**
     *
     * @return
     */
    public static Session connectFTPbySSHGrupoBC() {

        JSch jsch = new JSch();
        Session session = null;
        try {
            File file = new File(direccion.concat("/conf/configFtp_OCR.properties"));
            FileInputStream fileInputStream = new FileInputStream(file);
            Properties mainProperties = new Properties();
            mainProperties.load(fileInputStream);
            //buscando en el fichero de conf la llave "ipFtp"
            String ipFtp = mainProperties.getProperty("ipFtp");
            //buscando en el fichero de conf la llave "userFtp"
            String userFtp = mainProperties.getProperty("userFtp");
            //buscando en el fichero de conf la llave "passWdFtp"
            String passWdFtp = mainProperties.getProperty("passWdFtp");
            //buscando en el fichero de conf la llave "portFtp"
            String portFtp = mainProperties.getProperty("portFtp");
            //Cerrando el fichero
            fileInputStream.close();
            try {
                //session = jsch.getSession(userFtp, ipFtp);
                session = jsch.getSession(userFtp, ipFtp, new Integer(portFtp));
                session.setPassword(passWdFtp);
                Properties properties=new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                session.setConfig(properties);
                session.connect();
            } catch (JSchException ex) {
                System.out.println("Error:" + ex.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return session;
    }

    /**
     *
     * @return
     */
    public static Session connectFTPbySSHTech() {
          /* Cargando fichero de configuracion para la conexion al Ftp*/

        Session session = null;
        try {
            File file = new File(direccion.concat("/conf/configFtp_OCR.properties"));
            FileInputStream fileInputStream = new FileInputStream(file);
            Properties mainProperties = new Properties();
            mainProperties.load(fileInputStream);
            //buscando en el fichero de conf la llave "ipFtp"
           String ipFtp = mainProperties.getProperty("ipFtp");
           
            //buscando en el fichero de conf la llave "userFtp"
           String userFtp = mainProperties.getProperty("userFtp");
       
            //buscando en el fichero de conf la llave "passWdFtp"
           String passWdFtp = mainProperties.getProperty("passWdFtp");
           //buscando en el fichero de conf la llave "portFtp"
           String portFtp =mainProperties.getProperty("portFtp");
            //Cerrando el fichero
            fileInputStream.close();
            JSch jsch = new JSch();

            try {
                session = jsch.getSession(userFtp, ipFtp, new Integer(portFtp));
                session.setPassword(passWdFtp);
                Properties properties=new Properties();
                properties.setProperty("StrictHostKeyChecking","no");
                session.setConfig(properties);
                session.connect();
            } catch (Exception ex) {
                String o = ex.toString();
            }
            return session;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return session;
    }

    /**
     *
     * @param texto
     * @return
     */
    public static String limpiarTexto(String texto) {
        try {
            FileInputStream fis = new FileInputStream("conf/texto_eliminar.dat");
            int valor = fis.read();
            String linea = "";
            while (valor != -1) {
                Character caracter = (char) valor;
                if (caracter != '\n') {
                    if (caracter.equals('\u0093')) {
                        linea = linea.concat("“");
                    } else {
                        linea = linea.concat(String.valueOf(caracter));
                    }
                } else {
                    if (texto.contains(linea.trim())) {
                        texto = texto.replace(linea.trim(), "");
                    }
                    //texto = texto.replaceAll("^" + linea.trim() + "$", "");
                    linea = "";
                }
                valor = fis.read();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return texto;
    }

    /**
     *
     * @param listaNotaSimpleCaixa
     * @param channelSftpTech
     * @return
     */
    public static ArrayList<String> generarXMLNotaSimpleCaixa(ArrayList<NotaSimpleCaixa> listaNotaSimpleCaixa, ChannelSftp channelSftpTech) throws SftpException {
        
//        String result = "Documentos no procesados:\n";
        
        System.out.println("listanotasimplecaixa.length: " + listaNotaSimpleCaixa.size());
        ArrayList<ComponenteFormulario> listaComponentesXML = null;
        ArrayList<String> NoProcesados = new ArrayList<>();
        for (NotaSimpleCaixa notaSimpleCaixa : listaNotaSimpleCaixa) {
            
            boolean procesado = false;
            try {
                listaComponentesXML = new ArrayList<>();
                listaComponentesXML.add(new ComponenteFormulario("ID_DOCUMENTO", notaSimpleCaixa.getNombre().split("/.")[0]));
                System.out.println("Añadió ID A listaComponentesXML");
                String descripcion = "DESCRIPCION";
                String datosResgistrales[] = notaSimpleCaixa.getTexto().split(descripcion, 2);
                if (datosResgistrales.length < 2) {
                    descripcion = "DESCRIPCIÓN";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }
                if (datosResgistrales.length < 2) {
                    descripcion = "ESCRIPCION";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }
                if (datosResgistrales.length < 2) {
                    descripcion = "ESCRIPCIÓN";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }
                if (datosResgistrales.length < 2) {
                    descripcion = "Descripción";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }
                if (datosResgistrales.length < 2) {
                    descripcion = "URBANA";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }
                if (datosResgistrales.length < 2) {
                    descripcion = "DESCRIPCION DE LA FINCA";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }
                if (datosResgistrales.length < 2) {
                    descripcion = "DATOS DE LA FINCA";
                    datosResgistrales = notaSimpleCaixa.getTexto().split(descripcion, 2);
                }

                listaComponentesXML.add(new ComponenteFormulario("DATOS_REGISTRALES", datosResgistrales[0]));
                System.out.println("Añadió DATOS_REGISTRALES a listaComponentesXML");
                String titular = "TITULARIDADES";
                String datosDescripcion[] = datosResgistrales[1].split(titular, 2);
                if (datosDescripcion.length < 2) {
                    titular = "TITULARES";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                if (datosDescripcion.length < 2) {
                    titular = "ITULARES";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                if (datosDescripcion.length < 2) {
                    titular = "TITULARIDAD";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                if (datosDescripcion.length < 2) {
                    titular = "TITULAR";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                if (datosDescripcion.length < 2) {
                    titular = "Titular";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                if (datosDescripcion.length < 2) {
                    titular = "TITULO";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                if (datosDescripcion.length < 2) {
                    titular = "RESUMEN DE TITULARES";
                    datosDescripcion = datosResgistrales[1].split(titular, 2);
                }
                listaComponentesXML.add(new ComponenteFormulario("DESCRIPCION_FINCA", descripcion + "\n" + datosDescripcion[0]));
                System.out.println("Añadió DESCRIPCION_FINCA a listaComponentesXML");
                String cargas = "CARGAS";
                String datosTitulares[] = datosDescripcion[1].split(cargas);
                if (datosTitulares.length < 2) {
                    cargas = "Cargas";
                    datosTitulares = datosDescripcion[1].split(cargas);
                }
                if (datosTitulares.length < 2) {
                    cargas = "Libre de cargas";
                    datosTitulares = datosDescripcion[1].split(cargas);
                }
                if (datosTitulares.length < 2) {
                    cargas = "RESUMEN DE CARGAS";
                    datosTitulares = datosDescripcion[1].split(cargas);
                }
                if (datosTitulares.length < 2) {
                    cargas = "HIPOTECA";
                    datosTitulares = datosDescripcion[1].split(cargas);
                }
                if (datosTitulares.length < 2) {
                    cargas = "A R G A S";
                    datosTitulares = datosDescripcion[1].split(cargas);
                }
                if (datosTitulares.length > 2) {
                    for (int i = 2; i < datosTitulares.length; i++) {
                        datosTitulares[1] = datosTitulares[1].concat(datosTitulares[i]);
                    }
                }

                listaComponentesXML.add(new ComponenteFormulario("TITULARES", titular + "\n" + datosTitulares[0]));
                System.out.println("Añadió TITULARES a listaComponentesXML");
                String asiento = "PENDIENTES DE DESPACHO";
                String datosCargas[] = datosTitulares[1].split(asiento);
                
                if (datosCargas.length < 2) {
                    asiento = "Pendientes de Despacho";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "ASIENTOS LIBRO DIARIO";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "SIN ASIENTOS PENDIENTES";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "Sin asientos pendientes";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "Documentos relativos a la finca presentados y pendientes de despacho";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "DOCUMENTOS PENDIENTES";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "Asientos pendientes de despacho:";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "DOCUMENTOS RELATIVOS A LA FINCA PRESENTADOS Y PENDIENTES DE DESPACHO";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "PRESENTACION";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "ASIENTOS DEL DIARIO";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                if (datosCargas.length < 2) {
                    asiento = "ASIENTO/S";
                    datosCargas = datosTitulares[1].split(asiento);
                }
                //listaComponentesXML.add(new ComponenteFormulario("CARGAS", cargas + "\n" + datosCargas[0]));

                if (datosCargas.length < 2) {
                    String advertencia = "Para información de consumidores";
                    String datosAdvertencia[] = datosCargas[0].split(advertencia, 2);
                    if (datosAdvertencia.length < 2) {
                        advertencia = "1.- A los efectos de";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "INFORMACIÓN AL CONSUMIDOR:";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "INFORMACION AL CONSUMIDOR:";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "A los efectos de";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "Conforme a lo";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE,";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE :";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE:";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "ADVERTENCIA:";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "ADVERTENCIAS";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "ADVERTENCIA";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "Queda prohibida la incorporación de los datos de esta nota a ficheros o bases";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "Queda prohibida";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    listaComponentesXML.add(new ComponenteFormulario("CARGAS", cargas + "\n" + datosAdvertencia[0]));
                    listaComponentesXML.add(new ComponenteFormulario("ASIENTOS_PENDIENTES", "Sin asientos pendientes"));
                    System.out.println("Añadió cargas y asientos pendientes a listaComponentesXML");
                } else {
                    listaComponentesXML.add(new ComponenteFormulario("CARGAS", cargas + "\n" + datosCargas[0]));
                    System.out.println("Añadió cargas a listaComponentesXML");
                    //Separar las advertencias
                    String advertencia = "Para información de consumidores";
                    String datosAdvertencia[] = datosCargas[1].split(advertencia, 2);
                    if (datosAdvertencia.length < 2) {
                        advertencia = "1.- A los efectos de";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "INFORMACIÓN AL CONSUMIDOR:";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "INFORMACION AL CONSUMIDOR:";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "A los efectos de";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "Conforme a lo";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE,";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE :";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "MUY IMPORTANTE:";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "ADVERTENCIA:";
                        datosAdvertencia = datosCargas[0].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "ADVERTENCIAS";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "ADVERTENCIA";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "Queda prohibida la incorporación de los datos de esta nota a ficheros o bases";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    if (datosAdvertencia.length < 2) {
                        advertencia = "Queda prohibida";
                        datosAdvertencia = datosCargas[1].split(advertencia, 2);
                    }
                    listaComponentesXML.add(new ComponenteFormulario("ASIENTOS_PENDIENTES", asiento + datosAdvertencia[0].replace(advertencia, "")));
                }
                System.out.println("Terminó de añadir todos los nodos a listaComponentesXML");
                procesado = true;
                ManipularXML creador;
                try {
                    
                    creador = new ManipularXML();
                    creador.crearDocumentoNotaSimpleCaixa(listaComponentesXML, procesado);
                    creador.escribirArchivoNotaSimpleCaixa(notaSimpleCaixa.getNombre(),procesado);
                                       
                    try {                     
                        //Se envía el XML generado al FTP de Tech directorio PendientesOCR
//                        channelSftpTech.put(direccion.concat("/Procesados/").concat(notaSimpleCaixa.getNombre().split("\\.")[0].concat(".xml")), "/home/BPO/PendientesOCR/".concat("No").concat(notaSimpleCaixa.getNombre().split("\\.")[0].concat(".xml")));
                        Path temp;
                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get("/home/BPO/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(temp != null){
                            System.out.println("Se envía el XML generado a PendientesOCR");
                        }else{
                            System.out.println("Failed to move the file Procesados to pendientesOCR");
                        }
                        
                        Path tem;
                        tem = Files.copy(Paths.get("/home/BPO/EnviadosWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),Paths.get("/home/BPO/ConvirtiendoWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(tem != null){
                            System.out.println("Se envía el pdf procesado correctamente de EnviadosWS to ConvirtiendoWS/NotaSimpleOCR");
                        }else{
                            System.out.println("Failed to move the file EnviadosWS to ConvirtiendoWS/NotaSimpleOCR");
                        }
                        
                        Path temp1;
                        temp1 = Files.copy(Paths.get("/home/BPO/EnviadosWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),Paths.get("/home/BPO/PendientesOCR/" + notaSimpleCaixa.getNombre()),StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(temp1 != null){
                            System.out.println("Se envía el pdf procesado correctamente de EnviadosWS to PendientesOCR");
                        }else{
                            System.out.println("Failed to move the file EnviadosWS to PendientesOCR");
                        }
                        
                        
                        //Se pasa el documento PDF de EnviadosOCR a EnviadosOCRB1
                        //channelSftpTech.put("/home/adiaz/bpo/ocr/Enviados/".concat(notaSimpleCaixa.getNombre()), "/home/BPO/EnviadosOCRB1/".concat(notaSimpleCaixa.getNombre()));
                        //channelSftpTech.put("/home/adiaz/bpo/ocr/Enviados/".concat(notaSimpleCaixa.getNombre()), "/home/BPO/ConvirtiendoWS/NotaSimpleOCR/".concat(notaSimpleCaixa.getNombre()));
                        //channelSftpTech.rm("/home/BPO/EnviadosWS/NotaSimpleOCR/".concat(notaSimpleCaixa.getNombre()));
                        //Se eliminan los archivos temporales
                        try { 
                                                
                            String[] cmdPDF = {"rm","/home/BPO/EnviadosWS/NotaSimpleOCR/".concat(notaSimpleCaixa.getNombre())};
                            Runtime.getRuntime().exec(cmdPDF);
                            String[] cmdXML = {"rm",direccion.concat("/Procesados/").concat(notaSimpleCaixa.getNombre().split("\\.")[0].concat(".xml"))};
                            Runtime.getRuntime().exec(cmdXML);
                        } catch (IOException ioe) {
                            System.out.println("IOException. Error al intentar borrar los documentos" + ioe);
                        }
                    } catch (Exception ex) {
                        System.out.println("Excepción en envío a PendientesOCR" + ex.getMessage());
                        Logger.getLogger(MetodosGenerales.class.getName()).log(Level.SEVERE, null, ex);
//                        try{
//                            System.out.println("Entro al try 2");
////                            channelSftpTech.put(direccion.concat("/Enviados/").concat(notaSimpleCaixa.getNombre()), "/home/BPO/ConvirtiendoWS/DocumentosKO/".concat(notaSimpleCaixa.getNombre()));
//                            
//                            //channelSftpTech.rm("/home/BPO/ConvirtiendoWS/NotaSimpleOCR/".concat(notaSimpleCaixa.getNombre()));
////                            String[] cmdPDF = {"rm","/home/BPO/EnviadosWS/NotaSimpleOCR/".concat(notaSimpleCaixa.getNombre())};
////                            Runtime.getRuntime().exec(cmdPDF);
//                            result = result.concat(notaSimpleCaixa.getNombre() + "\n");
//                            System.out.println("Result" + result);
////                            System.out.println("Documento no procesado:" + notaSimpleCaixa.getNombre());
////                            TimerTaskSchedule.enviarCorreoNotificacion("BPO OCR:DOCUMENTOS NO PROCESADO", "TECH ID Solutions: al siguiente documento no se ha podido aplicar el OCR:" + notaSimpleCaixa.getNombre() + "\n" + "--------------------TEXTO DEL DOCUMENTO NO PROCESADO--------------------" + "\n\n" + notaSimpleCaixa.getTexto());
//                        }catch(Exception ex1){
//                            System.out.println("Exception" + ex1.getMessage());
//                        }
                    }
                } catch (ParserConfigurationException | TransformerException ex) {
                    System.out.println("ParserConfigurationException | TransformerException" + ex.getMessage());
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("ArrayIndexOutOfBoundsException: " + e.getMessage());
                //Generar xml de los no procesados 
                ManipularXML creador1;
                try{
                    creador1 = new ManipularXML();
                    creador1.crearDocumentoNotaSimpleCaixa(listaComponentesXML,procesado);
                    creador1.escribirArchivoNotaSimpleCaixa(notaSimpleCaixa.getNombre(),procesado);
                    try {
//                    channelSftpTech.put(direccion.concat("/Enviados/").concat(notaSimpleCaixa.getNombre()), "/home/BPO/ConvirtiendoWS/DocumentosKO/".concat(notaSimpleCaixa.getNombre()));
                        Path temp;
                        temp = Files.copy(Paths.get(direccion + "/No_procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get("/home/BPO/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(temp != null){
                            System.out.println("Se envía el XML generado de No_procesado a PendientesOCR");
                        }else{
                            System.out.println("Failed to move the file Procesados to pendientesOCR");
                        }
                        Path t;
//                        Files.delete("/home/BPO/ConvirtiendoWS/DocumentosKO/".concat(notaSimpleCaixa.getNombre()));
//                        Files.delete(Paths.get("/home/BPO/ConvirtiendoWS/DocumentosKO/" + notaSimpleCaixa.getNombre()));
                        t= Files.copy(Paths.get("/home/BPO/EnviadosWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),Paths.get("/home/BPO/PendientesOCR/" + notaSimpleCaixa.getNombre()), StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(t != null){
                            System.out.println("File renamed and moved successfully pdf from EnviadosWS/NotaSimpleOCR to PendientesOCR");
                        }else{
                            System.out.println("Failed to move the file");
                        }
                    
                        Path tem;
                    
                        tem= Files.copy(Paths.get("/home/BPO/EnviadosWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),Paths.get("/home/BPO/ConvirtiendoWS/DocumentosKO/" + notaSimpleCaixa.getNombre()),StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(tem != null){
                            System.out.println("File renamed and moved successfully pdf from EnviadosWS/NotaSimpleOCR to ConvirtiendoWS/DocumentosKO");
                        }else{
                            System.out.println("Failed to move the file");
                        }
        
                        Path temp1;
                        temp1= Files.copy(Paths.get("/home/BPO/EnviadosWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),Paths.get("/home/BPO/ConvirtiendoWS/NotaSimpleOCR/" + notaSimpleCaixa.getNombre()),StandardCopyOption.REPLACE_EXISTING);
//                        temp = Files.copy(Paths.get(direccion + "/Procesados/" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"),Paths.get(direccion + "/PendientesOCR/" + "No" + notaSimpleCaixa.getNombre().split("\\.")[0] + ".xml"));
                        if(temp1 != null){
                            System.out.println("File renamed and moved successfully pdf from EnviadosWS/NotaSimpleOCR to ConvirtiendoWS/NotaSimpleOCR");
                        }else{
                            System.out.println("Failed to move the file");
                        }
//                    channelSftpTech.rm("/home/BPO/ConvirtiendoWS/NotaSimpleOCR/".concat(notaSimpleCaixa.getNombre()));
                    String[] cmdPDF = {"rm",("/home/BPO/EnviadosWS/NotaSimpleOCR/").concat(notaSimpleCaixa.getNombre())};
                    Runtime.getRuntime().exec(cmdPDF);
//                    result = notaSimpleCaixa.getNombre() + "\n";
//                    System.out.println("Resultado de no procesados" + result);
//                    System.out.println("Documento no procesado:" + notaSimpleCaixa.getNombre());
                    NoProcesados.add(notaSimpleCaixa.getNombre());
                    
                    
//                    TimerTaskSchedule.enviarCorreoNotificacion("BPO OCR:DOCUMENTOS NO PROCESADO", "TECH ID Solutions: al siguiente documento no se ha podido aplicar el OCR:" + notaSimpleCaixa.getNombre() + "\n" + "--------------------TEXTO DEL DOCUMENTO NO PROCESADO--------------------" + "\n\n" + notaSimpleCaixa.getTexto());
                } catch (IOException ex) {
                    Logger.getLogger(MetodosGenerales.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("IOException" + ex.getMessage());
                }
                } catch (ParserConfigurationException | TransformerException ex) {
                    System.out.println("ParserConfigurationException | TransformerException" + ex.getMessage());
                }
            }
        }
//        System.out.println("Result final" + result);
        return NoProcesados;
    }
}
