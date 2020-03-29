package addition;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import javafx.util.Pair;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MainUnit implements Runnable{

    private SocketChannel socketChannel;
    private ServerProperties properties;
    private Path root;

    private Person person;
    private Selector selector;
    private SelectionKey selectionKey;
    private ByteBuffer byteBuffer;
    private CommandChannel currentState;
    private int bufferSize;


    public MainUnit(SocketChannel socketChannel, ServerProperties properties, Person person) {
        try {

            this.socketChannel = socketChannel;
            this.properties = properties;
            this.person = person;
            this.bufferSize = this.properties.getServerBufferSize();

            this.byteBuffer = ByteBuffer.allocate(this.bufferSize);
            this.root = Paths.get(this.properties.getRoot()).resolve(person.getName());
            if (!Files.exists(this.root)){
                Files.createDirectory(this.root);
            }

            this.selector = Selector.open();
            this.socketChannel.configureBlocking(false);
            this.selectionKey = this.socketChannel.register(this.selector, SelectionKey.OP_READ, this.byteBuffer);


        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {

            myWriteChannel(person.getName() + " " + person.getPath());
            System.out.println("Данные персоны отпралены");
            this.currentState = CommandChannel.IDLE;

            while (this.currentState != CommandChannel.EXIT) {

                selector.select();

                //спокойное состояние сервера - открыт для приёма сигнального флага
                if (currentState == CommandChannel.IDLE){

                    if (selectionKey.isReadable()) {
                        String commandLine = myReadChannel();
                        System.out.println(commandLine + " line 71");
                        this.currentState = CommandChannel.valueOf(commandLine);
                    }
                }

                //сервер получил запрос на отправку файлов
                if (currentState == CommandChannel.LISTFILES){

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sendCurrentStateFiles();
                    this.currentState = CommandChannel.IDLE;
                }

                //сервер получил запрос на принятие файла
                if (currentState == CommandChannel.INFILES){

                    //отправляем подтверждаение на смену флага
                    myWriteChannel(currentState.toString());
                    System.out.println("отправил флаг");

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //получены имя файла и размер
                    String[] dataFiles = myReadChannel().split(" ");
                    Path pathInFile = Paths.get(this.root.toString(), dataFiles[0]);
                    myWriteChannel("start");
//                    myWriteChannel("ok");

                    transferInDirectoryClient(pathInFile, Long.parseLong(dataFiles[1]));

                    this.currentState = CommandChannel.IDLE;
                }



            }

            System.out.println("Client EXIT");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ver 1.0 stable
//    void transferIn(final FileChannel channel, long position, long size) throws IOException {
//
//        while (position < size) {
//            position += channel.transferFrom(this.socketChannel, position, this.bufferSize);
//        }
//        System.out.println("method transferIn отработал...");
//    }

    void transferInDirectoryClient(Path pathInFile, long sizeInFile){
        try (
                RandomAccessFile rafIn = new RandomAccessFile(pathInFile.toString(), "rw");
                ){

            long position = 0;
            while (position < sizeInFile){
                position += rafIn.getChannel().transferFrom(this.socketChannel, position, this.bufferSize);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    private void sendCurrentStateFiles() throws IOException{

        //получаем лист путей
        List<Path> pathsListClientFiles = Files.list(this.root).collect(Collectors.toList());
        List<Pair<String, Long>> listPair = new LinkedList<>();

        for (Path p: pathsListClientFiles){
            String nameFile = p.getFileName().toString();
            Long sizeFile = Files.isDirectory(p) ? -1 : Files.size(p);
            listPair.add(new Pair<>(nameFile, sizeFile));
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<String, Long> p: listPair){
            stringBuilder.append(p.getKey() + " " + p.getValue() + ";");
        }

        String letter = stringBuilder.toString();

        if (letter.length() > 0) {
            myWriteChannel(stringBuilder.toString());
        } else {
            myWriteChannel("NULL");
        }
    }

    private byte[] myReadChannelByte() throws IOException {
        this.socketChannel.read(byteBuffer);
        int tmpSize = byteBuffer.position();
        byte[] tmpArray = new byte[tmpSize];
        this.byteBuffer.flip();
        this.byteBuffer.get(tmpArray);
        this.byteBuffer.clear();
        return tmpArray;
    }

    private String myReadChannel() throws IOException {
        this.socketChannel.read(byteBuffer);
        int tmpSize = byteBuffer.position();
        byte[] tmpArray = new byte[tmpSize];
        this.byteBuffer.flip();
        this.byteBuffer.get(tmpArray);
        this.byteBuffer.clear();
        System.out.println("прочитано" + Arrays.toString(tmpArray));
        return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        this.byteBuffer.clear();
        this.byteBuffer.put(line.getBytes());
        this.byteBuffer.flip();
        this.socketChannel.write(this.byteBuffer);
        this.byteBuffer.clear();
    }

//        private void myWriteChannel(String line) throws IOException {
//            ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
//            this.socketChannel.write(tmpPack);
//        }
}
