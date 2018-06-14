package fr.areastudio.jwterritorio.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

import fr.areastudio.jwterritorio.common.UUIDGenerator;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Assignment")
public class Assignment extends Model {


    @Column(name = "uuid")
    public String uuid;

    @Column(name = "territory", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Territory territory;

    @Column(name = "publisher", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Publisher publisher;

    @Column(name = "assign_date")
    public Date assignDate;

    @Column(name = "back_date")
    public Date backDate;

    @Column(name = "creation_date")
    public Date creationDate;

    @Column(name = "creator_uuid")
    public String creatorUuid;

    @Column(name = "update_date")
    public Date updaterDate;

    @Column(name = "updater_uuid")
    public String updaterUuid;


    public Assignment() {
        super();
    }

    public Assignment(Territory territory, Publisher publisher) {

        this.uuid = UUIDGenerator.uuidToBase64();
        this.territory = territory;
        this.publisher = publisher;
        this.assignDate = new Date();
    }


}
