package Controllers;

import addition.ClientFileInfo;
import addition.ClientProperties;
import addition.CommandChannel;
import addition.NetworkData;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {


    //leftPane
    public TableView<ClientFileInfo> clientFilesTableLeftPane;
    public TextField clientStatusLine;
    public ComboBox<String> diskBoxLeftPane;
    public TextField clientPathField;

    //rightPane
    public TableView serverFilesTableRightPane;
    public TextField serverStatusLine;

    //other elements main.fxml
    public Button btnExit;



    //переменные отвечающие за размер отображения в панелях
    private int sizeTypeColumn = 20;
    private int sizeNameColumn = 300;
    private int sizeFileSizeColumn = 75;


    //class variables
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

        comboBoxUpdate();
        clientTableView();
    }

    private void clientTableView(){

        TableColumn<ClientFileInfo, String> clientTypeColumn = new TableColumn<>();
        clientTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        clientTypeColumn.setPrefWidth(sizeTypeColumn);

        TableColumn<ClientFileInfo, String> clientFileNameColumn = new TableColumn<>("name files");
        clientFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        clientFileNameColumn.setPrefWidth(sizeNameColumn);

        TableColumn<ClientFileInfo, Long> clientFileSizeColumn = new TableColumn<>("size");
        clientFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        clientFileSizeColumn.setPrefWidth(sizeFileSizeColumn);
        clientFileSizeColumn.setCellFactory(column -> new TableCell<ClientFileInfo, Long>() {
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
                    } else {
                        //my insert 20.03
                        if (item > 1_048_576) {
                            text = String.format("%, d MB", (item / 1_048_576));
                        } else {
                            if (item > 1024) {
                                text = String.format("%, d KB", (item / 1024));
                            }
                        }
                    }

                    setText(text);
                }
            }
        });

        clientFilesTableLeftPane.getColumns().addAll(clientTypeColumn, clientFileNameColumn, clientFileSizeColumn);
        clientFilesTableLeftPane.getSortOrder().add(clientTypeColumn);

        updateListClient();
    }

    //обновление клиентских файлов
    private void updateListClient(){
        try {
            clientPathField.setText(pathMy.normalize().toAbsolutePath().toString());
            clientFilesTableLeftPane.getItems().clear();
            clientFilesTableLeftPane.getItems().addAll(Files.list(this.pathMy).map(ClientFileInfo::new).collect(Collectors.toList()));
            clientFilesTableLeftPane.sort();
        }catch (IOException e){
            System.err.println("List about file not create");
        }
    }

    //подготовительная работа для comboBox - список дисков для клиента
    private void comboBoxUpdate(){
        diskBoxLeftPane.getItems().clear();
        for (Path p: FileSystems.getDefault().getRootDirectories()){
            diskBoxLeftPane.getItems().add(p.toString());
        }
        diskBoxLeftPane.getSelectionModel().select(0);
    }

    public void btnExit(ActionEvent actionEvent) {
        Platform.exit();
//        System.out.println("btnExit");
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

    public void btnPathUpLeftPaneAction(ActionEvent actionEvent) {
        Path upperPath = this.pathMy.getParent();
        if (upperPath != null){
            this.pathMy = upperPath;
            this.updateListClient();
        }
    }

    public void selectDiskLeftPaneAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        this.pathMy = Paths.get(element.getSelectionModel().getSelectedItem());
        this.updateListClient();
    }

    public void clientFilesTableLeftPaneMouseAction(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2){
            Path tmpPath = pathMy.resolve(clientFilesTableLeftPane.getSelectionModel().getSelectedItem().getFileName());
            if (Files.isDirectory(tmpPath)){
                pathMy = tmpPath;
                updateListClient();
            }
        }
    }

    //работает только с левой стороной , т.е. с клиентской стороной
    public String getSelectedFileName(){
        if (!clientFilesTableLeftPane.isFocused()){
            return null;
        }
        return clientFilesTableLeftPane.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath(){
        return clientPathField.getText();
    }

    public void copyBtnAction(ActionEvent actionEvent) {
        
    }
}
