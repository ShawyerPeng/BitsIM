package protocol.process.store.impl;

import com.google.common.collect.Maps;
import protocol.process.store.ClientSession;
import protocol.process.store.ISessionStore;

import java.util.Map;

public class SessionStoreImpl implements ISessionStore {
    private final static Map<String, ClientSession> sessionStore = Maps.newConcurrentMap();

    @Override
    public ClientSession sessionForClient(String clientId) {
        ClientSession clientSession = sessionStore.get(clientId);
        if (clientSession == null) {
            clientSession = new ClientSession(clientId, false);
        }
        return clientSession;
    }
}