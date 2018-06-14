package fr.areastudio.jwterritorio.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by julien on 17.06.15.
 */
@Table(name = "News")
public class News extends Model {

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "date")
    public Date date;

    @Column(name = "title")
    public String title;

    @Column(name = "content")
    public String content;

    @Column(name = "alert")
    public boolean alert;

    @Column(name = "read")
    public boolean read;

    @Column(name = "presistent")
    public boolean persistent;

    public News() {
        super();
    }



}
