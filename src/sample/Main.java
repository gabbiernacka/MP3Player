/*

* The MP3 player allows you to play .mp3 files and create your own playlist in the SQLite database.
Features of the MP3 player:
- start of the .mp3 file;
- stop the .mp3 file;
- pause of the .mp3 file;
- resumption of playing the .mp3 file;
- scroll to the next .mp3 file;
- scroll to the previous .mp3 file;
*
* */
package sample;

import javafx.application.Platform;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.listPlaylist();
        primaryStage.setTitle("MP3 Player");
        primaryStage.setScene(new Scene(root, 700, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (!Player.getPlaylist().open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Player.getPlaylist().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}