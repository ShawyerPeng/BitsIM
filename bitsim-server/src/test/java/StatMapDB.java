import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Map;

/**
 * Created by ShawyerPeng on 2017/11/9.
 */
public class StatMapDB {
    private static final String MAP_NAME = "STAT_MAP";
    private String filePath;
    DB db = null;
    //需要的数据格式
    Map<String, String> statMap = null;
    DBMod type = null;

    static enum DBMod {
        READ,
        WRITE
    }

    //类的构造方法
    public StatMapDB(String filePath, DBMod type) {
        //初始化
        this.filePath = filePath;
        this.type = type;
        init();
    }

    //初始化mapdb
    private void init() {
        File file = new File(filePath);
        db = DBMaker
                .newFileDB(file)
                .transactionDisable()
                .asyncWriteFlushDelay(100)
                .make();
        if (type.equals(DBMod.WRITE)) {
            if (file.exists()) {
                file.delete();
                new File(filePath + ".p").delete();
            }
            statMap = db.createTreeMap(MAP_NAME).make();
        } else {
            statMap = db.getTreeMap(MAP_NAME);
        }
    }

    public Map<String, String> getStatMapDB() {
        return this.statMap;
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
