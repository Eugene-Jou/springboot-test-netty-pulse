package com.eugene.test.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class NettyClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    public static String host = "127.0.0.1";  //ip地址
    public static int port = 30000;          //端口
    /// 通过nio方式来接收连接和处理连接   
    private static EventLoopGroup group = new NioEventLoopGroup();
    private static Bootstrap bootstrap = new Bootstrap();
    private static Channel channel;
    /**
     * 重连标志
     */
    public static boolean retryConnectFlag = false;


    /**
     * 启动连接 客户端的是Bootstrap
     *
     * @param args
     * @return void
     * @author eugene
     * @date 2019/11/22 16:36
     **/
    public static void main(String[] args) throws InterruptedException, IOException {
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new NettyClientFilter());
        // 连接服务端
        channel = bootstrap.connect(host, port).sync().channel();
    }


    /**
     * netty client 连接，连接失败10秒后重试连接
     *
     * @param bootstrap
     * @param eventLoopGroup
     * @return void
     * @author eugene
     * @date 2019/11/22 16:36
     **/
    public static void doConnect(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {
        ChannelFuture f = null;
        try {
            if (bootstrap != null) {
                bootstrap.group(eventLoopGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.handler(new NettyClientFilter());
                bootstrap.remoteAddress(host, port);
                bootstrap.connect().addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        // TODO Auto-generated method stub

                    }
                });
                // 	Cache.channel为缓存的与服务端的连接channel,在其他发送消息的时候使用。
                bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        LOGGER.info("客户端与服务端建立连接失败，10s之后尝试重连...");
                        // 10s秒之后重连
                        eventLoop.schedule(() -> doConnect(new Bootstrap(), eventLoop), 10, TimeUnit.SECONDS);
                    } else {
                        if (retryConnectFlag) {
                            // 如果连接成功后，再次断开，则尝试重连。
                            retryConnectFlag = false;
                            LOGGER.info("客户端重连成功，port：{}" + port);
                        } else {
                            // 客户端首次成功连接服务端后，这里有个验证登录服务端的动作（不要验证可以取消）
                            LOGGER.info("客户端与服务器连接成功，port：{}，开始登录服务端..." + port);
                        }
                    }
                }).channel();
            }
        } catch (Exception e) {
            LOGGER.info("连接客户端失败,失败信息：" + e);
        } finally {
            // 要实现重连，所以不能关闭loop
            //loop.shutdownGracefully();
        }
    }


}
