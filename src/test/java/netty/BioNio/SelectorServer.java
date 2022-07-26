package netty.BioNio;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * author flynn
 * date 2022/02/01
 */
@Slf4j
public class SelectorServer {


    public static void main(String[] args) throws IOException {
        // 1.创建selector，管理多个channel
        Selector selector = Selector.open();
//        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // 2.建立selector 和 channel的联系
        // SelectionKey就是将来事件发生后，通过它可以知道事件和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. select 方法，没有事件发生，线程阻塞。有事件，线程才会恢复运行
            // 在事件未处理时，他不会阻塞，事件发生后要么处理，要么取消，不能置之不理
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                log.info("key:{}", key);
                // 区分事件类型，做不同处理
                if (key.isAcceptable()) {

                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.info("{}", sc);

                } else if (key.isReadable()) {
                    //
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        // 正常断开，read方法的返回值为-1；否则若是异常断开
                        int readResult = channel.read(buffer);
                        if (readResult == -1) {
                            // 正常断开，取消key
                            key.channel();
                        } else {
                            buffer.flip();
                            log.info("{}", StandardCharsets.UTF_8.decode(buffer));
                        }
                    } catch (Exception e) {
                        //如果因为客户端断开了，要将key取消(从selector中的keys集合中真正移除key)
                        e.printStackTrace();
                        key.channel();
                    }
                } else if (key.isConnectable()) {

                } else if (key.isWritable()) {

                } else {

                }
            }
        }
    }

    private static void handleKeyIsAcceptable(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel sc = channel.accept();
        sc.configureBlocking(false);
        SelectionKey scKey = sc.register(selector, 0, null);
        scKey.interestOps(SelectionKey.OP_READ);
        log.info("{}", sc);
    }

    private void handleKeyIsReadable(SelectionKey key) {

    }

    private void handleKeyIsWritable(SelectionKey key) {

    }

    private void handleKeyIsConnected(SelectionKey key) {
        String a = "asd";


    }
}
