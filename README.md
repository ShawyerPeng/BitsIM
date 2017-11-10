# 介绍
BitsIM，是一个移动即时通讯应用。
在保持 MQTT 标准控制报文结构的基础上，扩展 PUBLISH 控制报文的有效载荷，设计了即时通讯协议 MQTT-IM，通过设计主题订阅，对 MQTT 的开源实现 Moquette 进行扩展，增加 Moquette-IM 模块实现了通讯录、即时通信、状态呈现等系统核心功能。通过数据对比显示，该系统可有效降低功耗和流量消耗，具有很高的实用价值。 

# 简述


# 目录结构

# 快速运行
前置条件：
* 安装 Mysql
* 安装 Maven

运行步骤：
* 创建数据库easyim, 并导入sql/easy_im_v1.sql
* 修改src/main/resources/applicationContext.xml中 mysql 的配置
* 在BitsIM-Server根目录执行maven jetty:run
* 使用 Post 请求localhost:8080/users/register确定服务是否正常启动

# 框架与技术
BitsIM Server 主要为 BitsIM 客户端提供 Restful API。主要使用了以下框架及第三方服务：
* Tomcat  运行的容器
* Spring 提供依赖注入
* SpringMVC 主要提供 Restful 支持
* MyBatis 数据库映射框架
* MySQL 数据库，存储用户信息，MongoDB 存储聊天信息
* Redis 缓存存储层，热点数据缓存/提取
* RabbitMQ 消息发送队列
* Protobuf 序列化和反序列化
* MQTT 即时通讯协议
* Paho MQTT Client 实现
* Netty 网络框架
* 七牛云 用户发送的图片，语言存储服务
* JPush 提供最核心的消息推送支持

# 服务端平台及技术选型

# 平台关键设计
## 链接合法性验证/安全性设计理念
mina 客户端和服务器端建立了一个连接，并且为这个链接打开一个长连接。建立一个 channel, 客户端登陆操作，服务器将连接和账号进行关联。当有一个请求到来的时候，首先检查连接是否合法，这个链接是否登陆，用户是否正确。
目前使用的是 IP 白名单策略，如果是白名单的 IP 则校验通过，否则就拒绝对方连接。但是，更为可靠的方式应该是基于加密体系的，同时采用 ssl 连接等方式。

## 解码和编码
编码它将对象序列化为字节数组，用于网络传输、数据持久化等用途。反之，解码（Decode）/ 反序列化（deserialization）把从网络、磁盘等读取的字节数组还原成原始对象。
从网络读取的 inbound 消息，需要经过解码，将二进制的数据报转换成应用层协议消息或者业务消息，才能够被上层的应用逻辑识别和处理；同理，用户发送到网络的 outbound 业务消息，需要经过编码转换成二进制字节数组（对于 Netty 就是 ByteBuf）才能够发送到网络对端。编码和解码功能是 NIO 框架的有机组成部分，无论是由业务定制扩展实现，还是 NIO 框架内置编解码能力，该功能是必不可少的。

netty 常用的解码器：
LineBasedFrameDecoder：回车换行解码器
DelimiterBasedFrameDecoder：分隔符解码器
FixedLengthFrameDecoder：固定长度解码器，它能够按照指定的长度对消息进行自动解码，开发者不需要考虑 TCP 的粘包 / 拆包等问题，非常实用。
LengthFieldBasedFrameDecoder 解码器
ObjectEncoder 编码器

## 心跳和超时检测/链路有效性检测
从技术层面来看，要解决链路的可靠性问题，必须周期性的对链路进行有效性检测。目前最流行和通用的做法就是心跳检测。

心跳检测机制分为三个层面：
* TCP 层面的心跳检测，即 TCP 的 Keep-Alive 机制，他的作用域是整个 TCP 协议栈
* 协议层的心跳检测，主要存在于长连接协议中，例如 SMPP 协议
* 应用层的心跳检测，他主要由各业务产品通过约定方式定时给对方发送心跳消息实现

心跳检测的目的就是确认当前链路可用，对方或者并且能够正常接收和发送消息。作为高可靠的 NIO 框架，Netty 也提供了心跳检测机制。

不同的协议，心跳检测机制也存在差异，归纳起来主要分为两类：
* Ping-Pong 型心跳：由通信一方定时发送 Ping 消息，对方接收到 Ping 消息之后，立即返回 Pong 应答消息给对方，属于请求 - 响应型心跳。
* Ping-Ping 型心跳：不区分心跳请求和应答，由通信双方按照约定定时向对方发送心跳 Ping 消息，它属于双向心跳。

心跳检测策略如下：
* 联系 N 次心跳检测都没有收到对方的 Pong 应答消息或 Ping 请求消息，则认为链路已经发生逻辑失效，这杯称作心跳超时。
* 读取和发送心跳消息的时候如果直接发生了 IO 异常，说明链路已经失效，这被称为心跳失败。
* 无论发生心跳超时还是心跳失败，都需要关闭链路，由客户端发起重连操作，保证链路能够恢复正常。

Netty 的心跳检测实际上是利用了链路空闲检测机制实现的，他的空闲检测机制分为三种：
* 读空闲，链路持续时间 t 没有读取到任何消息
* 写空闲，链路持续时间 t 没有发送任何消息
* 读写空闲，链路持续时间 t 没有接收或者发送任何消息

## 断连重连机制
当发生如下异常时，客户端需要释放资源，重新发起连接：
* 服务端因为某种原因，主动关闭连接，客户端检测到链路被正常关闭
* 服务端因为宕机等故障，强制关闭连接，客户端检测到链路被 Reset 掉
* 心跳检测超时，客户端主动关闭连接
* 客户端因为其他原因，强制关闭连接
* 网络类故障，例如网络丢包、超时、单通等，导致链路中断

客户端检测到链路中断后，等到 INTERVAL 时机，由客户端发起重连操作，如果重连失败，间隔周期 INTERVAL 后再次发起重连，直到重连成功。

为了保证服务端能够有充足的时间释放句柄资源在首次断连时客户端需要等到 INTERVAL 时机之后再发起重连，而不是失败后就立即重连。

为了保证句柄资源能够及时释放，无论什么场景下的重连失败，客户端都必须保证自身资源被及时释放，包括但不限于 SocketChannel、Socket 等。重连失败后，需要打印异常堆栈信息，方便后续的问题定位。

## 消息缓存重发
当我们调用消息发送接口的时候，消息并没有真正被写入到 Socket 中，而是先放入 NIO 通信框架的消息发送队列中，由 Reactor 线程扫描待发送的消息队列，异步发送给通信对端。假设消息队列中积压了部分消息，此时链路中断，这回导致部分消息并没有真正发送给通信对端。

发生此故障时，我们希望 NIO 框架能够自动实现消息缓存和重新发送，遗憾的是作为基础的 NIO 通信框架，无论是 Mina 还是 Netty，都没有提供该功能，需要通信框架自己封装实现，基于 Netty 的实现策略如下：
* 调用 Netty ChannelHandlerContext 的 write() 方法时，返回 ChannelFuture 对象，我们在 ChannelFuture 中注册发送结果监听 Listener。
* 在 Listener 的 operationComplete 方法中判断操作结果，如果操作不成功，将之前发送的消息对象添加到重发队列中。
* 链路重连成功后，根据策略，将缓存队列中的消息重新发送给通信对端。

## 资源优雅释放
Java 的优雅停机通常通过注册 JDK 的 ShutdownHook 实现，当系统接收到退出指令后，首先标记系统处于退出状态，不再接收新的消息，然后将积压的消息处理完，最后调用资源回收接口将资源销毁，最后各线程退出执行。

通常优雅退出有个时间限制，例如 30s，如果到达执行时间仍然没有完成退出前的操作，则由监控脚本直接 kill -9 pid，强制退出。

Netty 提供了完善的优雅停机接口 shutdownGracefully，通过调用相关接口，可以实现线程池、消息队列、Socket 句柄、多路复用器等的资源释放。

## 消息存储
采用 Mysql 来存储
消息量的确非常大：HBase 来存储
我个人认为一般的应用 Mysql 足矣, 分库分表做好了, 数据存储问题不大。

## 主题订阅
客户端订阅感兴趣的主题，服务端选择匹配的主题将收到的即时消息推送到对应的客户端，通过这种方式实现不同移动终端间的通信。本文设计了 3 种类型的主题，客户端通过订阅这 3 种类型的主题，可以实现通讯录好友间的单聊、群组聊天、状态呈现功能。这 3 种类型的主题分别为： 
* f/：通讯录类主题，这类主题由前缀 “f/” 加用户名构成，通过订阅这类主题可以接收到通讯录内好友的即时消息。 
* g/：群组类主题，这类主题由前缀 “g/” 加群组名称构成，通过订阅这类主题可以接收到群组内即时消息。 
* s/：状态类主题，这类主题由前缀 “s/” 加用户名构成，通过订阅这类主题可以接收到状态改变类型的即时消息。


# 热点问题考量
## 系统性能考量
* 编码角度：采用高效的网络模型，线程模型，I/O 处理模型，合理的数据库设计和操作语句的优化；
* 垂直扩展：通过提高单服务器的硬件资源或者网络资源来提高性能；
* 水平扩展：通过合理的架构设计和运维方面的负载均衡策略将负载分担，有效提高性能；后期甚至可以考虑加入数据缓存层，突破 IO 瓶颈；

## 系统的高可用性（防止单点故障）
* 在架构设计时做到业务处理和数据的分离，从而依赖分布式的部署使得在单点故障时能保证系统可用。
* 对于关键独立节点可以采用双机热备技术进行切换。
* 数据库数据的安全性可以通过磁盘阵列的冗余配置和主备数据库来解决。


# 待实现功能
协议加密
快速链接（掉线重连机制）
连接保持（即心跳机制）
消息可达（即 QoS 机制）
文件上传优化


# API


# MQTT协议详解
MQTT 整个协议的组成，可以分为三个部分：
1. 固定头部：通用消息数据包格式
2. 可变头部：特定消息数据包格式
3. 消息体：有效载荷


# 实现详解
* Message：
    * FixedHeader：
        * MessageType messageType: 
        * boolean dup: 控制报文的重复分发标志
        * QoS qos: PUBLISH 报文的服务质量等级
        * boolean retain: PUBLISH 报文的保留标志
        * int messageLength: 剩余长度
    * variableHeader
    * payload
    * FixedHeader getXxxFixedHeader()
    
## Message
### CONNECT – 连接服务端
* ConnectMessage
* ConnectVariableHeader
    * String protocolName: 协议名
    * byte protocolVersionNumber: 协议级别，值为4
    * Connect Flags: 连接标志
        * boolean reservedIsZero: 服务端必须验证该保留标志位是否为0，如果不为 0 必须断开客户端连接
        * boolean cleanSession: 指定了会话状态的处理方式
        * boolean hasWill: 遗嘱标志
        * QoS willQoS: 遗嘱
        * boolean willRetain: 遗嘱保留
        * boolean hasPassword: 用户名标志
        * boolean hasUsername: 密码标志
    * int keepAlive: 保持连接/心跳包时长
* ConnectPayload
    * String clientId: 客户端标识符
    * String willTopic: 遗嘱主题
    * String willMessage: 遗嘱消息
    * String username: 用户名
    * String password: 密码
### CONNACK – 确认连接请求
* ConnAckMessage
    * enum ConnectionStatus
        * ACCEPTED: 连接已接受
        * UNACCEPTABLE_PROTOCOL_VERSION: 连接已拒绝，不支持的协议版本
        * IDENTIFIER_REJECTED: 连接已拒绝，不合格的客户端标识符
        * SERVER_UNAVAILABLE: 连接已拒绝，服务端不可用
        * BAD_USERNAME_OR_PASSWORD: 连接已拒绝，无效的用户名或密码
        * NOT_AUTHORIZED: 连接已拒绝，未授权
* ConnAckVariableHeader
    * Boolean sessionPresent: 当前会话，告知客户端服务器是否存储了session的位
    * ConnectionStatus status: 连接确认标志/连接返回码
* ConnAckPayload：无
### PUBLISH – 发布消息
* PublishMessage
* PublishVariableHeader
    * String topic: 主题名
    * int packetIdentifier: 报文标识符
* PublishPayload: 包含将被发布的应用消息
### PUBACK –发布确认
* ConnectMessage
* ConnAckVariableHeader
    * : 
    * : 
    * : 
        * : 
* ConnectPayload: 

### PUBREC – 发布收到
* ConnectMessage
* ConnAckVariableHeader
    * : 
    * : 
    * : 
        * : 
* ConnectPayload: 

### PUBREL – 发布释放
* ConnectMessage
* ConnAckVariableHeader
    * : 
    * : 
    * : 
        * : 
* ConnectPayload: 

### PUBCOMP – 发布完成
* ConnectMessage
* ConnAckVariableHeader
    * : 
    * : 
    * : 
        * : 
* ConnectPayload: 

### SUBSCRIBE - 订阅主题
* ConnectMessage
* ConnAckVariableHeader: 包含客户端标识符
* SubscribePayload: 
    * List<TopicSubscribe> topicSubscribes
        * String topicFilter: 主题过滤器
        * QoS qos: 

### SUBACK – 订阅确认
* SubAckMessage: 
* SubAckVariableHeader: 包含等待确认的 SUBSCRIBE 报文的报文标识符PackageIdVariableHeader
* SubAckPayload:  
    * List<Integer> grantedQosLevel: 返回码清单。每个返回码对应等待确认的 SUBSCRIBE 报文中的一个主题过滤器。
### UNSUBSCRIBE –取消订阅
* UnSubscribeMessage: 
* UnSubscribeVariableHeader: PackageIdVariableHeader
    * : 
* UnSubscribePayload: 
    * List<String> topics: 客户端想要取消订阅的主题过滤器列表
### UNSUBACK – 取消订阅确认
* UnSubAckKMessage
* UnSubAckVariableHeader
    * : 
    * : 
    * : 
        * : 
* UnSubAckPayload: 
### PINGREQ – 心跳请求
* PingReqMessage
* PingReqVariableHeader: 没有可变报头
* PingReqPayload: 没有有效载荷

### PINGRESP – 心跳响应
* PingRespMessage
* PingRespVariableHeader: 没有可变报头
* PingRespPayload: 没有有效载荷

### DISCONNECT –断开连接
* DisconnectMessage
* DisconnectVariableHeader: 没有可变报头
* DisconnectPayload: 没有有效载荷




# 数据库设计
## user
user_id
username
password
nickname
groupid


msg_id
msg_content
msg_from
msg_to
msg_isprivate

群聊
public_msg_id
public_msg_content
public_msg_from
public_msg_to
私聊 
privite_msg_id
privite_msg_content
privite_msg_from
privite_msg_to

int             -------     int(11)
bigint       -------     bigint(20)
mediumint   --------    mediumint()
smallint   -------     smallint(6)
tinyint     -------     tinyint(4)

## 群组基本信息`group_base_property`
列名 | 说明 | 类型 | 长度 | 是否主键 | 可否为空 | 描述
--- | --- | --- | --- | --- | --- | ---
group_id | 群组 ID | int | 11 | Yes | No | 
group_creater_id | 创建者 ID | int | 11 |  | NO |  
group_create_datetime | 创建时间 | timestamp | 13 |  | NO | 
group_status | 状态 | tinyint | 4 |  | NO | 1可用 2不可用（例如被禁用的群）

## 用户关系表`friend`
列名 | 说明 | 类型 | 长度 | 是否主键 | 可否为空 | 描述
--- | --- | --- | --- | --- | --- | ---
user_id |  |  |  |  |  | 
friend_id |  |  |  |  |  | 
status |  |  |  |  |  | 黑名单、待审核、正常

## 
列名 | 说明 | 类型 | 长度 | 是否主键 | 可否为空 | 描述
--- | --- | --- | --- | --- | --- | ---
 |  |  |  |  |  | 
 |  |  |  |  |  | 
 |  |  |  |  |  | 

## 
列名 | 说明 | 类型 | 长度 | 是否主键 | 可否为空 | 描述
--- | --- | --- | --- | --- | --- | ---
 |  |  |  |  |  | 
 |  |  |  |  |  | 
 |  |  |  |  |  | 

## 
列名 | 说明 | 类型 | 长度 | 是否主键 | 可否为空 | 描述
--- | --- | --- | --- | --- | --- | ---
 |  |  |  |  |  | 
 |  |  |  |  |  | 
 |  |  |  |  |  | 

## 
列名 | 说明 | 类型 | 长度 | 是否主键 | 可否为空 | 描述
--- | --- | --- | --- | --- | --- | ---
 |  |  |  |  |  | 
 |  |  |  |  |  | 
 |  |  |  |  |  | 

[IM 开源项目 群组服务 数据库设计 （01）](http://blog.csdn.net/jptaozhantaozhan/article/details/7286457)  
 


# 协议编解码功能实现
http://www.infoq.com/cn/articles/netty-codec-framework-analyse/  

从网络读取的 inbound 消息，需要经过解码，将二进制的数据报转换成应用层协议消息或者业务消息，才能够被上层的应用逻辑识别和处理；同理，用户发送到网络的 outbound 业务消息，需要经过编码转换成二进制字节数组（对于 Netty 就是 ByteBuf）才能够发送到网络对端。编码和解码功能是 NIO 框架的有机组成部分，无论是由业务定制扩展实现，还是 NIO 框架内置编解码能力，该功能是必不可少的。

* Encoder：将用户定义的类型转化为 byte 类型，这样才能通过 channel 发送出去
* Decoder：将读取的 byte 数据转化为用户自己定义的数据类型

## 编码
通过继承MessageToByteEncoder类，而MessageToByteEncoder继承了ChannelOutboundMessageHandlerAdapter（一个比较粗糙的实现方法，用户可以继承它然后重写自己感兴趣的方法）。

我们知道在 pipeline 上面调用 write 方法的时候，netty 会从 pipeline 的后面向前寻找合适的 outboundhandler 用于处理要写的数据，而且是先将数据存放到 handler 的 buffer 里面，真正的写数据则是调用 flush 方法实现的。

### 常用的编码器
* ObjectEncoder 编码器：Java 序列化编码器，它负责将实现 Serializable 接口的对象序列化为 byte []，然后写入到 ByteBuf 中用于消息的跨网络传输。
* MessageToByteEncoder 抽象编码器：负责将 POJO 对象编码成 ByteBuf，用户的编码器继承 Message ToByteEncoder，实现 `void encode(ChannelHandlerContext ctx, I msg, ByteBuf out)` 接口
* MessageToMessageEncoder：


## 解码
### 常用的解码器
* LineBasedFrameDecoder 解码器：用户发送的消息以回车换行符（ “\n” 或者 “\r\n”）作为消息结束的标识。  
通常情况下，LineBasedFrameDecoder 会和 StringDecoder 配合使用，组合成按行切换的文本解码器，对于文本类协议的解析，文本换行解码器非常实用，例如对 HTTP 消息头的解析、FTP 协议消息的解析等。
* DelimiterBasedFrameDecoder 解码器：分隔符解码器，用户可以指定消息结束的分隔符，它可以自动完成以分隔符作为码流结束标识的消息的解码。
* FixedLengthFrameDecoder 解码器：固定长度解码器，它能够按照指定的长度对消息进行自动解码，开发者不需要考虑 TCP 的粘包 / 拆包等问题，非常实用。
* LengthFieldBasedFrameDecoder 解码器：
* ByteToMessageDecoder 抽象解码器：抽象工具解码类。  
实际项目中，通常将 LengthFieldBasedFrameDecoder 和 ByteToMessageDecoder 组合使用，前者负责将网络读取的数据报解码为整包消息，后者负责将整包消息解码为最终的业务对象。
* MessageToMessageDecoder：Netty 的二次解码器，它的职责是将一个对象二次解码为其它对象。


```
processConnect



 INFO [nioEventLoopGroup-1-0] - 处理Connect的数据
DEBUG [nioEventLoopGroup-1-0] - 连接的心跳包时长是 {300} s
 INFO [nioEventLoopGroup-1-0] - CONNACK处理完毕并成功发送
 INFO [nioEventLoopGroup-1-0] - 连接的客户端clientID=5A-86-C4-12-40-DA, cleanSession为false
 INFO [nioEventLoopGroup-1-0] - 没有客户端{5A-86-C4-12-40-DA}存储的离线消息
 INFO [nioEventLoopGroup-1-0] - 处理subscribe数据包，客户端ID={5A-86-C4-12-40-DA},cleanSession={false}
 INFO [nioEventLoopGroup-1-0] - 订阅topic{test/topic},Qos为{AT_MOST_ONCE}

 INFO [nioEventLoopGroup-1-0] - 添加新订阅，订阅:[filter:test/topic, cliID: 5A-86-C4-12-40-DA, qos: AT_MOST_ONCE, active: true],客户端ID:5A-86-C4-12-40-DA
 INFO [nioEventLoopGroup-1-0] - 客户端ID{5A-86-C4-12-40-DA}不存在订阅集 , 为它创建订阅集
 INFO [nioEventLoopGroup-1-0] - 更新客户端5A-86-C4-12-40-DA的订阅集
DEBUG [nioEventLoopGroup-1-0] - 客户端5A-86-C4-12-40-DA的订阅集现在是这样的[[filter:test/topic, cliID: 5A-86-C4-12-40-DA, qos: AT_MOST_ONCE, active: true]]

 INFO [nioEventLoopGroup-1-0] - 回写subAck消息给订阅者，包ID={1}
```

[[filter:test/topic, clientId: D1-F9-8B-B6-58-78, qos: AT_MOST_ONCE, active: true]]
[[filter:test/topic, clientId: A1-86-49-15-A2-10, qos: AT_MOST_ONCE, active: true]]


# 心跳处理
## 超时机制
Netty 的超时类型 IdleState 主要分为：
ALL_IDLE : 一段时间内没有数据接收或者发送
READER_IDLE ： 一段时间内没有数据接收
WRITER_IDLE ： 一段时间内没有数据发送

在 Netty 的 timeout 包下，主要类有：
IdleStateEvent ： 超时的事件
IdleStateHandler ： 超时状态处理，这个类可以对三种类型的心跳检测
ReadTimeoutHandler ： 读超时状态处理
WriteTimeoutHandler ： 写超时状态处理
### IdleStateEvent
IdleStateHandler 不是发心跳包，而是触发心跳机制，在你设定的时候内没有收到包，就触发读心跳，没有发包就触发写心跳，如果都没有，就触发 all
```java
/**
 * 前三个的参数解释如下：
 * 1）readerIdleTime：为读超时时间（即测试端一定时间内未接受到被测试端消息），单位是秒
 * 2）writerIdleTime：为写超时时间（即测试端一定时间内向被测试端发送消息），单位是秒
 * 3）allIdleTime：所有类型的超时时间，单位是秒
*/
public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {

}
```
这个类主要也是一个 ChannelHandler，也需要被载入到 ChannelPipeline 中，加入我们在服务器端的 ChannelInitializer 中加入如下的代码：
```java
int keepAlive = 5;

channel.pipeline().addFirst("idleStateHandler", new IdleStateHandler(keepAlive, Integer.MAX_VALUE, Integer.MAX_VALUE, TimeUnit.SECONDS));
```
我们在 channel 链中加入了 IdleSateHandler，第一个参数是 5，单位是秒，那么这样做的意思就是：在服务器端会每隔 5 秒来检查一下 channelRead 方法被调用的情况，如果在 5 秒内该链上的 channelRead 方法都没有被触发，就会调用 userEventTriggered 方法。

简而言之：
IdleStateHandler 这个类会根据你设置的超时参数的类型和值，循环去检测 channelRead 和 write 方法多久没有被调用了，如果这个时间超过了你设置的值，那么就会触发对应的事件，read 触发 read，write 触发 write，all 触发 all  
如果超时了，则会调用 userEventTriggered 方法，且会告诉你超时的类型  
如果没有超时，则会循环定时检测，除非你将 IdleStateHandler 移除 Pipeline


# Connect过程处理
1. 查看保留位是否为0，不为0则断开连接，协议P24
2. 处理Protocol Name和Protocol Version，如果返回码!=0，sessionPresent必为0，协议P24,P32
3. 处理clientId为null或长度为0的情况，协议P29
5. 检查clientId的格式符合与否
4. 如果会话中已经存储了这个新连接的ID，就关闭之前的clientId
6. 若至此没问题，则将新客户端连接加入client的维护列表中
7. 处理心跳包时间，把心跳包时长和一些其他属性都添加到会话中，方便以后使用
    8. 协议P29规定，在超过1.5个keepAlive的时间以上没收到心跳包PingReq，就断开连接(但这里要注意把单位是s转为ms)
    9. 添加心跳机制处理的Handler
处理Will flag（遗嘱信息）,协议P26
处理身份验证（userNameFlag和passwordFlag）
处理cleanSession为1的情况：移除所有之前的session并开启一个新的，并且原先保存的subscribe之类的都得从服务器删掉
处理回写的CONNACK,并回写，协议P29
    协议32,session present的处理
如果cleanSession=0,需要在重连的时候重发同一clientId存储在服务端的离线信息