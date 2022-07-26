package netty.guide.chapter2;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author flynn
 * @date 2022/07/26
 */
@Slf4j
public class TimeClient {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            port = Integer.valueOf(args[0]);
        }
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            socket = new Socket("127.0.0.1", port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("QUERY TIME ORDER");
            log.info("Send order to server succeed");
            String resp = in.readLine();
            log.info("Now is : {}", resp);
        } catch (Exception e) {
            log.error("message={}, e={}", e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
