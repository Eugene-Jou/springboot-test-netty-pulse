package com.eugene.test.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EchoClientHandle继承了 ChannelInboundHandlerAdapter 的一个扩展(SimpleChannelInboundHandler),
 * 而ChannelInboundHandlerAdapter是ChannelInboundHandler的一个实现
 * ChannelInboundHandler提供了可以重写的各种事件处理程序方法
 * 目前，只需继承 SimpleChannelInboundHandler或ChannelInboundHandlerAdapter 而不是自己实现处理程序接口。
 * 我们在这里重写了channelRead0（）事件处理程序方法
 */
public class NettyClientHandle extends SimpleChannelInboundHandler<ByteBuf> {

    private final static Logger LOGGER = LoggerFactory.getLogger(NettyClientHandle.class);

    /**
     * 断开连接后调用此方法可重连
     *
     * @param ctx
     * @return void
     * @author eugene
     * @date 2019/11/22 16:37
     **/
    @SuppressWarnings("static-access")
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("与服务器连接断开，尝试重新连接...");
        NettyClient.retryConnectFlag = true;
        final EventLoop eventLoop = ctx.channel().eventLoop();
        // 立即重连
        NettyClient.doConnect(new Bootstrap(), eventLoop);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                //向服务端发送消息
                StringBuffer heartBeatBuffer = new StringBuffer("{\"deviceId\":\"C123\",\"content\":\"333333333333333333333333333333333333333333333333\"}");
                ctx.writeAndFlush(heartBeatBuffer.toString()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 服务端发消息都会到这儿来 接收消息的类型是ByteBuf。
     *
     * @param channelHandlerContext
     * @param byteBuf
     * @return void
     * @author eugene
     * @date 2019/11/22 16:38
     **/
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        //从服务端收到消息时被调用
        LOGGER.info("收到服务端发来的消息={}" , byteBuf.toString(CharsetUtil.UTF_8));
    }
}
