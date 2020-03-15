import CloudShare.CommandChannel;
import CloudShare.ServerProperties;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestServerProperties {

    private ServerProperties sp = new ServerProperties();

    @Test
    public void testPort(){
        Assert.assertEquals(8189, sp.getServerPort());
    }

    @Test
    public void testBufferSizeNotNull(){
        Assert.assertTrue(sp.getServerBufferSize() != 0);
    }

    @Test
    public void testFilesExists(){
        Assert.assertTrue(Files.exists(Paths.get("Server.properties")));
    }

    @Test
    public void testBufferSizeEquals(){
        Assert.assertEquals(2097152, sp.getServerBufferSize());
    }

    @Test
    public void testBufferCommandNotNull(){
        Assert.assertTrue(sp.getCommandSizeBuffer() != 0);
    }

    @Test
    public void testSizeBufferIdentifi(){
        Assert.assertEquals(1024, sp.getIdentifiSizeBuffer());
    }
}
