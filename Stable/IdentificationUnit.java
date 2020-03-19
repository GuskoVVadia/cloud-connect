/**
 * Блок идентификации
 * Задачи:
 *  1. идентификация клиента
 *  2. формирование объекта о клиенте
 *  3. запуск блока чтения/записи с передачей объекта.
 *  4*. Отслеживание IDLE, запуск таймеров, счётчиков, доп. обработчиков (опционально)
 *  5. Завершение работы при пустом 4 пункте.
 */
package addition;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IdentificationUnit implements Runnable {

    private SocketChannel socketChannel;    //сокет
    private ServerProperties properties;    //настройки сервера
    private Map<String, Pair<Integer, String>> clientData;  //данные клиентов


    private Selector selector;
    private SelectionKey selectionKey;

    private ByteBuffer bufferIdentifi;
    private int bufferSize;

    private CommandChannel currentState;
    private Person person;

    public IdentificationUnit(SocketChannel socketChannel, ServerProperties serverProperties,
                              Map<String, Pair<Integer, String>> map) throws IOException {

        this.socketChannel = socketChannel;
        this.properties = serverProperties;
        this.clientData = map;
        this.bufferSize = this.properties.getIdentifiSizeBuffer();
        this.bufferIdentifi = ByteBuffer.allocate(this.bufferSize);

        this.selector = Selector.open();
        this.socketChannel.configureBlocking(false);
        this.selectionKey = this.socketChannel.register(this.selector, SelectionKey.OP_READ);
        this.currentState = CommandChannel.START;
    }

    @Override
    public void run() {
        try {


            while (currentState == CommandChannel.START) {

                selector.select();
                if (selectionKey.isReadable()) {

                String link = myReadChannel();
                    String[] inComingPackage = link.split(" ");
                System.out.println(link);

                    String nameClient = inComingPackage[0];
                    int passClient = Integer.parseInt(inComingPackage[1]);
                    String pathClient = inComingPackage[2];

                    Pair<Integer, String> clTemp = clientData.get(nameClient);
                    if (clTemp != null && passClient == clTemp.getKey()){   //и имя подошло и пароль совпал

                            if (pathClient.equals("null")){ //  полученный путь = 0;

                                if (clTemp.getValue().equals("null")){  //и имеющийся путь равен 0;
                                    myWriteChannel(CommandChannel.PATHNONE.toString());
                                } else {
                                    //TODO тут мы получили и данные верные и путь из БД предыдущий
                                    this.person = new Person(nameClient, pathClient);
                                    myWriteChannel(CommandChannel.IDENTIFI.toString());
                                    this.currentState = CommandChannel.IDENTIFI;
                                }

                            } else {
                                //TODO тут данные верные, а путь в БД меняем
                                new BDServer(nameClient, pathClient);

                                this.person = new Person(nameClient, pathClient);
                                myWriteChannel(CommandChannel.IDENTIFI.toString());
                                this.currentState = CommandChannel.IDENTIFI;
                            }
                    }
                }
            }

            new MainUnit(this.socketChannel, this.properties, this.person).run();

        }catch (IOException e){
//            e.printStackTrace();
            System.err.println("client away");
        }
    }

    private String myReadChannel() throws IOException {
        byte[] tmpArray;
        this.socketChannel.read(bufferIdentifi);

        int tmpSize = bufferIdentifi.position();
        tmpArray = new byte[tmpSize];
        this.bufferIdentifi.flip();
        this.bufferIdentifi.get(tmpArray);
        this.bufferIdentifi.clear();
        System.out.println(Arrays.toString(tmpArray));
        return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
        this.socketChannel.write(tmpPack);
    }

}
