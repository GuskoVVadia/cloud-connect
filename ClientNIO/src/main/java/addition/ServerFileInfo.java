package addition;

import javafx.util.Pair;

public class ServerFileInfo {

    private String name;
    private Long size;

    public ServerFileInfo(Pair<String, Long> pair){
        this.name = pair.getKey();
        this.size = pair.getValue();
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }
}
