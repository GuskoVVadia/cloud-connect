package Controllers;

import addition.*;
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
import javafx.util.Pair;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {


    //leftPane
    public TableView<ClientFileInfo> clientFilesTableLeftPane;
    public TextField clientStatusLine;
    public ComboBox<String> diskBoxLeftPane;
    public TextField clientPathField;

    //rightPane
    public TableView<ServerFileInfo> serverFilesTableRightPane;
    public TextField serverStatusLine;

    //other elements main.fxml
    public Button btnExit;
    public Button btnSynch;


    //переменные отвечающие за размер отображения в панелях
    private int sizeTypeColumn = 20;
    private int sizeNameColumn = 300;
    private int sizeFileSizeColumn = 75;


    //class variables
    private SocketChannel socketChannel;
    private ClientProperties properties;
    private ByteBuffer byteBuffer;
    private int bufferSize;

    private CommandChannel currentState;

    private String nameMy;
    private Path pathMy;

    private List<Pair<String, Long>> listFilesInServer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.socketChannel = NetworkData.getInstance().getSocketChannel();
        this.properties = new ClientProperties();
        this.bufferSize = this.properties.getBufferSize();
        this.byteBuffer = ByteBuffer.allocate(this.bufferSize);

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

        btnSynch.fire();
        serverTableView();

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
        sendExitToServer();
        Platform.exit();
    }

    private void sendExitToServer(){
        try {
            System.out.println("jnghfdkty EXIT");
            myWriteChannel(CommandChannel.EXIT.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    //возвращает имя выделенного файла
    public String getSelectedFileName(){
        if (clientFilesTableLeftPane == isActivePane()){
            return clientFilesTableLeftPane.getSelectionModel().getSelectedItem().getFileName();
        } else {
            return serverFilesTableRightPane.getSelectionModel().getSelectedItem().getName();
        }
    }

    //возвращает активную панель
    public TableView isActivePane(){
        return clientFilesTableLeftPane.isFocused() ? clientFilesTableLeftPane : serverFilesTableRightPane;
    }

    public String getCurrentPath(){
        return clientPathField.getText();
    }

    //TODO
    public void copyBtnAction(ActionEvent actionEvent) {

        String nameFileActive = getSelectedFileName();

        if (clientFilesTableLeftPane == isActivePane()){

            Path pathActive = Paths.get(getCurrentPath(), nameFileActive);
            System.out.println("client " + pathActive);

            if (!Files.isDirectory(pathActive)){
                fileCopyAtClientToServer(pathActive, nameFileActive);
            }

        } else {
            System.out.println("server " + nameFileActive);
        }

    }

    //отправка файла от клиента на сервер
    private boolean fileCopyAtClientToServer(Path pathActive, String nameFileActive) {
        boolean answer = true;
        try {
            this.serverStatusLine.setText("Copy " + nameFileActive + " on Server");

            //отправка флага на сервер
            myWriteChannel(CommandChannel.INFILES.toString());

            //!получаем от сервера подтверждение
            myReadChannel();


            long sizeFileOut = Files.size(pathActive);
            myWriteChannel(nameFileActive + ";" + sizeFileOut);
            System.out.println(this.bufferSize + " размер буфера, " + sizeFileOut + " размер файла");
            long count = sizeFileOut / this.bufferSize;
            count = (count == 0) ? 1 : count;
            int i = 0;

            myReadChannel();



            try (
                    RandomAccessFile rafOut = new RandomAccessFile(pathActive.toString(), "rw");
            ){

                while (i <= count){

                    this.byteBuffer.clear();
                    rafOut.getChannel().read(this.byteBuffer);
                    this.byteBuffer.flip();
                    Thread.sleep(50);
                    this.socketChannel.write(this.byteBuffer);
                    i++;
                    System.out.println(i);
                }

            }catch (Exception e){
                throw new RuntimeException("Error read file" + nameFileActive);
            }

            this.serverStatusLine.setText("End copy file on Server");


        }catch (IOException e){
            answer = false;
        }


        return answer;
    }

    //здесь мы получили файлы с сервера в виде list <Pair>.
    public void btnSynchFileServerAction(ActionEvent actionEvent) {
        try {
            System.out.println("отправляю запрос на сервер о файлах");
            myWriteChannel(CommandChannel.LISTFILES.toString());
            String stringFilesInServer = myReadChannel();
            if (!stringFilesInServer.equals("NULL")) {
                String[] tmpArray = stringFilesInServer.split(";");

                this.listFilesInServer = new LinkedList<>();

                for (int i = 0; i < tmpArray.length; i++) {
                    String[] line = tmpArray[i].split(" ");
                    this.listFilesInServer.add(new Pair<String, Long>(line[0], Long.parseLong(line[1])));
                }
            } else {
                this.listFilesInServer = new LinkedList<>();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        serverTableView();
    }

    //заполнение правой(серверной) стороны приложения
    private void serverTableView(){

        TableColumn<ServerFileInfo, String> serverFileNameColumn = new TableColumn<>("name files");
        serverFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        serverFileNameColumn.setPrefWidth(sizeNameColumn);

        TableColumn<ServerFileInfo, Long> serverFileSizeColumn = new TableColumn<>("size");
        serverFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        serverFileSizeColumn.setPrefWidth(sizeFileSizeColumn);

        serverFileSizeColumn.setCellFactory(column -> new TableCell<ServerFileInfo, Long>() {
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

        serverFilesTableRightPane.getColumns().clear();
        serverFilesTableRightPane.getColumns().addAll(serverFileNameColumn, serverFileSizeColumn);
        serverFilesTableRightPane.getSortOrder().add(serverFileSizeColumn);

        updateListServer();

    }

    //обновление отображения серверных файлов в правом окне приложения
    private void updateListServer(){
        serverFilesTableRightPane.getItems().clear();
        serverFilesTableRightPane.getItems().addAll(this.listFilesInServer.stream().map(ServerFileInfo::new).collect(Collectors.toList()));
        serverFilesTableRightPane.sort();
    }

}
