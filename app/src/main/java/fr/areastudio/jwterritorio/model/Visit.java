package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

import fr.areastudio.jwterritorio.common.UUIDGenerator;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Visit")
public class Visit extends Model {

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "address", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Address address;

    @Column(name = "type")
    public String type;

    @Column(name = "date")
    public Date date;

    @Column(name = "notes")
    public String notes;

    @Column(name = "next_theme")
    public String nextTheme;

    @Column(name = "next_visit_date")
    public Date nextVisitDate;

    @Column(name = "publisher", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Publisher publisher;

    @Column(name = "publication")
    public int publication;

    @Column(name = "video")
    public int video;

    @Column(name = "creation_date")
    public Date creationDate;

    @Column(name = "creator_uuid")
    public String creatorUuid;

    @Column(name = "update_date")
    public Date updaterDate;

    @Column(name = "updater_uuid")
    public String updaterUuid;


    public Visit() {
        super();
    }

    public Visit(Address address) {
        this.uuid = UUIDGenerator.uuidToBase64();
        this.address = address;
        this.date = new Date();
        this.type = "visit";
    }

}
