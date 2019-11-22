package com.eugene.test.netty.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eugene.test.netty.utils.NettyHolderUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Set;

@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    private final static Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);


    /**
     * 设备断开调用此方法
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Set<String> deviceIdSet = NettyHolderUtils.getIdByChannel((NioSocketChannel) ctx.channel());
        if(!deviceIdSet.isEmpty()){
            //获取设备Id
            //String deviceId = deviceIdSet.iterator().next();
            //InetSocketAddress inetSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            //获取设备ip
            //String deviceIp = inetSocket.getAddress().getHostAddress();
            Boolean update = true;//去数据库里修改设备状态，如果成功则返回true
            if(update){
                //删除与 Channel 之间的关系
                NettyHolderUtils.remove((NioSocketChannel) ctx.channel());
            }else {
                //如果没有在库里找到就关闭连接
                handlerRemoved(ctx);
                ctx.channel().close();
                ctx.close();
            }
        }else {
            //如果没有在库里找到就关闭连接
            handlerRemoved(ctx);
            ctx.channel().close();
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                //LOGGER.info("已经5秒没有收到信息！");
                Set<String> deviceIdSet = NettyHolderUtils.getIdByChannel((NioSocketChannel) ctx.channel());
                if(!deviceIdSet.isEmpty()){
                    String deviceId = deviceIdSet.iterator().next();
                    //deviceEntity是数据库里设备的实体这儿就先new一个  生产环境中应去数据库中查询
                    Object deviceEntity = new Object();
                    if (!deviceId.isEmpty() && deviceEntity != null) {
                        StringBuffer heartBeatBuffer = new StringBuffer("{\"deviceId\":\"" + deviceId + "\",\"content\":\"返回\"}");
                        ByteBuf HEART_BEAT = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(heartBeatBuffer, CharsetUtil.UTF_8));
                        //向客户端发送消息
                        ctx.writeAndFlush(HEART_BEAT).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    }else {
                        handlerRemoved(ctx);
                        ctx.channel().close();
                        ctx.close();
                    }
                }


            }
        }
        super.userEventTriggered(ctx, evt);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        LOGGER.info("收到客服端发来的消息={}", s);
        //我们调用writeAndFlush（Object）来逐字写入接收到的消息并刷新线路
        ctx.writeAndFlush(s);
        JSONObject jsonObject = JSON.parseObject(s);
        String deviceId = jsonObject.getString("deviceId");
        InetSocketAddress inetSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String deviceIp = inetSocket.getAddress().getHostAddress();
        //去数据库里面查询是否存在 存在 update:true  不存在 update:false
        boolean update = true;
        if(update){
            //保存客户端与 Channel 之间的关系
            NettyHolderUtils.put(deviceId, (NioSocketChannel) ctx.channel());
        }else {
            //如果没有在库里找到就关闭连接
            handlerRemoved(ctx);
            ctx.channel().close();
            ctx.close();
        }

    }

}
