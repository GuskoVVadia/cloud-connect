package Controllers;

import addition.ClientFileInfo;
import addition.ClientProperties;
import addition.CommandChannel;
import addition.NetworkData;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {


    public TableView<ClientFileInfo> leftFilesTable;
    public TableView rightFilesTable;

    public TextField clientStatusLine;
    public TextField serverStatusLine;

    public Button btnExit;
    public VBox mainBox;

    private SocketChannel socketChannel;
    private ClientProperties properties;
    private ByteBuffer byteBuffer;

    private CommandChannel currentState;

    private String nameMy;
    private Path pathMy;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.socketChannel = NetworkData.getInstance().getSocketChannel();
        this.properties = new ClientProperties();
        this.byteBuffer = ByteBuffer.allocate(this.properties.getBufferSize());

        try {
            String[] tmpData = myReadChannel().split(" ");
            this.nameMy = tmpData[0];
            this.pathMy = Paths.get(tmpData[1]);
            System.out.println(Arrays.toString(tmpData));


        } catch (IOException e) {
            e.printStackTrace();
        }

        clientTableView();

    }

    public void btnExit(ActionEvent actionEvent) {
        System.out.println("btnExit");
    }

    private String myReadChannel() throws IOException {
        byte[] tmpArray;
        this.socketChannel.read(byteBuffer);
        int tmpSize = byteBuffer.position();
        tmpArray = new byte[tmpSize];
        this.byteBuffer.flip();
        this.byteBuffer.get(tmpArray);
        this.byteBuffer.clear();
        return new String(tmpArray);
    }

    private void myWriteChannel(String line) throws IOException {
        this.byteBuffer.clear();
        this.byteBuffer.put(line.getBytes());
        this.byteBuffer.flip();
        this.socketChannel.write(this.byteBuffer);
        this.byteBuffer.clear();
    }

    //наполенение левой части приложения - данных о клиентских файлах
    private void clientTableView(){
        TableColumn<ClientFileInfo, String> clientTypeColumn = new TableColumn<>();
        clientTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        clientTypeColumn.setPrefWidth(20);

        TableColumn<ClientFileInfo, String> clientFileNameColumn = new TableColumn<>("name files");
        clientFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        clientFileNameColumn.setPrefWidth(400);

        TableColumn<ClientFileInfo, Long> clientFileSizeColumn = new TableColumn<>("size");
        clientFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        clientFileSizeColumn.setPrefWidth(150);
        clientFileSizeColumn.setCellFactory(column -> {
            return new TableCell<ClientFileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty){
                        setText("");
                        setStyle("");
                    } else {
                        String text = String.format("%, d bytes", item);
                        if (item == -1L){
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        leftFilesTable.getColumns().addAll(clientTypeColumn, clientFileNameColumn, clientFileSizeColumn);
        leftFilesTable.getSortOrder().add(clientTypeColumn);

        updateListClient();
    }

    //обновление клиентских файлов
    private void updateListClient(){
        try {
            leftFilesTable.getItems().clear();
            leftFilesTable.getItems().addAll(Files.list(this.pathMy).map(ClientFileInfo::new).collect(Collectors.toList()));
            leftFilesTable.sort();
        }catch (IOException e){
            System.err.println("List about file not create");
        }
    }

}
