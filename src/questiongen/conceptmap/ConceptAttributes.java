package questiongen.conceptmap;

/**
 * Created by raymond on 18/03/15.
 */
public class ConceptAttributes {
    public String lemma;
    public String pos;
    public String ner;
    public String speaker;

    public String toString() {
        return "{ConceptAttributes: lemma:[" + lemma + "], pos:[" +pos +"], ner:[" + ner + "], speaker:[" +speaker +"]}";
    }
}
