package im.actor.core.network.parser;

public class ParsingExtension {

    private BaseParser<RpcScope> rpcScopeParser;

    private BaseParser<Update> updateScopeParser;

    public ParsingExtension(BaseParser<RpcScope> rpcScopeParser, BaseParser<Update> updateScopeParser) {
        this.rpcScopeParser = rpcScopeParser;
        this.updateScopeParser = updateScopeParser;
    }

    public BaseParser<RpcScope> getRpcScopeParser() {
        return rpcScopeParser;
    }

    public BaseParser<Update> getUpdateScopeParser() {
        return updateScopeParser;
    }
}
