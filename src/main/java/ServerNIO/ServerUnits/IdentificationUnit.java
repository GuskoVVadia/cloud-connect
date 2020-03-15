/**
 * Блок идентификации
 * Задачи:
 *  1. идентификация клиента
 *  2. формирование объекта о клиенте
 *  3. запуск блока чтения/записи с передачей объекта.
 *  4*. Отслеживание IDLE, запуск таймеров, счётчиков, доп. обработчиков (опционально)
 *  5. Завершение работы при пустом 4 пункте.
 */
package ServerNIO.ServerUnits;

import CloudShare.CommandChannel;
import CloudShare.ServerProperties;
import ServerNIO.addition.Person;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class IdentificationUnit implements Runnable {

    private SocketChannel socketChannel;
    private ServerProperties properties;
    private ByteBuffer bufferIdentifi;
    private Map<String, String> clientData;
    private String rootDirectoryServer;
    private Person personClient;
    private int bufferSize;

    public IdentificationUnit(SocketChannel socketChannel, ServerProperties serverProperties, Map<String, String> map){
        this.socketChannel = socketChannel;
        this.properties = serverProperties;

        this.rootDirectoryServer = properties.getRoot();
        this.bufferSize = properties.getIdentifiSizeBuffer();
        this.bufferIdentifi = ByteBuffer.allocate(this.bufferSize);
        this.clientData = map;
    }

    @Override
    public void run() {

        if (openGate()) {
            new SecondUnit(socketChannel, properties, personClient).run();
        }
    }

    private boolean openGate(){
        try {
            if (myReadChannel().equals(CommandChannel.START.toString())){
                myWriteChannel(CommandChannel.START.toString());
            } else {
                System.out.println("Error client channel");
//                return false;
            }
            concurrencyClient();
        }catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }

    private String myReadChannel() throws IOException {
        byte[] tmpArray;
        this.socketChannel.read(bufferIdentifi);
        int tmpSize = bufferIdentifi.position();
        tmpArray = new byte[tmpSize];
        this.bufferIdentifi.flip();
        this.bufferIdentifi.get(tmpArray);
        this.bufferIdentifi.clear();
        return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
        this.socketChannel.write(tmpPack);
    }

    private void concurrencyClient() throws IOException {

        boolean key = false;
        while (!key){
            try {
                String[] dataClient = myReadChannel().split(" ");

                String name = dataClient[0];
                String pass = dataClient[1];
                System.out.println("name " + name + ", pass + " + pass);

                if (pass.equals(clientData.get(name))){
                    this.personClient = new Person(name, this.rootDirectoryServer);
                    myWriteChannel(CommandChannel.IDENTIFI.toString());
                    key = true;
                } else {
                    myWriteChannel("invalid login/password");
                }

            }catch (IOException e){
                this.socketChannel.finishConnect();
                e.printStackTrace();
            }
        }
    }


}
