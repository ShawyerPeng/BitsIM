package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Java Properties属性文件操作类，JVM参数为："-Duser.dir=D:\Demo\BitsIM\bitsim-server\src\main"
 */
public class MqttTool {
	private final static Logger Log = Logger.getLogger(MqttTool.class);
	
	private static Properties props = new Properties();
	//配置文件路径
	private static final String CONFIG_FILE = System.getProperty("user.dir") + "/resources/mqtt.properties";
	
	static{
		loadProperties(CONFIG_FILE);
	}
	
	/**
	 * 加载属性文件
	 */
	private static void loadProperties(String propertyFilePath){
		try {
			FileInputStream in = new FileInputStream(propertyFilePath);
			props = new Properties();
			props.load(in);
		} catch (IOException e) {
			Log.error("属性文件读取错误");
			e.printStackTrace();
		}
	}

	/**
	 * 从指定的键取得对应的值
	 */
	public static String getProperty(String key){
		return props.getProperty(key);
	}
	
	/**
	 *  从指定的键取得整数
	 */
	public static Integer getPropertyToInt(String key){
		String str = props.getProperty(key);
		if(StringTool.isBlank(str.trim())){
			return null;
		}
		return Integer.valueOf(str.trim()); 
	}
}
