import addition.ClientProperties;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Temp {

    public static void main(String[] args) {

        Path pathOut = Paths.get("C:\\Users\\gusko\\Downloads\\Роберт Лафоре - Структуры данных и алгоритмы JAVA, 2-е изд (Классика Computer Science) - 2012.pdf");
        Path pathIn = Paths.get("D:\\SERVER\\user2\\Роберт Лафоре - Структуры данных и алгоритмы JAVA, 2-е изд (Классика Computer Science) - 2012.pdf");

        int lengthBuffer = 1024*1024;
        long count;

        ByteBuffer byteBuffer = ByteBuffer.allocate(lengthBuffer);

        try (
                RandomAccessFile rafIn = new RandomAccessFile(pathIn.toString(), "rws");
                RandomAccessFile rafOut = new RandomAccessFile(pathOut.toString(), "rws");
                )
        {
            count = Files.size(pathOut) / lengthBuffer;
            int i = 0;
            System.out.println(count);

            while (i <= count){
                byteBuffer.clear();
                rafOut.getChannel().read(byteBuffer);
                byteBuffer.flip();
                rafIn.getChannel().write(byteBuffer);
                i++;
            }


        } catch (Exception e){
            e.printStackTrace();
        }


    }


    public static void varOne() throws IOException {

        int lengthBuffer = 1024*1024;
        long count;

        Path pathOut = Paths.get("C:\\Users\\gusko\\Downloads\\LibreOffice_6.4.0_Win_x86.msi");
        Path pathIn = Paths.get("D:\\SERVER\\user2\\LibreOffice_6.4.0_Win_x86.msi");

        if (Files.exists(pathOut)) {

            count = Files.size(pathOut) / lengthBuffer;
            System.out.println("Будет скопировано " + count + " блоков.");

            try (
                    RandomAccessFile rafIn = new RandomAccessFile(pathIn.toString(), "rws");
                    RandomAccessFile rafOut = new RandomAccessFile(pathOut.toString(), "rws");
            ) {
                int read = 0;
                byte[] in = new byte[lengthBuffer];

                int y = 0;
                while ((read = rafOut.read(in)) == in.length) {

                    rafIn.write(in);

//                rafOut.write(in);   //пишет в файл
//                rafOut.read(in);    //читает из файла

                    y++;
                    System.out.println("Скопировано " + y + " блоков из " + count);
                }

                System.out.println(read);
                for (int i = 0; i < read; i++) {
                    rafIn.write(in[i]);
                }

            }
        }
        System.out.println("end");
    }
}
