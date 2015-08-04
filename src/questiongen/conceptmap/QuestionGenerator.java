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

    private static final String CXL_XML_NAMESPACE = "http://cmap.ihmc.us/xml/cmap/";

    public static void main(String[] args) {
        QuestionGenerator qg = new QuestionGenerator();

//        File tmlFile1 = new File("/Users/raymond/IdeaProjects/questiongen/cmm-enhanced/Age_of_Reformation_2012_a.xml");
//        File tmlFile2 = new File("/Users/raymond/IdeaProjects/questiongen/cmm-enhanced/Age_of_Reformation_2012_b.xml");
//        File cxlFile = new File("/Users/raymond/IdeaProjects/questiongen/manual-cmm/Age_of_Reformation.cxl");
        File tmlFile1 = new File("/Users/raymond/IdeaProjects/questiongen/cmm-enhanced/Wars_of_Independence_2014_d.xml");
        File tmlFile2 = new File("/Users/raymond/IdeaProjects/questiongen/cmm-enhanced/Wars_of_Independence_2014_e.xml");
        File cxlFile = new File("/Users/raymond/IdeaProjects/questiongen/manual-cmm/Wars_of_Independence.cxl");
        try {
            qg.process(tmlFile1, tmlFile2, cxlFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void process(File sourceAFile, File sourceBFile, File manualFile) throws ParserConfigurationException, SAXException, IOException {
        Collection<Concept> sourceAConcepts = readTMLFile(sourceAFile, SourceLocation.SourceA);
        Collection<Concept> sourceBConcepts = readTMLFile(sourceBFile, SourceLocation.SourceB);
        Collection<Concept> manualConcepts  = readCXLFile(manualFile, SourceLocation.None);


        mergeConcepts(sourceAConcepts, manualConcepts);
        mergeConcepts(sourceBConcepts, manualConcepts);
        mergeConcepts(sourceBConcepts, sourceAConcepts);

        Collection<Concept> matchedConcepts = filterMatchingConcepts(manualConcepts);

        printMatchBothSides(matchedConcepts);
        System.out.println("===========================================");
        printMatchOneSide(matchedConcepts);
        System.out.println("===========================================");

        Collection<Relationship> matchingRelationships = matchingRelationshipsBothSides(matchedConcepts);
        Collection<String> questions = generateMatchBothSidesQuestions(matchingRelationships);
        for (String question : questions) {
            System.out.println(question);
        }

        System.out.println("===========================================");

        Collection<Relationship> onesidedRelationships = matchingRelationshipsOneSide(matchedConcepts);
        Collection<String> differQuestions = generateMatcOneSidesQuestions(onesidedRelationships);
        for (String question : differQuestions) {
            System.out.println(question);
        }
    }

    private void mergeConcepts(Collection<Concept> fromConcepts, Collection<Concept> toConcepts) {
        Map<String, Concept> termConceptMap = new HashMap<String, Concept>();
        for (Concept c : toConcepts) {
            termConceptMap.put(c.getTitle(), c);
            for (String altTerm : c.getAlternativeTerms()) {
                termConceptMap.put(altTerm, c);
            }
        }

        for (Concept cA : fromConcepts) {
            if (termConceptMap.containsKey(cA.getTitle())) {
                Concept c = termConceptMap.get(cA.getTitle());
                c.mergeConcept(cA);
            }

            for (String altTerm : cA.getAlternativeTerms()) {
                if (termConceptMap.containsKey(altTerm)) {
                    Concept c = termConceptMap.get(altTerm);
                    c.mergeConcept(cA);
                }
            }
        }
    }

    public Collection<Concept> readTMLFile(File tmlFile, SourceLocation location) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document conceptMapDocument = builder.parse(tmlFile);
        return parseTMLDocument(conceptMapDocument, location);
    }

    public Collection<Concept> parseTMLDocument(Document conceptMapDocument, SourceLocation location) {
        Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
        NodeList conceptNodeList = conceptMapDocument.getElementsByTagName("concept");
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

        NodeList relationshipNodeList = conceptMapDocument.getElementsByTagName("relationship");
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
        return parseCXLDocument(conceptMapDocument, location);
    }

    public Collection<Concept>parseCXLDocument(Document conceptMapDocument, SourceLocation location) {
        Map<String, Concept> concepts = new HashMap<String, Concept>();
        NodeList conceptNodeList = conceptMapDocument.getElementsByTagName("concept");
        for (int i = 0; i < conceptNodeList.getLength(); i++) {
            Element conceptNode = (Element) conceptNodeList.item(i);

            String title = conceptNode.getAttribute("label");
            title = title.replace('\n', ' ');
            Concept concept = new Concept(title, null, location);

            if (conceptNode.hasAttribute("long-comment")) {
                String[] alternatives = conceptNode.getAttribute("long-comment").split("\n");
                for (String alternative : alternatives) {
                    concept.addAlternativeTerm(alternative);
                }
            }

            String conceptId = conceptNode.getAttribute("id");
            concepts.put(conceptId, concept);
        }

        Map<String, List<String>> linkingPhases = new HashMap<String, List<String>>();
        NodeList linkingList = conceptMapDocument.getElementsByTagName("linking-phrase");
        for (int i = 0; i < linkingList.getLength(); i++) {
            Element linkingNode = (Element) linkingList.item(i);
            String linkingId = linkingNode.getAttribute("id");
            String label = linkingNode.getAttribute("label");
            label = label.replace('\n', ' ');

            List<String> phases = new LinkedList<String>();
            phases.add(label);

            if (linkingNode.hasAttribute("long-comment")) {
                String[] alternatives = linkingNode.getAttribute("long-comment").split("\n");
                for (String alternative : alternatives) {
                    phases.add(alternative);
                }
            }

            linkingPhases.put(linkingId, phases);
        }

        Map<String, List<String>> forwardConnections = new HashMap<String, List<String>>();

        NodeList connectionNodeList = conceptMapDocument.getElementsByTagName("connection");
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

    Collection<Concept> filterMatchingConcepts(Collection<Concept> concepts) {
        LinkedList<Concept> matchingConcepts = new LinkedList<Concept>();
        for (Concept c : concepts) {
            if (c.getSourceLocation() == SourceLocation.SourceBoth) {
                matchingConcepts.add(c);
//                c.reduceRelationships(SourceLocation.SourceBoth);
            }
        }

        return matchingConcepts;
    }

    public Collection<Relationship> matchingRelationshipsBothSides(Collection<Concept> concepts) {
        LinkedList<Relationship> relationships = new LinkedList<Relationship>();

        for (Concept source : concepts) {
            for (Relationship relationship : source.getRelationshipSet()) {
                Concept target = relationship.getTargetConcept();
                if (target.getSourceLocation() == SourceLocation.SourceBoth) {
                    relationships.add(relationship);
                }
            }
        }

        return relationships;
    }

    public Collection<Relationship> matchingRelationshipsOneSide(Collection<Concept> concepts) {
        LinkedList<Relationship> relationships = new LinkedList<Relationship>();

        for (Concept source : concepts) {
            for (Relationship relationship : source.getRelationshipSet()) {
                Concept target = relationship.getTargetConcept();
                if (target.getSourceLocation() == SourceLocation.SourceA || target.getSourceLocation() == SourceLocation.SourceB) {
                    relationships.add(relationship);
                }
            }
        }

        return relationships;
    }

    public void printMatchBothSides(Collection<Concept> concepts) {
        for (Concept source : concepts) {
            for (Relationship relationship : source.getRelationshipSet()) {
                Concept target = relationship.getTargetConcept();
                if (target.getSourceLocation() == SourceLocation.SourceBoth) {
                    System.out.println(source.getDebugDescription() + " [" + relationship.getLinkingTerm() + "] " + target.getDebugDescription() + " : " + relationship.getRanking());
                }
            }
        }
    }

    public void printMatchOneSide(Collection<Concept> concepts) {
        for (Concept source : concepts) {
            for (Relationship relationship : source.getRelationshipSet()) {
                Concept target = relationship.getTargetConcept();
                if (target.getSourceLocation() == SourceLocation.SourceA || target.getSourceLocation() == SourceLocation.SourceB) {
                    System.out.println(source.getDebugDescription() + " [" + relationship.getLinkingTerm() + "] " + target.getDebugDescription()  + " : " + relationship.getRanking());
                }
            }
        }
    }

    public Collection<String> generateMatchBothSidesQuestions(Collection<Relationship> relationships) {
        List<String> questions = new LinkedList<String>();

        for (Relationship relationship : relationships) {
            String question = "Both sources agree, " +
                    relationship.getSourceConcept().getTitle() +
                    " " +
                    relationship.getLinkingTerm() +
                    " " +
                    relationship.getTargetConcept().getTitle() +
                    ". Are the underlaying reasons the same?";
            questions.add(question);
        }

        return questions;
    }

    public Collection<String> generateMatcOneSidesQuestions(Collection<Relationship> relationships) {
        List<String> questions = new LinkedList<String>();

        for (Relationship relationship : relationships) {
            String source = relationship.getTargetConcept().getSourceLocation() == SourceLocation.SourceA ? "Source A" : "Source B";
            String otherSource = relationship.getTargetConcept().getSourceLocation() == SourceLocation.SourceA ? "Source B" : "Source A";

            String question = "From " +
                    source +
                    ", " +
                    relationship.getSourceConcept().getTitle() +
                    " " +
                    relationship.getLinkingTerm() +
                    " " +
                    relationship.getTargetConcept().getTitle() +
                    ". Why does this differ from the account in " +
                    otherSource +
                    "?";
            questions.add(question);
        }

        return questions;
    }
}
