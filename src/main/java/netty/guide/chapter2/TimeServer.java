package netty.guide.chapter2;

import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author flynn
 * @date 2022/07/26
 */
@Slf4j
public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (Exception e) {
                // 采用默认端口
            }
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            log.info("The time server is start in port : {}", port);

//            processV1(server);
            processV2(server);

        } catch (Exception e) {

        } finally {
            if (server != null) {
                log.info("The time server close");
                server.close();
            }
        }
    }

    /**
     * 单线程处理
     *
     * @param server
     * @throws IOException
     */
    private static void processV1(ServerSocket server) throws IOException {
        Socket socket = null;
        while (true) {
            socket = server.accept();
            new Thread(new TimeServerHandler(socket)).start();
        }
    }

    /**
     * 多线程处理
     *
     * @param server
     * @throws IOException
     */
    private static void processV2(ServerSocket server) throws IOException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                7,
                13,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000));
        Socket socket = null;
        while (true) {
            socket = server.accept();
            threadPoolExecutor.execute(new TimeServerHandler(socket));
        }
    }

}
