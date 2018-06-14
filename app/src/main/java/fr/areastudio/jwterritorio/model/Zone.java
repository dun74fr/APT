package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Zone")
public class Zone extends Model {

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "name")
    public String name;


    public Zone() {
        super();
    }



}
