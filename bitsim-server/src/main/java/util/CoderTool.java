package util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 对数据解码编码的工具类
 */
public class CoderTool {
    // 字符集
    private static Charset charset = Charset.forName("utf-8");

    /**
     * 对字符串进行byte编码
     */
    public static ByteBuffer encode(String str) {
        return charset.encode(str);
    }

    /**
     * 对bytebuffer进行解码，解码成字符串
     */
    public static String decode(ByteBuffer byteBuffer) {
        return charset.decode(byteBuffer).toString();
    }
}
