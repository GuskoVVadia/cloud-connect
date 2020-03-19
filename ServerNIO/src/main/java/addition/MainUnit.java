package addition;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class MainUnit implements Runnable{

    private SocketChannel socketChannel;
    private ServerProperties properties;
    private Person person;
    private Selector selector;
    private SelectionKey selectionKey;
    private ByteBuffer byteBuffer;


    public MainUnit(SocketChannel socketChannel, ServerProperties properties, Person person) {
        try {

            this.socketChannel = socketChannel;
            this.properties = properties;
            this.person = person;
            this.byteBuffer = ByteBuffer.allocate(this.properties.getServerBufferSize());

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String myReadChannel() throws IOException {
        this.socketChannel.read(byteBuffer);
        int tmpSize = byteBuffer.position();
        byte[] tmpArray = new byte[tmpSize];
        this.byteBuffer.flip();
        this.byteBuffer.get(tmpArray);
        this.byteBuffer.clear();
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
