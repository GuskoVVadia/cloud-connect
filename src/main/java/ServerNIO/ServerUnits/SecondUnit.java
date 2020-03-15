package ServerNIO.ServerUnits;

import CloudShare.CommandChannel;
import CloudShare.ServerProperties;
import ServerNIO.addition.Person;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class SecondUnit implements Runnable{

    private SocketChannel socketChannel;
    private ServerProperties properties;
    private Person clientInfo;

    private ByteBuffer byteBuffer;
    private CommandChannel currentState;

    public SecondUnit(SocketChannel socketChannel, ServerProperties properties, Person clientInfo) {
        this.socketChannel = socketChannel;
        this.properties = properties;
        this.clientInfo = clientInfo;
        this.byteBuffer = ByteBuffer.allocate(this.properties.getServerBufferSize());
        this.currentState = CommandChannel.IDLE;
    }

    @Override
    public void run() {

        System.out.println("Пройден блок аторизации. Запущен второй блок обработки");

        try {
            String command;

            //пока соединён канал и состояние объекта любое, кроме EXIT
            while (currentState != CommandChannel.EXIT){

                //ждём смены состояния.
                if (currentState == CommandChannel.IDLE){
                    this.currentState = CommandChannel.valueOf(myReadChannel());
                    System.out.println("Получено состояние: " + currentState);
                }

                //смена состояния на приём файла
                if (this.currentState == CommandChannel.INFILES){
                    myWriteChannel(CommandChannel.READCHANNEL.toString());
                    System.out.println(currentState + " state");

                    String[] fileProperty = myReadChannel().split(" ");

                    String nameInFile  = fileProperty[0];
                    long sizeInFile = Long.parseLong(fileProperty[1]);
                    long countPostTransaction = Integer.parseInt(fileProperty[2]);

                    System.out.println(nameInFile + ", " + sizeInFile + ", " + countPostTransaction);
                    //TODO тут пока норм
                    myWriteChannel(CommandChannel.START.toString());
                    transactionInFile(nameInFile, sizeInFile, countPostTransaction);
                }





            }

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private void transactionInFile(String nameFile, long sizeInFile, long countPost) throws IOException {
        //TODO до метода все работает
        System.out.println("begin copy transaction");

       if (readInFile(nameFile, countPost, sizeInFile)){
            this.currentState = CommandChannel.IDLE;
        }

        System.out.println("copy end");
    }

    private boolean readInFile(String nameFile, long count, long sizeFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(nameFile, "rws");

        this.byteBuffer.clear();
        int i;

        for (i = 0; i < count; i++) {
            this.socketChannel.read(this.byteBuffer);
            this.byteBuffer.flip();

            raf.getChannel().write(this.byteBuffer);

            this.byteBuffer.clear();
        }

        System.out.println("i = " + i);

        raf.close();
        return true;
    }

    private String myReadChannel() throws IOException {
        byte[] tmpArray;
        this.socketChannel.read(this.byteBuffer);
        int tmpSize = this.byteBuffer.position();
        tmpArray = new byte[tmpSize];
        this.byteBuffer.flip();
        this.byteBuffer.get(tmpArray);
        this.byteBuffer.clear();
        return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
        this.socketChannel.write(tmpPack);
    }
}
