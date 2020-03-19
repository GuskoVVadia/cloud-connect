import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("identificationView.fxml"));
        primaryStage.setTitle("identity");
        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }


}
