package com.eugene.test.netty.utils;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class NettyHolderUtils {

    private static final Map<String, NioSocketChannel> MAP = new ConcurrentHashMap<String, NioSocketChannel>();

    public static void put(String id, NioSocketChannel socketChannel) {
        MAP.put(id, socketChannel);
    }

    public static NioSocketChannel get(String id) {
        return MAP.get(id);
    }

    public static Map<String, NioSocketChannel> getMAP() {
        return MAP;
    }

    public static void remove(NioSocketChannel nioSocketChannel) {
        MAP.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> MAP.remove(entry.getKey()));
    }

    public static Set<String> getIdByChannel(NioSocketChannel nioSocketChannel){
        return MAP.entrySet().stream().filter(kvEntry -> Objects.equals(kvEntry.getValue(), nioSocketChannel)).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}
