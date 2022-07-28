package common;

import lombok.extern.slf4j.Slf4j;

/**
 * @author flynn
 * @date 2022/07/27
 */
@Slf4j
public class PortUtil {

    public static int getInitPort(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (Exception e) {
                log.error("{}, {}", e.getMessage(), e);
            }
        }
        return port;
    }

}
