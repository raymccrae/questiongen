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
import java.util.*;

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

    public Collection<Concept> readCXLFile(File cxlFile, SourceLocation location) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document conceptMapDocument = builder.parse(cxlFile);
        return parseTMLDocument(conceptMapDocument, location);
    }

    public Collection<Concept>parseCXLDocument(Document conceptMapDocument, SourceLocation location) {
        Map<String, Concept> concepts = new HashMap<String, Concept>();
        NodeList conceptNodeList = conceptMapDocument.getElementsByTagNameNS(null, "concept");
        for (int i = 0; i < conceptNodeList.getLength(); i++) {
            Element conceptNode = (Element) conceptNodeList.item(i);

            String title = conceptNode.getAttribute("label");
            title = title.replace('\n', ' ');
            Concept concept = new Concept(title, null, location);
            String[] alternatives = conceptNode.getAttribute("long-comment").split("\n");
            for (String alternative : alternatives) {
                concept.addAlternativeTerm(alternative);
            }

            String conceptId = conceptNode.getAttribute("id");
            concepts.put(conceptId, concept);
        }

        Map<String, List<String>> linkingPhases = new HashMap<String, List<String>>();
        NodeList linkingList = conceptMapDocument.getElementsByTagNameNS(null, "linking-phrase");
        for (int i = 0; i < linkingList.getLength(); i++) {
            Element linkingNode = (Element) linkingList.item(i);
            String linkingId = linkingNode.getAttribute("id");
            String label = linkingNode.getAttribute("label");
            label = label.replace('\n', ' ');

            List<String> phases = new LinkedList<String>();
            phases.add(label);
            String[] alternatives = linkingNode.getAttribute("long-comment").split("\n");
            for (String alternative : alternatives) {
                phases.add(alternative);
            }

            linkingPhases.put(linkingId, phases);
        }

        Map<String, List<String>> forwardConnections = new HashMap<String, List<String>>();
//        Map<String, List<String>> reverseConnections = new HashMap<String, List<String>>();

        NodeList connectionNodeList = conceptMapDocument.getElementsByTagNameNS(null, "connection");
        for (int i = 0; i < connectionNodeList.getLength(); i++) {
            Element connectionNode = (Element) connectionNodeList.item(i);
            String fromId = connectionNode.getAttribute("from-id");
            String toId = connectionNode.getAttribute("to-id");

            if (forwardConnections.containsKey(fromId)) {
                forwardConnections.get(fromId).add(toId);
            }
            else {
                List<String> list = new LinkedList<String>();
                list.add(toId);
                forwardConnections.put(fromId, list);
            }

//            if (reverseConnections.containsKey(toId)) {
//                reverseConnections.get(toId).add(fromId);
//            }
//            else {
//                List<String> list = new LinkedList<String>();
//                list.add(fromId);
//                reverseConnections.put(toId, list);
//            }
        }

        for (String id : forwardConnections.keySet()) {
            if (concepts.containsKey(id)) {
                List<String> linkingIds = forwardConnections.get(id);
                for (String linkingId : linkingIds) {
                    if (linkingPhases.containsKey(linkingId)) {
                        List<String> targetIds = forwardConnections.get(linkingId);
                        for (String targetId : targetIds) {
                            if (concepts.containsKey(targetId)) {
                                Concept sourceConcept = concepts.get(id);
                                Concept targetConcept = concepts.get(targetId);
                                List<String> linkingTerms = linkingPhases.get(linkingId);

                                Relationship relationship = new Relationship(
                                        sourceConcept,
                                        targetConcept,
                                        linkingTerms.get(0)
                                );

                                for (int i = 1; i < linkingTerms.size(); i++) {
                                    relationship.addAlternativeTerm(linkingTerms.get(i));
                                }
                            }
                        }
                    }
                }
            }
        }

        return concepts.values();
    }

}
