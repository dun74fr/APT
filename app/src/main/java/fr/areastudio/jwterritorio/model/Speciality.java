package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

import fr.areastudio.jwterritorio.common.UUIDGenerator;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Speciality")
public class Speciality extends Model {

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "name")
    public String name;


    public Speciality() {
        super();
    }



}
