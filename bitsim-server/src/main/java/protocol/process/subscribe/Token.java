package protocol.process.subscribe;

/**
 * 此类用于存储每个Topic解析出来的订阅（Topic：country/china/tianjin）
 */
public class Token {
    static final Token MULTI = new Token("#");  // #支持一个主题内任意级别话题
    static final Token SINGLE = new Token("+"); // + 只匹配一个主题级别的通配符
    static final Token EMPTY = new Token("");   // 不匹配

    private String name;

    Token(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
