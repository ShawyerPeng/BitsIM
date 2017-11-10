import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 启动服务器，主线程所在
 */
public class StartServer {
    public static void main(String[] args) throws IOException, TimeoutException {
        final TcpServer tcpServer = new TcpServer();
        // 启动服务器
        ChannelFuture future = tcpServer.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    tcpServer.destory();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        });

        future.channel().closeFuture().syncUninterruptibly();
    }
}
