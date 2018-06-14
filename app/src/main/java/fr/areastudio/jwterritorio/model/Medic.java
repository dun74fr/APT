package fr.areastudio.jwterritorio.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.areastudio.jwterritorio.common.UUIDGenerator;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Medic")
public class Medic extends Model {


    @Column(name = "category", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Category category;

    @Column(name = "name")
    public String name = "";

    @Column(name = "address")
    public String address = "";

    @Column(name = "city")
    public String city = "";

    @Column(name = "rating")
    public String rating = "";

    @Column(name = "image")
    public String image = "";

    @Column(name = "lat")
    public String lat = "";

    @Column(name = "lng")
    public String lng = "";

    @Column(name = "insurance")
    public boolean insurance;

    @Column(name = "witness")
    public boolean witness;

    @Column(name = "lang_level")
    public boolean langLevel;

    @Column(name = "phone")
    public String phone = "";

    @Column(name = "mobile")
    public String mobile = "";

    @Column(name = "additional")
    public String additional;

    @Column(name = "privileges")
    public String privileges;

    @Column(name = "pharmacy")
    public boolean pharmacy;

    @Column(name = "intensive_therapy")
    public boolean intensiveTherapy;

    @Column(name = "speciality", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Speciality speciality;

    @Column(name = "price_list")
    public Double priceList;

    @Column(name = "zone", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Zone zone;

    @Column(name = "special_cases")
    public String specialCases;

    @Column(name = "credit")
    public boolean credit;

    @Column(name = "operating_room")
    public boolean operatingRoom;

    public Medic() {
        super();

    }


}
