/*
*The class contains the methods needed to create and maintain a playlist in the SQLite database.
* It also includes methods for operating the MP3 player buttons.
*/

package sample;

import javafx.stage.FileChooser;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class Player extends Thread {

    private File songFile;
    private javazoom.jl.player.Player player;
    private String songPath;
    private String currentSongPath;
    private static Player playlist = new Player();
    private boolean pausedSong = false;
    private Thread currentPlayingSongThread;

    private Connection conn;
    private PreparedStatement queryAddSong;
    private PreparedStatement querySong;
    private PreparedStatement queryCreateTable;
    private PreparedStatement queryUpdate;
    private PreparedStatement queryRemoveSong;
    private PreparedStatement querySongId;
    private PreparedStatement querySongPath;

    private static final String TABLE_PLAYLIST = "playlist";
    private static final String COLUMN_PLAYLIST_TITLE = "title";
    private static final String COLUMN_PLAYLIST_PATH = "path";
    private static final String COLUMN_PLAYLIST_ID = "_id";
    private static final String DB_NAME = "playlist.db";

    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLIST + "( " +
            COLUMN_PLAYLIST_ID + " INT PRIMARY KEY," + COLUMN_PLAYLIST_PATH + " TEXT, " + COLUMN_PLAYLIST_TITLE + " TEXT)";
    private static final String CONNECTION_STRING = "jdbc:sqlite:C:\\Users\\gabbi\\Documents\\JavaProjects\\MP3Player\\"
            + DB_NAME;
    private static final String QUERY_ADD_SONG = "INSERT INTO " + TABLE_PLAYLIST + " (" +
            COLUMN_PLAYLIST_PATH + ", " + COLUMN_PLAYLIST_TITLE + ") VALUES(?, ?)";
    private static final String QUERY_UPDATE_ID = "UPDATE " + TABLE_PLAYLIST + " SET " + COLUMN_PLAYLIST_ID +
            " = ? WHERE " + COLUMN_PLAYLIST_TITLE + " = ?";
    private static final String QUERY_SONG = "SELECT " + COLUMN_PLAYLIST_TITLE + " FROM " + TABLE_PLAYLIST + " WHERE " +
            COLUMN_PLAYLIST_PATH + " = ?";
    private static final String QUERY_SONG_ID = "SELECT " + COLUMN_PLAYLIST_ID + " FROM " + TABLE_PLAYLIST + " WHERE " +
            COLUMN_PLAYLIST_PATH + " = ?";
    private static final String QUERY_SONG_PATH = "SELECT " + COLUMN_PLAYLIST_PATH + " FROM " + TABLE_PLAYLIST + " WHERE " +
            COLUMN_PLAYLIST_ID + " = ?";
    private static final String QUERY_REMOVE_SONG = "DELETE FROM " + TABLE_PLAYLIST + " WHERE " + COLUMN_PLAYLIST_ID +
            " = ?";

    private Player() {
        currentPlayingSongThread = new Thread(this::run);
    }

    public static Player getPlaylist() {
        return playlist;
    }

    public boolean open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
            queryCreateTable = conn.prepareStatement(QUERY_CREATE_TABLE);
            queryCreateTable.execute();
            queryAddSong = conn.prepareStatement(QUERY_ADD_SONG);
            querySong = conn.prepareStatement(QUERY_SONG);
            queryUpdate = conn.prepareStatement(QUERY_UPDATE_ID);
            queryRemoveSong = conn.prepareStatement(QUERY_REMOVE_SONG);
            querySongId = conn.prepareStatement(QUERY_SONG_ID);
            querySongPath = conn.prepareStatement(QUERY_SONG_PATH);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if (queryCreateTable != null) {
                queryCreateTable.close();
            }

            if (queryAddSong != null) {
                queryAddSong.close();
            }

            if (querySong != null) {
                querySong.close();
            }

            if (queryUpdate != null) {
                queryUpdate.close();
            }

            if (queryRemoveSong != null) {
                queryRemoveSong.close();
            }

            if (querySongId != null) {
                querySongId.close();
            }

            if (querySongPath != null) {
                querySongPath.close();
            }

            if (conn != null) {
                conn.close();
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addSong() {
        try {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Choose song to play...");
                songFile = chooser.showOpenDialog(null);

            querySong.setString(1, songFile.getAbsoluteFile().getPath());
            ResultSet resultSet = querySong.executeQuery();

            if (resultSet.next()) {
                System.out.println("Song " + resultSet.getString(1) + " has already added to playlist!");
            } else {

                songPath = songFile.getAbsoluteFile().getPath();

                queryAddSong.setString(1, songPath);
                queryAddSong.setString(2, songFile.getAbsoluteFile().getName());
                queryAddSong.executeUpdate();

                ResultSet generateKeys = queryAddSong.getGeneratedKeys();
                int primary_key =  generateKeys.getInt(1);

                queryUpdate.setInt(1, primary_key);
                queryUpdate.setString(2, songFile.getAbsoluteFile().getName());
                queryUpdate.executeUpdate();

            }
        } catch (Exception e) {
            System.out.println();
        }
    }

    public void removeSong(int songId) {
        try {
            queryRemoveSong.setInt(1, songId);
            queryRemoveSong.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Remove error: " + e.getMessage());
        }
    }

    public void playSong(String newSongPath) throws IOException {

        if (pausedSong) {
            if (isNewSongChoosen(newSongPath)) {
                playNewSong(newSongPath);
            } else {
                currentPlayingSongThread.resume();
                pausedSong = false;
            }
        } else {
            playNewSong(newSongPath);
            currentSongPath = newSongPath;
        }
    }

    public void playNextSong() throws IOException {
        try {
            querySongId.setString(1, currentSongPath);
            ResultSet resultSet = querySongId.executeQuery();
            int id = resultSet.getInt(1);
            id++;

            querySongPath.setInt(1, id);
            resultSet = querySongPath.executeQuery();
            currentSongPath = resultSet.getString(1);

            playSong(currentSongPath);

        } catch (SQLException e) {
            System.out.println("Next song error: " + e.getMessage());
        }
    }

    public void playPreviousSong() throws IOException {
        try {
            querySongId.setString(1, currentSongPath);
            ResultSet resultSet = querySongId.executeQuery();
            int id = resultSet.getInt(1);
            id--;

            querySongPath.setInt(1, id);
            resultSet = querySongPath.executeQuery();
            currentSongPath = resultSet.getString(1);

            playSong(currentSongPath);

        } catch (SQLException e) {
            System.out.println("Previous song error: " + e.getMessage());
        }
    }

    private boolean isNewSongChoosen(String newSongPath) {
        return currentSongPath != newSongPath;
    }

    private void playNewSong(String newPathSong) {
        currentPlayingSongThread.stop();
        currentPlayingSongThread = new Thread(this::run);
        this.songPath = newPathSong;
        currentPlayingSongThread.start();
    }

    @Override
    public void run() {
        try(FileInputStream fis = new FileInputStream(songPath)){
            currentSongPath = songPath;
            player = new javazoom.jl.player.Player(fis);
            player.play();

        } catch(Exception e){System.out.println(e);}
    }

    public void stopSong() {
        if (currentPlayingSongThread.isAlive()) {
            currentPlayingSongThread.stop();
        } else {
            System.out.println("Thread is not alive! Song isn't playing!");
        }
    }

    public void pauseSong() {
        if (isSongPlaying()) {
            currentPlayingSongThread.suspend();
            pausedSong = true;
        } else {
            currentPlayingSongThread.resume();
            pausedSong = false;
        }
    }

    private boolean isSongPlaying() {
        return (currentPlayingSongThread.isAlive() && !pausedSong);
    }

    public List<Song> queryPlaylist() {

        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(TABLE_PLAYLIST);

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(sb.toString())) {

            List<Song> songs = new ArrayList<>();
            while (results.next()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    System.out.println("Interuppted: " + e.getMessage());
                }
                Song song = new Song();
                song.setId(results.getInt(1));
                song.setPath(results.getString(2));
                song.setTitle(results.getString(3));
                songs.add(song);
            }

            return songs;

        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return null;
        }
    }
}
