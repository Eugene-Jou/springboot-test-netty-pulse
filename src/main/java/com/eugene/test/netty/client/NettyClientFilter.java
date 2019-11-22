package com.eugene.test.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyClientFilter extends ChannelInitializer<Channel> {


    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                //2 秒没发送消息 将IdleStateHandler 添加到 ChannelPipeline 中
                .addLast(new IdleStateHandler(0, 2, 0))
                .addLast(new NettyEncode())
                .addLast(new NettyClientHandle());
    }
}
