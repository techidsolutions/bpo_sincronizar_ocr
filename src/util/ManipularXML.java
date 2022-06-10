/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import modelo.ComponenteFormulario;
import modelo.NotaSimpleCaixa;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author TECH ID SOLUTIONS
 */
public class ManipularXML {

    Document documento;
     private static String direccion=System.getProperty("user.dir");
    
    /**
     * 
     * @throws ParserConfigurationException 
     */
    public ManipularXML() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder constructor = docFactory.newDocumentBuilder();
        documento = constructor.newDocument();
    }
     
    /**
     * 
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException 
     */ 
    public String convertirCadena() throws TransformerConfigurationException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();        
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        DOMSource source = new DOMSource(documento);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        String output = writer.getBuffer().toString();
        return output;
    }
    
    /**
     * 
     * @param nombreArchivoXML
     * @throws TransformerConfigurationException
     * @throws TransformerException 
     */
    public void escribirArchivoNotaSimpleCaixa(String nombreArchivoXML, boolean procesado) throws TransformerConfigurationException, TransformerException {
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        String ruta = "";
        if(procesado){
            ruta = direccion.concat("/Procesados/").concat(nombreArchivoXML.split("\\.")[0]);//Utiles.rutaProcesadosNotaSimpleCaixa;
        }else{
            ruta = direccion.concat("/No_procesados/").concat(nombreArchivoXML.split("\\.")[0]);
        }
        File archivo = new File(ruta +".xml");
        
        DOMSource source = new DOMSource(documento);
        
        StreamResult result = new StreamResult(archivo);
        
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"xhtml1-strict.dtd");
        transformer.transform(source, result);
        
    }
    
    /**
     * 
     * @param listaComponentesXML 
     */
    public void crearDocumentoNotaSimpleCaixa(ArrayList<ComponenteFormulario> listaComponentesXML, boolean procesado) {
        
        Element raiz;
        Element elementoTemporal;
        Element DatosRegistrales;
        Element DescripicionFinca;
        Element Titulares;
        Element Cargas;
        Element Asientospendientes;
        
        raiz = documento.createElement("OcrResultNotaSimple");
        
        documento.appendChild(raiz);
        
        for (ComponenteFormulario componenteFormulario: listaComponentesXML){
            
            elementoTemporal = documento.createElement(componenteFormulario.getNombre());
            elementoTemporal.setTextContent(componenteFormulario.getValor());
            raiz.appendChild(elementoTemporal);
        }
        if(!procesado){
            DatosRegistrales = documento.createElement("DATOS_REGISTRALES");
            DatosRegistrales.appendChild(documento.createTextNode("DATOS_REGISTRALES"));
            raiz.appendChild(DatosRegistrales);

            DescripicionFinca = documento.createElement("DESCRIPCION_FINCA");
            DescripicionFinca.appendChild(documento.createTextNode("DESCRIPCION_FINCA"));
            raiz.appendChild(DescripicionFinca);

            Titulares = documento.createElement("TITULARES");
            Titulares.appendChild(documento.createTextNode("TITULARES"));
            raiz.appendChild(Titulares); 

            Cargas = documento.createElement("CARGAS");
            Cargas.appendChild(documento.createTextNode("CARGAS"));
            raiz.appendChild(Cargas);

            Asientospendientes = documento.createElement("ASIENTOS_PENDIENTES");
            Asientospendientes.appendChild(documento.createTextNode("ASIENTOS_PENDIENTES"));
            raiz.appendChild(Asientospendientes);
        }
    }
    
    /**
     *
     * @param notaSimpleCaixa
     */
//    public void crearDocumentoNotaSimpleCaixaNoGenerado(ArrayList<ComponenteFormulario> listaComponentesXML) {
//        System.out.println("Entró a crear Documento NotaSimpleCaixa NoGenerado");
//        Element raiz;
//        Element elementoTemporal;
//        raiz = documento.createElement("OcrResultNotaSimple");
//        documento.appendChild(raiz);
////        elementoTemporal = documento.createElement(notaSimpleCaixa.getNombre());
////        elementoTemporal.setTextContent(notaSimpleCaixa.getTexto());
////        raiz.appendChild(elementoTemporal);
//                
//        for (ComponenteFormulario componenteFormulario: listaComponentesXML){
//            
//            elementoTemporal = documento.createElement(componenteFormulario.getNombre());
//            elementoTemporal.setTextContent(componenteFormulario.getValor());
//            raiz.appendChild(elementoTemporal);
//        }
//        
//    } 
}

