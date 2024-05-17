package enity;

import java.util.ArrayList;
import java.util.List;

public class Pic {

    public int id;
    public String path_leaf;
    public String path_seg;
    public float severity;
    public String name;
    public String date;

    public Pic(){


    }

    public Pic(String path_leaf, String path_seg, float severity, String name, String date) {
        this.path_leaf = path_leaf;
        this.path_seg = path_seg;
        this.severity = severity;
        this.name = name;
        this.date = date;

    }

    public int length(){
        return date.length();
    }



    @Override
    public String toString() {
        return "Pic{" +
                "path_leaf='" + path_leaf + '\'' +
                ", path_seg='" + path_seg + '\'' +
                ", severity=" + severity +
                '}';
    }
}
