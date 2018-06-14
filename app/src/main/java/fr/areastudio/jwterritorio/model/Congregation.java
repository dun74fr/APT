package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.sql.Date;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Congregation")
public class Congregation extends Model {

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "number")
    public String number;

    @Column(name = "name")
    public String name;

    @Column(name = "address")
    public String address;

    @Column(name = "lat")
    public String lat;

    @Column(name = "lng")
    public String lng;

    @Column(name = "city")
    public String city;



    public Congregation() {
        super();
    }

    public Congregation(String name) {
        this.name = name;
    }

}
