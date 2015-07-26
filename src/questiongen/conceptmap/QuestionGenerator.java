package questiongen.conceptmap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by raymond on 26/06/2015.
 */
public class QuestionGenerator {

    public Collection<Concept> readTMLFile(File tmlFile, SourceLocation location) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document conceptMapDocument = builder.parse(tmlFile);
        return parseTMLDocument(conceptMapDocument, location);
    }

    public Collection<Concept> parseTMLDocument(Document conceptMapDocument, SourceLocation location) {
        Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
        NodeList conceptNodeList = conceptMapDocument.getElementsByTagNameNS(null, "concept");
        for (int i = 0; i < conceptNodeList.getLength(); i++) {
            Element conceptNode = (Element) conceptNodeList.item(i);

            ConceptAttributes attributes = null;
            if (conceptNode.hasAttribute("lemma")) {
                attributes = new ConceptAttributes(
                        conceptNode.getAttribute("lemma"),
                        conceptNode.getAttribute("POS"),
                        conceptNode.getAttribute("NER"),
                        conceptNode.getAttribute("Speaker"));
            }
            Concept concept = new Concept(
                    conceptNode.getAttribute("label"),
                    attributes,
                    location
            );
            int id = Integer.parseInt(conceptNode.getAttribute("id"));
            concepts.put(id, concept);
        }

        NodeList relationshipNodeList = conceptMapDocument.getElementsByTagNameNS(null, "relationship");
        for (int i = 0; i < relationshipNodeList.getLength(); i++) {
            Element relationshipNode = (Element) relationshipNodeList.item(i);

            int sourceConceptId = Integer.parseInt(relationshipNode.getAttribute("source"));
            int targetConceptId = Integer.parseInt(relationshipNode.getAttribute("target"));
            Concept sourceConcept = concepts.get(sourceConceptId);
            Concept targetConcept = concepts.get(targetConceptId);

            Relationship relationship = new Relationship(
                    sourceConcept,
                    targetConcept,
                    relationshipNode.getAttribute("linkingWord"));
        }

        return concepts.values();
    }

}
