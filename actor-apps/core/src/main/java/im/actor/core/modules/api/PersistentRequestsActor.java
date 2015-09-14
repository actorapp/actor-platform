package im.actor.core.modules.api;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.api.entity.CursorStoredRequest;
import im.actor.core.modules.api.entity.PersistentStorage;
import im.actor.core.modules.api.entity.StoredRequest;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.runtime.Storage;
import im.actor.runtime.storage.KeyValueStorage;

public class PersistentRequestsActor extends ModuleActor {

    private static final String STORAGE = "persistent_requests";

    private KeyValueStorage storage;
    private PersistentStorage requestsStorage;

    public PersistentRequestsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        storage = Storage.createKeyValue(STORAGE);

        requestsStorage = new PersistentStorage();
        byte[] data = storage.getValue(0);
        if (data != null) {
            try {
                requestsStorage = PersistentStorage.fromBytes(data);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // Restarting requests

        for (StoredRequest r : requestsStorage.getRequests()) {
            performRequest(r);
        }

        for (CursorStoredRequest r : requestsStorage.getCursorRequests()) {
            if (r.getRequest() != null) {
                performCursorRequest(r.getRequest().getRequest(), r.getName(), r.getCurrentKey());
            }
        }
    }

    private void onNewRequest(Request request) {
        StoredRequest storedRequest = new StoredRequest(request);
        requestsStorage.getRequests().add(storedRequest);
        performRequest(storedRequest);

        // Saving storage
        saveStorage();
    }

    private void onNewCursorRequest(String name, long key, Request request) {

        // Checking if request is valid
        CursorStoredRequest cursorStoredRequest = requestsStorage.findCursorRequest(name);
        if (cursorStoredRequest != null) {
            if (cursorStoredRequest.getCurrentKey() >= key) {
                return;
            }
            requestsStorage.getCursorRequests().remove(cursorStoredRequest);
        }

        cursorStoredRequest = new CursorStoredRequest(name, key, new StoredRequest(request));
        requestsStorage.getCursorRequests().add(cursorStoredRequest);

        performCursorRequest(request, name, key);

        // Saving storage
        saveStorage();
    }

    private void performCursorRequest(Request request, final String name, final long key) {
        request(request, new RpcCallback() {
            @Override
            public void onResult(Response response) {
                requestCompleted(name, key);
            }

            @Override
            public void onError(RpcException e) {
                // Remove even when error
                requestCompleted(name, key);
            }
        });
    }

    private void requestCompleted(String name, long key) {
        CursorStoredRequest s = requestsStorage.findCursorRequest(name);
        if (s.getCurrentKey() == key) {
            requestsStorage.getCursorRequests().remove(s);
            requestsStorage.getCursorRequests().add(new CursorStoredRequest(name, key, null));
            saveStorage();
        }
    }

    private void performRequest(final StoredRequest request) {

        request(request.getRequest(), new RpcCallback() {
            @Override
            public void onResult(Response response) {
                requestCompleted(request);
            }

            @Override
            public void onError(RpcException e) {
                // Remove even when error
                requestCompleted(request);
            }
        });
    }

    private void requestCompleted(StoredRequest request) {
        requestsStorage.getRequests().remove(request);
        saveStorage();
    }

    private void saveStorage() {
        storage.addOrUpdateItem(0, requestsStorage.toByteArray());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PerformRequest) {
            onNewRequest(((PerformRequest) message).getRequest());
        } else if (message instanceof PerformCursorRequest) {
            PerformCursorRequest cursorRequest = (PerformCursorRequest) message;
            onNewCursorRequest(cursorRequest.getName(), cursorRequest.getKey(),
                    cursorRequest.getRequest());
        } else {
            drop(message);
        }
    }

    public static class PerformRequest {

        private Request request;

        public PerformRequest(Request request) {
            this.request = request;
        }

        public Request getRequest() {
            return request;
        }
    }

    public static class PerformCursorRequest {
        private String name;
        private long key;
        private Request request;

        public PerformCursorRequest(String name, long key, Request request) {
            this.name = name;
            this.key = key;
            this.request = request;
        }

        public String getName() {
            return name;
        }

        public long getKey() {
            return key;
        }

        public Request getRequest() {
            return request;
        }
    }
}