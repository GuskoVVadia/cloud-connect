package addition;

import java.nio.channels.SocketChannel;

public class NetworkData {

    private static NetworkData ourInstance = new NetworkData();
    private SocketChannel socketChannel;

    public static NetworkData getInstance(){
        return ourInstance;
    }

    private NetworkData(){
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

}
