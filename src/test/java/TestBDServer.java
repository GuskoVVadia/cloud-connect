import ServerNIO.addition.BDServer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TestBDServer {

    @Test
    public void testFileBDExists(){
        Path path = Paths.get("src/main/resources/CloudBase.db");
        Assert.assertTrue(Files.exists(path));
    }

    @Test
    public void testMapNotNull(){
        Map<String, String> map = new BDServer().getMap();
        Assert.assertNotNull(map);
    }
}
