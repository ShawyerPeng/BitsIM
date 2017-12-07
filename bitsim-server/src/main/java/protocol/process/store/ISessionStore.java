package protocol.process.store;

/**
 * SessionStore接口
 */
public interface ISessionStore {
    /**
     * 为客户端分配ClientSession
     */
    ClientSession sessionForClient(String clientId);
}
