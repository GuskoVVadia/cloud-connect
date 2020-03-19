package addition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServerProperties {

    private Path pathCurrentServProp = Paths.get("Server.properties");
    private Path pathDefaultServProp = Paths.get("ServerNIO\\src\\main\\java\\addition\\ServerDefault.properties");
    private int serverPort;
    private int serverBufferSize;
    private int commandSizeBuffer;
    private int identifiSizeBuffer;
    private String root;

    public ServerProperties() {
        if (!Files.exists(pathCurrentServProp)){
            createDefaultProperties();
        }
        readServProp();
    }

    private void readServProp(){
        try {
            List<String> listProp =  Files.readAllLines(pathCurrentServProp);
            for (String link: listProp){

                if (link.contains("PORT_SERVER")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.serverPort = Integer.parseInt(tmp);
                }
                if (link.contains("BUFFER_SIZE")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.serverBufferSize = Integer.parseInt(tmp);
                }
                if (link.contains("READ_SIZE")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.commandSizeBuffer = Integer.parseInt(tmp);
                }
                if (link.contains("BUFFER_IDENTIFI")){
                    String tmp = link.split("=")[1].split(";")[0].trim();
                    this.identifiSizeBuffer = Integer.parseInt(tmp);
                }
                if (link.contains("ROOT")){
                    String tmp = link.split("=")[1].split(";")[0].trim().split(" ")[0];
                    Path tmpPath = Paths.get(tmp);
                    if (!Files.exists(tmpPath)){
                        Files.createDirectory(tmpPath);
                    }
                    this.root = tmpPath.toString();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultProperties(){
        try {
            Files.copy(pathDefaultServProp, pathCurrentServProp);
        } catch (IOException e) {
            System.out.println("Error read/write file option");
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getServerBufferSize() {
        return serverBufferSize;
    }

    public int getCommandSizeBuffer() {
        return commandSizeBuffer;
    }

    public int getIdentifiSizeBuffer() {
        return identifiSizeBuffer;
    }

    public String getRoot() {
        return root;
    }

}

