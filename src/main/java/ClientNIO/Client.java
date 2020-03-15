package ClientNIO;

import CloudShare.CommandChannel;
import CloudShare.ServerProperties;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client implements Runnable{

    private ServerProperties properties;
    private int port;
    private ByteBuffer clientBuff;
    private int clientBufferSize;

    private CommandChannel currentState;
    private SocketChannel socketChannel;

    public Client() {
        try {
            this.properties = new ServerProperties();
            this.port = properties.getServerPort();

            this.clientBufferSize = this.properties.getServerBufferSize();
            this.clientBuff = ByteBuffer.allocate(this.clientBufferSize);

            this.socketChannel = SocketChannel.open(new InetSocketAddress(port));
            System.out.println("Clien start, port " + this.port);

            this.currentState = CommandChannel.START;


        } catch (IOException e){
            System.out.println("Server not started");
        }

    }

    @Override
    public void run() {
       try {
           authorizationUnit();    //блок авторизации на сервере

           System.out.println("пройден блок авторизации");
           System.out.println("Отправка серверу " + CommandChannel.INFILES.toString());
           myWriteChannel(CommandChannel.INFILES.toString());

           String nameFile = "D:\\ppt.mp4";
           transactionFileInServer(nameFile);

           try {
               Thread.sleep(5000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }


//        while ((currentState != CommandChannel.EXIT) && this.socketChannel.isConnected()) {
//        }


       } catch (IOException e){
           clientExit();
           e.printStackTrace();
       }

    }

    /**
     * Метод авторизации:
     *  если клиент-сервер обоюдно обменялись состоянием готовности,
     *      запуск обмена логин-пароль от клиента к серверу.
     *  если нет - смена состояния, вывод информации о сбое и возврат false.
     * @return true - в случае успеха, false и DISCONNECT в случае провала
     */
    private boolean authorizationUnit(){
        try {

            //если сервер говот принимать данные авторизации
            if (firstUnitAuthorization()){
                secondUnitAuthorization();
            } else {
                System.out.println("Обрыв связи");
                this.currentState = CommandChannel.DISCONNECT;
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Между клиентом и сервером происходит обмен флагами START, что говорит о готовности обоюдном соединении
     * и готовности обоих переходить к другому юниту.
     * @return true - если сервер готов, false - не готов к приёму.
     * @throws IOException
     */
    private boolean firstUnitAuthorization() throws IOException {
        System.out.println("unit1");
        // отправка на сервер флага о готовности к старту операции
        myWriteChannel(currentState.toString());

        //получаем ответ от сервера о готовности
        if (!myReadChannel().equals(currentState.toString())){
            System.out.println("Error server post");
            return false;
        }
        return true;
    }

    private String myReadChannel() throws IOException {
        byte[] tmpArray;
        this.socketChannel.read(clientBuff);
        int tmpSize = clientBuff.position();
        tmpArray = new byte[tmpSize];
        this.clientBuff.flip();
        this.clientBuff.get(tmpArray);
        this.clientBuff.clear();
        return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
        this.socketChannel.write(tmpPack);
    }

    //пока состояние не будет IDENTIFI - будем запрашивать пароль
    //на входе в метод состояние START
    private void secondUnitAuthorization() throws IOException {
        while (this.currentState != CommandChannel.IDENTIFI){
            //TODO здесь размещается метод запроса логина-пароля
            String name = "user1";
            String password = "pass1";
            String post = name + " " + password;

            myWriteChannel(post);
            String answer = myReadChannel();

            System.out.println(answer);
            if (answer.equals(CommandChannel.IDENTIFI.toString())){
                this.currentState = CommandChannel.IDENTIFI;
            }
        }
    }

    private void clientExit(){
        try {
            myWriteChannel(CommandChannel.EXIT.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transactionFileInServer(String nameFile) throws IOException {

        Path pathFile = Paths.get(nameFile);

        String answer = myReadChannel();
        if (answer.equals(CommandChannel.READCHANNEL.toString())) {
            String nameOutFile = pathFile.getFileName().toString();
            long sizeOutFile = Files.size(pathFile);
            long countTransaction = sizeOutFile / this.clientBufferSize;
            if (countTransaction == 0){
                countTransaction = 1;
            }
            //отправил данные
            myWriteChannel(nameOutFile + " " + sizeOutFile + " " + countTransaction);

            answer = myReadChannel();
            if (answer.equals(CommandChannel.START.toString())) {
                //TODO тут норм
                writeInFile(pathFile, countTransaction, sizeOutFile);
            }
        }
    }

    private void writeInFile(Path path, long count, long sizeFile) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(path.toFile().getName(), "rw");
            this.clientBuff.clear();

            int i;
            for (i = 0; i < count; i++) {
                raf.getChannel().read(this.clientBuff);
                this.clientBuff.flip();
                this.socketChannel.write(this.clientBuff);

                this.clientBuff.clear();
            }


            System.out.println("Отдачу файла завершил. i = " + i);

            raf.close();
        }




    public static void main(String[] args) {
        new Client().run();
    }
}
