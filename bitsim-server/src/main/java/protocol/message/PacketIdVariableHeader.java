package protocol.message;

/**
 * MQTT协议中，有部分消息类型的可变头部只含有报文标识符Packet Identifier，把这部分抽取出来，单独成为一个可变头部
 */	
public class PacketIdVariableHeader {
	// 报文标识符
	private int packetId;

	public PacketIdVariableHeader(int packetId) {
		if (packetId < 1 || packetId > 65535) {
			throw new IllegalArgumentException("报文标识符ID:" + packetId + "必须在1~65535范围内");
		}
		this.packetId = packetId;
	}

	public int getPacketId() {
		return packetId;
	}

	public void setPacketId(int packetId) {
		this.packetId = packetId;
	}

	@Override
	public String toString() {
		return "PacketIdVariableHeader{" +
				"packetId=" + packetId +
				'}';
	}
}