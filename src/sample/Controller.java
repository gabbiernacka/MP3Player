package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import java.io.IOException;

public class Controller {

    @FXML
    private TableView<Song> playlistTable;

    @FXML
    public void listPlaylist() {
        GetAllPlaylistTask task = new GetAllPlaylistTask();
        playlistTable.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    @FXML
    public void play() throws IOException {
        final Song song = playlistTable.getSelectionModel().getSelectedItem();
        if (song == null) {
            System.out.println("No song selected!");
            return;
        }

        Player.getPlaylist().playSong(song.getPath());

    }

    @FXML
    public void playNext() throws IOException {
        Player.getPlaylist().playNextSong();
    }

    @FXML
    public void playPrevious() throws IOException {
        Player.getPlaylist().playPreviousSong();
    }

    @FXML
    public void remove() {
        final Song song = playlistTable.getSelectionModel().getSelectedItem();
        if (song == null) {
            System.out.println("No song selected!");
            return;
        }
        Player.getPlaylist().removeSong(song.getId());

        GetAllPlaylistTask task = new GetAllPlaylistTask();
        playlistTable.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    @FXML
    public void stop() {
        Player.getPlaylist().stopSong();
    }

    @FXML
    public void pause() {
        Player.getPlaylist().pauseSong();
    }

    @FXML
    public void chooser() {
        try {
            Player.getPlaylist().addSong();
        } catch (Exception e) {
            System.out.println();
        }
        GetAllPlaylistTask task = new GetAllPlaylistTask();
        playlistTable.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    class GetAllPlaylistTask extends Task {
        @Override
        public ObservableList<Song> call() {
            return FXCollections.observableArrayList(Player.getPlaylist().queryPlaylist());
        }
    }


}
