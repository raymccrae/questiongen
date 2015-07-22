package questiongen.conceptmap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tml.storage.Repository;
import tml.vectorspace.operations.CmmProcess;
import tml.corpus.SearchResultsCorpus;
import tml.conceptmap.*;
import tml.vectorspace.operations.results.ConceptMapResult;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by raymond on 18/03/15.
 */
public class ConceptMapExtract {

    public static void main(String[] args) throws Exception {
//        extractConceptMap("Age_of_Reformation_2012_a");
//        invokeNLP(new File("/Users/raymond/Documents/OpenUniversity/T802/samples/tablet.txt"));
        enrichConceptMapWithStanford(
                new File("/Users/raymond/Documents/OpenUniversity/T802/tml/output.xml"),
                new File("/Users/raymond/Documents/OpenUniversity/T802/stanford-corenlp-full-2015-01-30/tablet.txt.xml"));
    }

    public static void extractConceptMap(String filename) throws Exception {
        Repository repository = new Repository("/Users/raymond/Dropbox/Documents/Open University/T802/tml/lucene");

        String title = filename.replaceAll("_", "");
        SearchResultsCorpus corpus = new SearchResultsCorpus("type:sentence AND parent:" + title);
        corpus.getParameters().setTermSelectionThreshold(1.0);
        corpus.load(repository);

        CmmProcess extraction = new CmmProcess();
        extraction.setCorpus(corpus);
        extraction.start();

        ConceptMapResult result = extraction.getResults().get(0);
        ConceptMap conceptMap = result.getConceptMap();
        conceptMap.writeToXML("./" + filename);
    }

    public static void importDocumentIntoRepository(File documentFile, Repository repository) throws IOException {
        repository.addDocumentsInList(new File[] {documentFile});
    }

    public static void invokeNLP(File file) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "/Users/raymond/Documents/OpenUniversity/T802/stanford-corenlp-full-2015-01-30/corenlp.sh",
                "-annotators",
                "tokenize,ssplit,pos,lemma,ner,parse,dcoref",
                "-file",
                file.getAbsolutePath()
        );

        pb.directory(new File("/Users/raymond/Documents/OpenUniversity/T802/stanford-corenlp-full-2015-01-30"));

        Process p = pb.start();
        p.waitFor();
        int exitValue = p.exitValue();
        System.out.println("exit code " + exitValue);
    }

    public static void enrichConceptMapWithStanford(File conceptMap, File stanfordFile) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document conceptMapDocument = builder.parse(conceptMap);
        Document nlpDocument = builder.parse(stanfordFile);

        Map<String, ConceptAttributes> nlpTokens = new HashMap<String, ConceptAttributes>();

        NodeList tokenList = nlpDocument.getElementsByTagName("token");
        for (int i = 0; i < tokenList.getLength(); i++) {
            Element token = (Element) tokenList.item(i);
            String word = childValue(token, "word").toLowerCase();
            ConceptAttributes attributes = tokenElementToConceptAttributes(token);
            nlpTokens.put(word, attributes);

            System.out.println("Token [" + word + "] : " + attributes);
        }

        NodeList conceptList = conceptMapDocument.getElementsByTagName("concept");
        for (int i = 0; i < conceptList.getLength(); i++) {
            Element concept = (Element) conceptList.item(i);
            String label = concept.getAttribute("label").toLowerCase();

            ConceptAttributes attributes = nlpTokens.get(label);
            if (attributes != null) {
                System.out.println("Match concept " + label);
                concept.setAttribute("lemma", attributes.lemma);
                concept.setAttribute("POS", attributes.pos);
                concept.setAttribute("NER", attributes.ner);
                concept.setAttribute("Speaker", attributes.speaker);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(conceptMapDocument);
        StreamResult result = new StreamResult(new File("/Users/raymond/Documents/OpenUniversity/T802/tml/enhance.xml"));
        transformer.transform(source, result);
    }

    public static ConceptAttributes tokenElementToConceptAttributes(Element token) {
        ConceptAttributes attributes = new ConceptAttributes();
        attributes.lemma = childValue(token, "lemma");
        attributes.pos = childValue(token, "POS");
        attributes.ner = childValue(token, "NER");
        attributes.speaker = childValue(token, "Speaker");
        return attributes;
    }

    public static String childValue(Element element, String childName) {
        String value = null;
        NodeList list = element.getElementsByTagName(childName);
        if (list.getLength() > 0) {
            Element child = (Element) list.item(0);
            value = child.getTextContent();
        }
        return value;
    }

}
