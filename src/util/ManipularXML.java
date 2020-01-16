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
    public void escribirArchivoNotaSimpleCaixa(String nombreArchivoXML) throws TransformerConfigurationException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        String ruta = direccion.concat("\\Procesados\\").concat(nombreArchivoXML);//Utiles.rutaProcesadosNotaSimpleCaixa;
        
        File archivo = new File(ruta + nombreArchivoXML.split("\\.")[0] + ".xml");
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
    public void crearDocumentoNotaSimpleCaixa(ArrayList<ComponenteFormulario> listaComponentesXML) {
        Element raiz;
        Element elementoTemporal;
        raiz = documento.createElement("OcrResultNotaSimple");
        documento.appendChild(raiz);
        for (ComponenteFormulario componenteFormulario: listaComponentesXML){
            elementoTemporal = documento.createElement(componenteFormulario.getNombre());
            elementoTemporal.setTextContent(componenteFormulario.getValor());
            raiz.appendChild(elementoTemporal);
        }
    }  
}
