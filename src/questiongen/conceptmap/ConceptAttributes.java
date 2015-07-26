package questiongen.conceptmap;

/**
 * Created by raymond on 18/03/15.
 */
public class ConceptAttributes {
    public String lemma;
    public String pos;
    public String ner;
    public String speaker;

    public  ConceptAttributes() {

    }

    public ConceptAttributes(String lemma, String pos, String ner, String speaker) {
        this.lemma = lemma;
        this.pos = pos;
        this.ner = ner;
        this.speaker = speaker;
    }

    public String toString() {
        return "{ConceptAttributes: lemma:[" + lemma + "], pos:[" +pos +"], ner:[" + ner + "], speaker:[" +speaker +"]}";
    }

    public String getLemma() {
        return lemma;
    }

    public String getPos() {
        return pos;
    }

    public String getNer() {
        return ner;
    }

    public String getSpeaker() {
        return speaker;
    }
}
