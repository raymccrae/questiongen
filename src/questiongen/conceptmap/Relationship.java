package questiongen.conceptmap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by raymond on 26/06/2015.
 */
public class Relationship {
    private Concept sourceConcept;
    private Concept targetConcept;
    private String linkingTerm;
    private Set<String> alternativeTerms;

    public Relationship(Concept sourceConcept, Concept targetConcept, String linkingTerm) {
        this.sourceConcept = sourceConcept;
        this.targetConcept = targetConcept;
        this.linkingTerm = linkingTerm.toLowerCase().trim();
        this.alternativeTerms = new HashSet<String>();

        sourceConcept.addRelationship(this);
    }

    public Concept getSourceConcept() {
        return sourceConcept;
    }

    public Concept getTargetConcept() {
        return targetConcept;
    }

    public String getLinkingTerm() {
        return linkingTerm;
    }

    public Set<String> getAlternativeTerms() {
        return Collections.unmodifiableSet(alternativeTerms);
    }

    public void addAlternativeTerm(String alternativeTerm) {
        alternativeTerms.add(alternativeTerm.toLowerCase().trim());
    }

    public int getRanking() {
        int rank = 0;
        String sourceNER = sourceConcept.getNER();
        String targetNER = targetConcept.getNER();

        if (sourceConcept.getSourceLocation() == SourceLocation.SourceBoth)
            rank += 10;

        if (sourceNER != null) {
            rank += 5;

            if (!sourceNER.equals("O"))
                rank += 20;
        }

        if (targetNER != null) {
            rank += 5;

            if (!targetNER.equals("O"))
                rank += 20;
        }

        return rank;
    }
}
