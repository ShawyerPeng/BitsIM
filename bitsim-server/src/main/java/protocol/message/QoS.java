package protocol.message;

/**
 * QoS：Quality of Service，PUBLISH 报文的服务质量等级，保证消息传递的次数。
 */
public enum QoS {
	AT_MOST_ONCE  (0),// QoS=0，最多一次
	AT_LEAST_ONCE (1),// QoS=1，最少一次
	EXACTLY_ONCE  (2),// QoS=2，只有一次
	RESERVED      (3);// QoS=3，保留

    public final int val;

	QoS(int val) {
		this.val = val;
	}

	/**
	 * 获取类型对应的值
	 */
	public int value() {
		return val;
	}

    /**
     * 通过读取到的整型来获取对应的QoS类型
     */
	public static QoS valueOf(int i) {
		for(QoS qos: QoS.values()) {
			if (qos.val == i)
				return qos;
		}
		throw new IllegalArgumentException("Qos值无效: " + i);
	}
}