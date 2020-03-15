package ServerNIO.addition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Person {

    private String name;
    private Path pathPerson;

    public Person(String name, String root) throws IOException {
        this.name = name;
        this.pathPerson = Paths.get(root, name);
        if (!Files.exists(this.pathPerson)){
            Files.createDirectory(pathPerson);
        }

    }

    public String getName() {
        return name;
    }

    public Path getRootPersonServer() {
        return pathPerson;
    }


}
