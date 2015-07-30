package questiongen.conceptmap;

import java.util.*;

/**
 * Created by raymond on 26/06/2015.
 */
public class Concept {
    private String title;
    private ConceptAttributes attributes;
    private SourceLocation sourceLocation;
    private Set<String> alternativeTerms;
    private Set<Relationship> relationshipSet;

    public Concept(String title, ConceptAttributes attributes, SourceLocation location) {
        this.title = title.toLowerCase();
        this.attributes = attributes;
        this.sourceLocation = location;
        this.alternativeTerms = new HashSet<String>();
        this.relationshipSet = new HashSet<Relationship>();

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

    public Set<String> getAlternativeTerms() {
        return Collections.unmodifiableSet(alternativeTerms);
    }

    public void addAlternativeTerm(String term) {
        alternativeTerms.add(term.toLowerCase());
    }

    public Set<Relationship> getRelationshipSet() {
        return Collections.unmodifiableSet(relationshipSet);
    }

    public void addRelationship(Relationship relationship) {
        relationshipSet.add(relationship);
    }

    public void mergeConcept(Concept concept) {
        addAlternativeTerm(concept.getTitle());
        for (String term : concept.getAlternativeTerms()) {
            addAlternativeTerm(term);
        }

        relationshipSet.addAll(concept.relationshipSet);

        if ((sourceLocation == SourceLocation.SourceA && concept.sourceLocation == SourceLocation.SourceB) ||
                (sourceLocation == SourceLocation.SourceB && concept.sourceLocation == SourceLocation.SourceA) ||
                (concept.sourceLocation == SourceLocation.SourceBoth)) {
            sourceLocation = SourceLocation.SourceBoth;
        }
        else if (sourceLocation == SourceLocation.None) {
            sourceLocation = concept.sourceLocation;
        }

        if (attributes == null) {
            attributes = concept.attributes;
        }
    }

    public void reduceRelationships(SourceLocation location) {
        List<Relationship> relationshipsToRemove = new LinkedList<Relationship>();

        for (Relationship r : relationshipSet) {
            if (r.getTargetConcept().getSourceLocation() != location) {
                relationshipsToRemove.add(r);
            }
        }

        relationshipSet.removeAll(relationshipsToRemove);
    }

    public String getDebugDescription() {
        StringBuilder description = new StringBuilder();
        description.append("[");
        description.append(title);


        description.append("(");
        description.append(getSourceString());
        if (attributes != null) {
            description.append(":");
            description.append(attributes.ner);
        }

        description.append(")");
        description.append("]");
        return description.toString();
    }

    public String getSourceString() {
        String result = null;

        switch (sourceLocation) {
            case None:
                result = "None";
                break;
            case SourceA:
                result = "A";
                break;
            case SourceB:
                result = "B";
                break;
            case SourceBoth:
                result = "Both";
                break;
        }

        return result;
    }
}
