package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

import fr.areastudio.jwterritorio.common.UUIDGenerator;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "DbUpdate")
public class DbUpdate extends Model {


    @Column(name = "model")
    public String model;

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "updateType")
    public String updateType;

    @Column(name = "date")
    public Date date;

    @Column(name = "publisher_uuid")
    public String publisherUuid;


    public DbUpdate() {
        super();
        this.date = new Date();
    }


}
