import org.mapdb.DB;
import org.mapdb.DBMaker;
import protocol.process.event.PublishEvent;

import java.io.File;
import java.util.concurrent.ConcurrentMap;

public class MapDbTest {
    private static final String PATH = "D:\\Demo\\BitsIM\\moquette_store.mapdb";

    public static void main(String[] args) {
        File tmpFile = new File("D:\\Demo\\BitsIM\\moquette_store.mapdb");
        DB db = DBMaker.newFileDB(tmpFile).make();
        ConcurrentMap<String, PublishEvent> persistentQosTempMessage = db.getHashMap("publishTemp");
        PublishEvent pe = persistentQosTempMessage.get("client-id-1");
        if (pe != null) {
            System.out.println(pe.toString());
        }
    }
}
