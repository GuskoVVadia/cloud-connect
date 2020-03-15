package ServerNIO;

import CloudShare.ServerProperties;
import ServerNIO.addition.Person;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tests {
    public static void main(String[] args) throws IOException {

        String nameFile = "D:\\ppt.mp4";
        Path path = Paths.get(nameFile);
        boolean answer = Files.exists(path);

        System.out.println(answer);



//        ServerProperties properties = new ServerProperties();
//        Path path = Paths.get(properties.getRoot());
//        String name = "Vadia";
//        Person client = new Person(name, properties.getRoot());
//        System.out.println(client.getRootPersonServer());
//        boolean answer = Files.exists(client.getRootPersonServer());
//        if (!answer){
//            Files.createDirectory(client.getRootPersonServer());
//        }
//        System.out.println(answer);

//        String answer = CommandChannel.WRITECHANNEL.toString();
//        System.out.println(answer);


        //        new Server().run();


//        Path pathCurrentServProp = Paths.get("Server.properties");
//        ServerProperties sp = new ServerProperties();
//        System.out.println(sp.getServerBufferSize());
//        System.out.println(sp.getServerPort());


//        Path path = Paths.get("src/main/java/ServerNIO/addition");
//        System.out.println(Files.exists(path));

//        String str = "849;";
//        Integer s = Integer.parseInt(str);

    }
}
