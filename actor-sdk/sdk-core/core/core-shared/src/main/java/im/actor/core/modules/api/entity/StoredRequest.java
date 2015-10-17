package im.actor.core.modules.api.entity;

import java.io.IOException;

import im.actor.core.api.parser.RpcParser;
import im.actor.core.network.parser.Request;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class StoredRequest extends BserObject {

    public static StoredRequest fromBytes(byte[] data) throws IOException {
        return Bser.parse(new StoredRequest(), data);
    }

    private static final RpcParser PARSER = new RpcParser();

    private Request request;

    public StoredRequest(Request request) {
        this.request = request;
    }

    private StoredRequest() {

    }

    public Request getRequest() {
        return request;
    }

    @Override
    public void parse(BserValues values) throws IOException {

        // Loading header and serialization of request
        int headerKey = values.getInt(1);
        byte[] requestData = values.getBytes(2);

        // Parsing request
        request = (Request) PARSER.read(headerKey, requestData);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        // Writing header and serialization of request;
        writer.writeInt(1, request.getHeaderKey());
        writer.writeBytes(2, request.toByteArray());
    }
}
