package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Territory")
public class Territory extends Model {

    @Column(name = "uuid")
    public String uuid;

//    @Column(name = "congregation", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
//    public Congregation congregation;

    @Column(name = "number")
    public String number;

    @Column(name = "name")
    public String name;

    @Column(name = "publisher", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Publisher assignedPub;

    @Column(name = "image")
    public String image;


    public Territory() {
        super();
    }

    public Territory(String name) {
        this.name = name;
    }

    public List<Address> getAddresses() {
        return new Select().from(Address.class).where("territory = ?", this.getId()).orderBy("name desc").execute();
    }

}
