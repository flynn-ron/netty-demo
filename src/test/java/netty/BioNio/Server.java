package netty.BioNio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * author flynn
 * date 2022/02/01
 */
@Slf4j
public class Server {

    public static void main(String[] args) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(16);

        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 设置为非阻塞
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            SocketChannel sc = ssc.accept();
            if (sc != null) {
                log.info("connected.");
                // 设置为非阻塞
                sc.configureBlocking(false);
                channels.add(sc);
            }

            for (SocketChannel channel : channels) {
                int read = channel.read(buffer);
                if (read > 0) {
                    log.info("before read... {}", channel);
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        System.out.println((char) buffer.get());
                    }
                    buffer.clear();
                    log.info("after read... {}", channel);
                }
            }
        }
    }

}
