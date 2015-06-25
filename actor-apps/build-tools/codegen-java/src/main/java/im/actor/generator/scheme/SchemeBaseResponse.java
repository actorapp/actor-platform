package im.actor.generator.scheme;


/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeBaseResponse extends SchemeContainer implements SchemeRecord {
    private int header;

    public SchemeBaseResponse(int header) {
        this.header = header;
    }

    public int getHeader() {
        return header;
    }
}
