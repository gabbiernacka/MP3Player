package sample;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Song {
    private SimpleIntegerProperty id;
    private SimpleStringProperty path;
    private SimpleStringProperty title;

    public Song() {
        this.id = new SimpleIntegerProperty();
        this.path = new SimpleStringProperty();
        this.title = new SimpleStringProperty();
    }

    public int getId() {
        return id.get();
    }

    public String getPath() {
        return path.get();
    }

    public String getTitle() {
        return title.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public void setTitle(String title) {
        this.title.set(title);
    }
}
