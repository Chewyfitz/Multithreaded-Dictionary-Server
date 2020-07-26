// DictionaryGUI.java: The dictionary itself
/* Aidan Fitzpatrick (fitzpatricka) 835833 for Distributed Systems COMP90015 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Platform.exit;

public class DictionaryGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        // requires JavaFX
        Parent root = FXMLLoader.load(getClass().getResource("DictionaryGUI.fxml"));
        primaryStage.setTitle("Dictionary Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop(){
        // Disconnect
        System.out.println("Disconnected.");
        exit();
    }

}
