package protocol.process.interfaces;

import protocol.process.subscribe.Subscription;

/**
 *  会话存储类
 */
public interface ISessionStore {
	/**
	 * 查看是否已储存了该客户端ID，如果存储了则返回true
	 */
	boolean searchSubscriptions(String clientId);
	
	/**
	 * 清理某个ID所有订阅信息
	 */
	void wipeSubscriptions(String clientId);

	/**
	 * 添加某个订阅消息到存储
	 */
	void addNewSubscription(Subscription newSubscription, String clientId);
	
	/**
	 * 从会话的持久化存储中移除某个订阅主题中的某个client
	 */
	void removeSubscription(String topic, String clientId);
}