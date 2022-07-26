package c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * author flynn
 * date 2022/01/30
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        // FileChannel
        try {
            // 输入输出流， RandomAccessFile
            FileChannel fileChannel = new FileInputStream("data.txt").getChannel();
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                int length = fileChannel.read(buffer);
                log.info("读取到的字节数：{}", length);
                if (length == -1) {
                    break;
                }
                // 切换：读模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();
                    log.info("{}", (char) b);
                }
                // 切换为：写模式
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        try (
                FileChannel from = new FileInputStream("data.txt").getChannel();
                FileChannel to = new FileOutputStream("to.txt").getChannel();
        ) {
            // 效率高，底层利用操作系统的零拷贝，最大2g数据。可以改进一下，size
            from.transferTo(0, from.size(), to);
            // 改进：
            long size = from.size();
            for (long left = size; left > 0; ) {
                left -= from.transferTo(0, size, to);
            }
        } catch (IOException e) {

        }

    }

}
