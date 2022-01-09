package at.dcosta.tracks.util;

import java.io.Serializable;
import java.util.Objects;

public class Photo implements Comparable, Serializable {

    private String path;
    private long createdOn;
    private int width;
    private int height;
    private int orientation;

    public void setPath(String path) {
        this.path = path;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(path, photo.path) && Objects.equals(createdOn, photo.createdOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, createdOn);
    }

    @Override
    public int compareTo(Object o) {
        if (o != null && o.getClass() == Photo.class) {
            if (getPath() == null) {
                return -1;
            }
            return getPath().compareTo(((Photo) o).getPath());
        }
        return 1;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "path='" + path + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", orientation=" + orientation +
                ", createdOn=" + createdOn +
                '}';
    }
}
