package addition;

import java.nio.file.Files;
import java.nio.file.Path;

public class ClientFileInfo {

    public enum FileType{

        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private String fileName;
    private FileType fileType;
    private long size;

    public ClientFileInfo(Path path){
        try {
            this.fileName = path.getFileName().toString();
            this.size = Files.size(path);
            this.fileType = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (fileType == FileType.DIRECTORY){
                this.size = -1L;
            }
        }catch (Exception e){
            throw new RuntimeException("Error read files properties");
        }
    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getSize() {
        return size;
    }
}
