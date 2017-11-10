package protocol.message;

import java.util.Hashtable;

public class PacketIdManager {
    // 包ID是两个字节，所以最大的是65535，最小是1
    private static final int MIN_MSG_ID = 1;
    private static final int MAX_MSG_ID = 65535;
    private static int nextMsgId = MIN_MSG_ID - 1;
    private static Hashtable<Integer, Integer> inUseMsgIds = new Hashtable<Integer, Integer>();

    // 包ID
    private int packetId;

    /**
     * 获取包ID
     */
    public static synchronized int getNextMessageId() {
        int startingMessageId = nextMsgId;
        // 循环两次是为了给异步出问题提供一个容错范围
        int loopCount = 0;
        do {
            nextMsgId++;
            if (nextMsgId > MAX_MSG_ID) {
                nextMsgId = MIN_MSG_ID;
            }
            if (nextMsgId == startingMessageId) {
                loopCount++;
                if (loopCount == 2) {
                    throw new UnsupportedOperationException("获取不到可用的包ID");
                }
            }
        } while (inUseMsgIds.containsKey(nextMsgId));
        Integer id = nextMsgId;
        inUseMsgIds.put(id, id);
        return nextMsgId;
    }

    /**
     * 释放不用的包ID
     */
    public synchronized static void releaseMessageId(int msgId) {
        inUseMsgIds.remove(msgId);
    }
}
