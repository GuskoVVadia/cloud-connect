/**
 * Блок идентификации
 * Задачи:
 *  1. идентификация клиента
 *  2. формирование объекта о клиенте
 *  3. запуск блока чтения/записи с передачей объекта.
 *  4*. Отслеживание IDLE, запуск таймеров, счётчиков, доп. обработчиков (опционально)
 *  5. Завершение работы при пустом 4 пункте.
 *
 *  После указания нового пути от клиента - Сервер заносит этот путь к БД, но не обновляет MAP данных пользователей.
 */
package addition;

import javafx.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;

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
        this.selectionKey = this.socketChannel.register(this.selector, SelectionKey.OP_READ,
                bufferIdentifi);
        this.currentState = CommandChannel.START;
    }

    @Override
    public void run() {
        try {
            while (!currentState.equals(CommandChannel.IDENTIFI)) {
                selector.select();

                if (selectionKey.isReadable()){
                    String[] inComingPackage = myReadChannel().split(" ");
                    String name = inComingPackage[0];
                    int pass = Integer.parseInt(inComingPackage[1]);
                    String path = inComingPackage[2];

                    Pair<Integer, String> pair = clientData.get(name);
                    if (pair == null){      //Если по имени нет данных в БД
                        myWriteChannel(CommandChannel.ERROR.toString());
                    } else {                //если Пара нашлась

                        if (pass == pair.getKey()){ //совпал пароль к имени
                            if (path.equals("null") & pair.getValue() == null){ //если введённый путь и путь из БД: все null
                                myWriteChannel(CommandChannel.PATHNONE.toString());
                            }

                            if (path.equals("null") & pair.getValue() != null){ // введён null, а в БД - нет
                                myWriteChannel(CommandChannel.IDENTIFI.toString());
                                this.person = new Person(name, pair.getValue());
                                this.currentState = CommandChannel.IDENTIFI;
                            }

                            if (!path.equals("null")){ //если введён путь
                                new BDServer(name, path);   //изменили данные в бд
                                myWriteChannel(CommandChannel.IDENTIFI.toString());
                                this.person = new Person(name, path);
                                this.currentState = CommandChannel.IDENTIFI;
                            }
                        } else {    //пароль отличается от введённого
                            myWriteChannel(CommandChannel.ERROR.toString());
                        }
                    }
                }
            }

            this.selector.close();
            new MainUnit(this.socketChannel, this.properties, this.person).run();
        }catch(IOException e){
            e.printStackTrace();
            }
        }

    private String myReadChannel() throws IOException {
            this.socketChannel.read(bufferIdentifi);
            int tmpSize = bufferIdentifi.position();
            byte[] tmpArray = new byte[tmpSize];
            this.bufferIdentifi.flip();
            this.bufferIdentifi.get(tmpArray);
            this.bufferIdentifi.clear();
            return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
        this.socketChannel.write(tmpPack);
    }
}
