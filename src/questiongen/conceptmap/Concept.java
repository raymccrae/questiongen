package questiongen.conceptmap;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by raymond on 26/06/2015.
 */
public class Concept {
    private String title;
    private ConceptAttributes attributes;
    private SourceLocation sourceLocation;
    private Set<String> alternativeTerms;
    private List<Relationship> relationshipList;

    public Concept(String title, ConceptAttributes attributes, SourceLocation location) {
        this.title = title;
        this.attributes = attributes;
        this.sourceLocation = location;
        this.alternativeTerms = new HashSet<String>();
        this.relationshipList = new LinkedList<Relationship>();

        if (attributes != null) {
            addAlternativeTerm(attributes.getLemma());
        }
    }

    public String getTitle() {
        return title;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void addAlternativeTerm(String term) {
        alternativeTerms.add(term);
    }

    public void addRelationship(Relationship relationship) {
        relationshipList.add(relationship);
    }
}
