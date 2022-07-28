package netty.guide.chapter2.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author flynn
 * @date 2022/07/27
 */
@Slf4j
public class MultiplexerTimeServer implements Runnable {
    private Selector selector;

    private ServerSocketChannel servChannel;
    private volatile boolean stop;

    public MultiplexerTimeServer(int port) {
        try {
            // 创建多路复用器Selector
            selector = Selector.open();

            // 创建ServerSocketChannel
            servChannel = ServerSocketChannel.open();
            // 对channel和TCP参数进行配置
            // 1.设置ServerSocketChannel为非阻塞模式
            servChannel.configureBlocking(false);
            // 2.绑定端口，并将backlog设置为1024
            servChannel.socket().bind(new InetSocketAddress(port), 1024);


            // 将ServerSocketChannel注册到Selector, 监听Selection.OP_ACCEPT操作位
            servChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.info("The time server is start in port : {}", port);
        } catch (IOException e) {
            log.error("{}, {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                // 1s唤醒一次，无论是否有读写事件发生。
//                selector.select(1000);
                // 无参的select:当有处于就绪状态的Channel时，selector将返回该Channel的SelectionKey集合。通过对就绪状态的Channel集合进行迭代，可以进行网络的异步读写操作。
                selector.select();

                // 遍历处理SelectionKey集合
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        // 处理事件
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                log.error("{}, {}", t.getMessage(), t);
            }
        }

        if (this.selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            // 处理新接入的客户端请求
            if (key.isAcceptable()) {
                // 获取到ServerSocketChannel
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                // accept()接收客户端的连接请求，并创建SocketChannel实例
                SocketChannel sc = ssc.accept();

                // 完成上述操作，相当于完成了TCP的三次握手，TCP物理链路的正式建立。
                // 注意,我们需要将新创建的SocketChannel设置为异步非阻塞，同时也可以对其TCP参数进行设置，例如TCP接收和发送缓冲区的大小等。

                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }

            // 读取客户端的请求消息
            if (key.isReadable()) {
                // read data
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                // SocketChannel设置为非阻塞模式，因此它的read是非阻塞的。使用返回值进行判断，看读取到的字节数：0，-1，大于0
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    // 读取到数据
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    log.info("The time server receive order : {}", body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString() : "BAD ORDER";

                    // 应答
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    // -1，链路已关闭，需要关闭SocketChannel，释放资源
                    key.cancel();
                    sc.close();
                } else {
                    // 读到0字节，忽略
                }
            }

        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer);
        }
    }


}
