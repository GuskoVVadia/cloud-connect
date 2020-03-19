import addition.ClientProperties;
import addition.CommandChannel;
import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Path;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class IdentificationController implements Initializable {


    public Button btnAuto;
    public TextField textF;
    public PasswordField passF;
    public Button direc;
    public Label labelStatus;

    private File file;
    private SocketChannel socketChannel;
    private ClientProperties properties;
    private ByteBuffer byteBuffer;
    private Selector selector;
    private SelectionKey selectionKey;
    private CommandChannel currentState;

    public void getIdentity(ActionEvent actionEvent) {
        int pass = passF.getText().hashCode();

        String name = textF.getText().trim().equals("") ? "null" : textF.getText();

//        if (file == null)
        if (file != null) {
            if (file.isDirectory()) {
                file = file.getParentFile();
            }
        }



        if (isOpenGate(pass, name, file)){
            new Thread(this::changeSceneToMain).start();
        }

        passF.clear();
        textF.clear();
    }

    private boolean isOpenGate(int pass, String name, File file) {
        try {

            System.out.println("pass = " + pass + ", name = " + name + ", file = " + file);
            if (pass == 0 | name.equals("null")) {
                labelStatus.setText("invalid password / login");
                return false;
            }

            String pathClient;

            if (file == null){
                pathClient = "null";
            } else {
                pathClient = file.toString();
            }

            myWriteChannel(name +" " + pass + " " + pathClient);

            String inPackage = myReadChannel();
            System.out.println(inPackage);

            if (inPackage.equals(CommandChannel.ERROR.toString())){
                labelStatus.setText("invalid password / login");
            }
            if (inPackage.equals(CommandChannel.PATHNONE.toString())){
                labelStatus.setText("directory is missing");
            }
            if (inPackage.equals(CommandChannel.IDENTIFI.toString())){
                labelStatus.setText("welcome");
                return true;
            }


        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public void changeSceneToMain() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Parent mainScene = FXMLLoader.load(getClass().getResource("main.fxml"));
                        ((Stage) btnAuto.getScene().getWindow()).setScene(new Scene(mainScene));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void direct(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        file = directoryChooser.showDialog(btnAuto.getScene().getWindow());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.properties = new ClientProperties();
            this.socketChannel = SocketChannel.open(new InetSocketAddress(this.properties.getPort()));
            this.byteBuffer = ByteBuffer.allocate(this.properties.getIdentifiSizeBuffer());

            this.currentState = CommandChannel.START;
        }catch (IOException e){
//            e.printStackTrace();
            System.err.println("server is not started");
            Platform.exit();
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

//    private void myWriteChannel(String line) throws IOException {
//        ByteBuffer tmpPack = ByteBuffer.wrap(line.getBytes());
//        this.socketChannel.write(tmpPack);
//        System.out.println("отправил");
//    }
    private void myWriteChannel(String line) throws IOException {
        this.byteBuffer.clear();
        this.byteBuffer.put(line.getBytes());
        this.byteBuffer.flip();
        this.socketChannel.write(this.byteBuffer);
        this.byteBuffer.clear();
    }
}
