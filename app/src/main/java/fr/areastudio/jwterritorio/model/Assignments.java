package fr.areastudio.jwterritorio.model;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "Assignments")
public class Assignments extends Model {


    @Column(name = "uuid")
    public String uuid;

    @Column(name = "territory", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Territory territory;

    @Column(name = "publisher", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public Publisher publisher;

    @Column(name = "assign_date")
    public Date dateBegin;

    @Column(name = "back_date")
    public Date dateEnd;

    public Assignments() {
        super();
    }

    public Assignments(String uuid,Territory territory, Publisher publisher,Date dateBegin, Date dateEnd) {

        this.uuid =uuid;
        this.territory = territory;
        this.publisher = publisher;
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
    }


}
