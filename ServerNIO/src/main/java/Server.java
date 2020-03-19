/**
 * Класс запуска сервера.
 * Задачи:
 *  1. Получение объекта настроек
 *  2. Получение данных из БД.
 *   Запуск сервера (канала сокета) в бесконечном цикле
 *   При получении соединения с клиентом - запустить в отдельном потоке дальнейшую обработку,
 *      передав ему канал и объект настроек.
 */

import addition.BDServer;
import addition.IdentificationUnit;
import addition.ServerProperties;
import javafx.util.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class Server implements Runnable{
    private ServerSocketChannel serverSocketChannel;
    private int serverPort;

    private ServerProperties properties;
    private Map<String, Pair<Integer, String>> clientData;

    public Server(){
        try {
            //узнать параметры сервера
            this.properties = new ServerProperties();
            this.serverPort = this.properties.getServerPort();   //порт
            this.clientData = new BDServer().getMap();

            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.socket().bind(new InetSocketAddress(this.serverPort));

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Server start. Port = " + this.serverPort);
        while (this.serverSocketChannel.isOpen()){
            try {
                SocketChannel clientNew = this.serverSocketChannel.accept();
                new IdentificationUnit(clientNew, this.properties, this.clientData).run();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void rebootMap(){
        this.clientData = new BDServer().getMap();
    }
}


