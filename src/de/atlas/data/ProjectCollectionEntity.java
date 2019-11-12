package de.atlas.data;

import java.io.File;

/**
 * Created by smeudt on 13.02.2015.
 */
public class ProjectCollectionEntity implements Comparable {
    private String path;
    private String name;
    public ProjectCollectionEntity(String path, String name){
        this.name = name;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public void setPath(File file) {
        this.path = file.getAbsolutePath();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Object o) {
        int res = String.CASE_INSENSITIVE_ORDER.compare(name, ((ProjectCollectionEntity)o).getName());
        if (res == 0) {
            res = name.compareTo(((ProjectCollectionEntity)o).getName());
        }
        return res;
    }
    @Override
    public String toString(){
        return this.name;
    }
}
