package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeResponseAnonymous extends SchemeBaseResponse {
    private SchemeRpc rpc;

    public SchemeResponseAnonymous(int header) {
        super(header);
    }

    public void setRpc(SchemeRpc rpc) {
        this.rpc = rpc;
    }

    public SchemeRpc getRpc() {
        return rpc;
    }
}
