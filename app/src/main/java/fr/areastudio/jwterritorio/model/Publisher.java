package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name = "Publisher")
public class Publisher extends Model {

    @Column(name = "uuid")
    public String uuid;

//    @Column(name = "congregation", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
//    public Congregation congregation;

    @Column(name = "name")
    public String name;

    @Column(name = "email")
    public String email;

    @Column(name = "type")
    public String type;


    public Publisher() {
        super();
    }

    public Publisher(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
