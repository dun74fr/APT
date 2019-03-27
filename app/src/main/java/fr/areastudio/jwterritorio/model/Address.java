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
@Table(name = "Address")
public class Address extends Model implements Parcelable {


    @Column(name = "uuid")
    public String uuid;

    @Column(name = "territory", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Territory territory;

    @Column(name = "name")
    public String name = "";

    @Column(name = "address")
    public String address = "";

    @Column(name = "gender")
    public String gender = "m";

    @Column(name = "lat")
    public String lat = "";

    @Column(name = "lng")
    public String lng = "";

    @Column(name = "home_description")
    public String homeDescription = "";

    @Column(name = "phone")
    public String phone = "";

    @Column(name = "age")
    public String age;

    @Column(name = "language")
    public String language;

    @Column(name = "deaf")
    public boolean deaf;

    @Column(name = "mute")
    public boolean mute;

    @Column(name = "blind")
    public boolean blind;

    @Column(name = "sign")
    public boolean sign;

    @Column(name = "description")
    public String description;

    @Column(name = "type")
    public String type = "";

    @Column(name = "status")
    public String status = "";

    @Column(name = "publisher")
    public Publisher assignedPub;

    @Column(name = "creation_date")
    public Date creationDate;

    @Column(name = "creator_uuid")
    public String creatorUuid;

    @Column(name = "update_date")
    public Date updaterDate;

    @Column(name = "updater_uuid")
    public String updaterUuid;

    @Column(name = "opt_in")
    public boolean optIn;

    @Column(name = "family_description")
    public String familyDescription = "";

    @Column(name = "my_local_dir")
    public boolean myLocalDir;

    @Column(name = "last_visit")
    public Date lastVisit;

    public Address() {
        super();
        this.uuid = UUIDGenerator.uuidToBase64();
    }

    public Address(String name) {
        this.uuid = UUIDGenerator.uuidToBase64();
        this.name = name;
    }

//    public Visit getLastVisit() {
//        List<Visit> v = new Select().from(Visit.class).where("address = ?",this.getId()).orderBy("date desc").execute();
//        for (Visit vis : v) {
//            if (!"NOT AT HOME".equals(vis.type)){
//                return vis;
//            }
//        }
//        return null;
//    }

    public List<Visit> getVisits() {
        return new Select().from(Visit.class).where("address = ?", this.getId()).orderBy("date desc").execute();
    }
    protected Address(Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
}
