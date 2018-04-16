package com.manning.nettyinaction.chapter17;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class EchoServer {

    EventExecutorGroup eventExecutorGroup1 = new DefaultEventExecutorGroup(8);
    EventExecutorGroup eventExecutorGroup2 = new DefaultEventExecutorGroup(8);
    EventExecutorGroup eventExecutorGroup3 = new DefaultEventExecutorGroup(8);
    EventExecutorGroup eventExecutorGroup4 = new DefaultEventExecutorGroup(8);

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup eventLoopGroup = null;
        try {
            //server端引导类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //连接池处理数据
            eventLoopGroup = new NioEventLoopGroup(8);
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)//指定通道类型为NioServerSocketChannel，一种异步模式，OIO阻塞模式为OioServerSocketChannel
                    .localAddress("localhost",port)//设置InetSocketAddress让服务器监听某个端口已等待客户端连接。
                    .childHandler(new ChannelInitializer<Channel>() {//设置childHandler执行所有的连接请求
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // 注册两个InboundHandler，执行顺序为注册顺序，所以应该是InboundHandler1 InboundHandler2
                            // 注册两个OutboundHandler，执行顺序为注册顺序的逆序，所以应该是OutboundHandler2 OutboundHandler1
                            ch.pipeline().addLast(eventExecutorGroup1, new EchoInHandler1());
                            ch.pipeline().addLast(new EchoInHandler5());
                            ch.pipeline().addLast(eventExecutorGroup2, new EchoOutHandler1());
                            ch.pipeline().addLast(eventExecutorGroup3, new EchoOutHandler2());
                            ch.pipeline().addLast(eventExecutorGroup4, new EchoInHandler2());

                        }
                    });
            // 最后绑定服务器等待直到绑定完成，调用sync()方法会阻塞直到服务器完成绑定,然后服务器等待通道关闭，因为使用sync()，所以关闭操作也会被阻塞。
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("开始监听，端口为：" + channelFuture.channel().localAddress());
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(20000).start();
    }
}
