package netty.guide.chapter2.nio;

import common.PortUtil;

import java.io.IOException;

/**
 * @author flynn
 * @date 2022/07/27
 */
public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = PortUtil.getInitPort(args);
        new Thread(new MultiplexerTimeServer(port)).start();
    }
}
