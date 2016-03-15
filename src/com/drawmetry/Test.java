package com.drawmetry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Test {

    public static void main(String[] args) throws Exception {
        TransformerFactory fact = TransformerFactory.newInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out);
        writer.writeStartDocument("1.0");
        writer.writeStartElement("svg");
        writer.writeDefaultNamespace("http://www.w3.org/2000/svg");
        writer.writeNamespace("dm", "http://www.drawmetry.com");
        writer.writeStartElement("dm:catalog");
        writer.writeStartElement("book");
        writer.writeAttribute("id", "\"01\"");
        writer.writeStartElement("code");
        writer.writeCharacters("I01");
        writer.writeEndElement();
        writer.writeEmptyElement("title");
        writer.writeCharacters("This is the title. \"1 < 2\"");
        writer.writeEndElement();
        writer.writeStartElement("price");
        writer.writeCharacters("$2.95");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        writer.close();
        Transformer transformer = fact.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        Source source = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        transformer.transform(source, new StreamResult(System.out));
    }
}
