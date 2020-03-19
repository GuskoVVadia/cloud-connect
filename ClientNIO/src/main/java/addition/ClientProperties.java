package addition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClientProperties {

    private Path pathCurrentClientProp = Paths.get("Client.properties");
    private Path pathDefaultClientProp = Paths.get("ClientNIO\\src\\main\\java\\addition\\ClientDefault.properties");
    private int serverPort;
    private int serverBufferSize;
    private int identifiSizeBuffer;

    public ClientProperties() {
        if (!Files.exists(pathCurrentClientProp)){
            createDefaultProperties();
        }
        readClientProperty();
    }

    private void readClientProperty(){
        try {
            List<String> listProp =  Files.readAllLines(pathCurrentClientProp);
            for (String link: listProp){

                if (link.contains("PORT_SERVER")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.serverPort = Integer.parseInt(tmp);
                }
                if (link.contains("BUFFER_SIZE")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.serverBufferSize = Integer.parseInt(tmp);
                }
                if (link.contains("BUFFER_IDENTIFI")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.identifiSizeBuffer = Integer.parseInt(tmp);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultProperties(){
        try {
            Files.copy(pathDefaultClientProp, pathCurrentClientProp);
        } catch (IOException e) {
            System.out.println("Error read/write file option");
        }
    }

    public int getPort() {
        return serverPort;
    }

    public int getBufferSize() {
        return serverBufferSize;
    }

    public int getIdentifiSizeBuffer() {
        return identifiSizeBuffer;
    }

    public static void main(String[] args) {
        ClientProperties clientProperties = new ClientProperties();
        System.out.println( Files.exists(clientProperties.pathCurrentClientProp));
        System.out.println( Files.exists(clientProperties.pathDefaultClientProp));
    }
}

