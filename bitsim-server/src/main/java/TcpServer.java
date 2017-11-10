import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import protocol.MqttDecoder;
import protocol.MqttEncoder;
import protocol.MqttProcess;
import util.MqttTool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * 基于 JAVA AIO 的, 面向 TCP/IP 的, 非阻塞式 Socket 服务器框架类
 */
public class TcpServer {
    private final static Logger Log = Logger.getLogger(TcpServer.class);

    // 系统常量配置
    private static final String PORT = "mqtt.port";// 端口号

    private volatile Integer port;// 服务器端口
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public TcpServer() {
        // 从配置中获取端口号
        this.port = MqttTool.getPropertyToInt(PORT);
        if (this.port == null) {
            this.port = 1883;// 设置默认端口为 1883
        }
    }

    /**
     * 启动服务器
     */
    public ChannelFuture startServer() throws IOException, TimeoutException {
        // 事件循环组，用于管理多个 channel 的线程组
        // 前者线程组负责连接的处理，即是用于接受客户端的连接，后者线程组负责 handler 消息数据的处理，即是用于处理 SocketChannel 网络读写
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        // 为服务器启动进行一些配置
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置循环线程组，前者用于处理客户端连接事件，后者用于处理网络IO
        bootstrap.group(bossGroup, workerGroup)
                // 用于构造socketchannel工厂
                .channel(NioServerSocketChannel.class)
                // 为处理accept客户端的channel中的pipeline添加自定义处理函数
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch)
                            throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("MqttDecoder", new MqttDecoder());
                        pipeline.addLast("MqttEncoder", new MqttEncoder());
                        pipeline.addLast("MqttProcess", new MqttProcess());
                        // 心跳处理在收到 CONNECT 消息协议的时候，根据协议内容动态添加
                    }
                })
                // 标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 是否启用心跳保活机制
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            // 绑定端口（实际上是创建ServerSocketChannnel，并注册到EventLoop上），同步等待完成，返回相应channel
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
            channel = future.channel();
            Log.info("服务器已启动，端口：" + port);
            return future;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 优雅退出
     */
    public void destory() throws IOException, TimeoutException {
        if (channel != null) {
            channel.close();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
