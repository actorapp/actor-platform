package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeRpc extends SchemeContainer implements SchemeRecord {
    private String name;
    private int header;

    private AbsResponse response;

    public SchemeRpc(String name, int header, AbsResponse response) {
        this.name = name;
        this.header = header;
        this.response = response;
    }

    public AbsResponse getResponse() {
        return response;
    }

    public String getName() {
        return name;
    }

    public int getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "rpc " + name;
    }

    public static abstract class AbsResponse {

    }

    public static class RefResponse extends AbsResponse {
        private String name;

        public RefResponse(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class AnonymousResponse extends AbsResponse {
        private SchemeResponseAnonymous response;

        public AnonymousResponse(SchemeResponseAnonymous response) {
            this.response = response;
        }

        public SchemeResponseAnonymous getResponse() {
            return response;
        }
    }
}
