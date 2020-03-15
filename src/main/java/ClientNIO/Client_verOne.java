/**
 * первая стабильная версия получения буфера из сети
 */
package ClientNIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client_verOne {
    public static void main(String[] args) throws IOException, InterruptedException {

        SocketChannel sc = SocketChannel.open(new InetSocketAddress(8189));

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byte[] tmpArray;

        System.out.println("1 step");
        sc.read(byteBuffer);
        int tmpSize = byteBuffer.position();
        tmpArray = new byte[tmpSize];

        System.out.println("2 step");
        byteBuffer.flip();

        System.out.println("3 step");
        byteBuffer.get(tmpArray);

        System.out.println(new String(tmpArray));
        System.out.println("Посылка получена. Режим сна.");

        Thread.sleep(15000);
        System.out.println("end");

        sc.finishConnect();
    }
}
