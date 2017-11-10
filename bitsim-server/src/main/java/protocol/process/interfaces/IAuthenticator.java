package protocol.process.interfaces;

/**
 *  身份验证接口
 */
public interface IAuthenticator {
	/**
	 * 校验用户名和密码是否正确
	 */
	boolean checkValid(String username, String password);
}
