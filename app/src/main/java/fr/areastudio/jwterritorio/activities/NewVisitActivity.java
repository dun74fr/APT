package fr.areastudio.jwterritorio.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.activeandroid.query.Select;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.CommonTools;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Visit;

public class NewVisitActivity extends AppCompatActivity {

    TextView date;
    TextView time;
    Spinner result;
    EditText notes;
//    EditText verses;
    EditText publication;
    EditText video;
    EditText nextTheme;
    TextView nextDate;
    TextView nextTime;
//    EditText coursePublication;
//    EditText courseBookmark;
    private SimpleDateFormat dateformater;
    private SimpleDateFormat timeformater;
    private Address address;
    Visit visit;
    private Calendar calendar;
    private Calendar nextCalendar;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        setContentView(R.layout.activity_new_visit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        calendar = Calendar.getInstance();
        nextCalendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        result = findViewById(R.id.result);
        notes = findViewById(R.id.notes);
        nextTheme = findViewById(R.id.nextTheme);
        nextDate = findViewById(R.id.nextdate);
        nextTime = findViewById(R.id.nexttime);
        publication = findViewById(R.id.publication);
        video = findViewById(R.id.video);
        dateformater = new SimpleDateFormat("dd-MMM-yyyy");
        timeformater = new SimpleDateFormat("HH:mm");

        date.setText(dateformater.format(calendar.getTime()));

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int mYear = calendar.get(Calendar.YEAR);
                final int mMonth = calendar.get(Calendar.MONTH);
                final int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(NewVisitActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox

                                if (year < mYear)
                                    view.updateDate(mYear,mMonth,mDay);

                                if (monthOfYear < mMonth && year == mYear)
                                    view.updateDate(mYear,mMonth,mDay);

                                if (dayOfMonth < mDay && year == mYear && monthOfYear == mMonth)
                                    view.updateDate(mYear,mMonth,mDay);
                                calendar.set(year, monthOfYear,dayOfMonth);

                                date.setText(dateformater.format(calendar.getTime()));
//                                date.setText(dayOfMonth + "-"
//                                        + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                //dpd.getDatePicker().setMinDate(System.currentTimeMillis());
                dpd.show();
            }
        });

        time.setText(timeformater.format(calendar.getTime()));

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dpd = new TimePickerDialog(NewVisitActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {


                            @Override
                            public void onTimeSet(TimePicker timePicker, int h, int m) {
                                calendar.set(Calendar.HOUR_OF_DAY,h);
                                calendar.set(Calendar.MINUTE,m);
                                time.setText(timeformater.format(calendar.getTime()));
                            }
                        }, hour, minute, true);

                dpd.show();
            }
        });

        nextDate.setText(R.string.indefinida);

        nextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int mYear = nextCalendar.get(Calendar.YEAR);
                final int mMonth = nextCalendar.get(Calendar.MONTH);
                final int mDay = nextCalendar.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(NewVisitActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox

                                if (year < mYear)
                                    view.updateDate(mYear,mMonth,mDay);

                                if (monthOfYear < mMonth && year == mYear)
                                    view.updateDate(mYear,mMonth,mDay);

                                if (dayOfMonth < mDay && year == mYear && monthOfYear == mMonth)
                                    view.updateDate(mYear,mMonth,mDay);
                                nextCalendar.set(year, monthOfYear,dayOfMonth);

                                nextDate.setText(dateformater.format(nextCalendar.getTime()));
//                                date.setText(dayOfMonth + "-"
//                                        + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                //dpd.getDatePicker().setMinDate(System.currentTimeMillis());
                dpd.show();
            }
        });

        nextTime.setText(R.string.indefinida);

        nextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dpd = new TimePickerDialog(NewVisitActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {


                            @Override
                            public void onTimeSet(TimePicker timePicker, int h, int m) {
                                nextCalendar.set(Calendar.HOUR_OF_DAY,h);
                                nextCalendar.set(Calendar.MINUTE,m);
                                nextTime.setText(timeformater.format(nextCalendar.getTime()));
                            }
                        }, hour, minute, true);

                dpd.show();
            }
        });


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null){
           if ( intent.getExtras().getLong("address_id") > 0) {
               address = new Select().from(Address.class).where("id = ?",intent.getExtras().getLong("address_id")).executeSingle();
               if (intent.getExtras().getLong("visit_id") > 0) {
                   initView(intent.getExtras().getLong("visit_id"));
               }
               else {
                   visit = new Visit(address);
                   visit.publisher = new Select().from(Publisher.class).where("email = ?", settings.getString("user","")).executeSingle();
               }
           }else {
               //Toast.makeText(this,"")
               finish();
           }
        }
    }

    private void initView(long visit_id) {
        visit = new Select().from(Visit.class).where("id = ?",visit_id).executeSingle();
        calendar.setTime(visit.date);
        date.setText(dateformater.format(calendar.getTime()));
        time.setText(timeformater.format(calendar.getTime()));
        if (visit.nextVisitDate != null){
            nextCalendar.setTime(visit.nextVisitDate);
            nextDate.setText(dateformater.format(nextCalendar.getTime()));
            nextTime.setText(timeformater.format(nextCalendar.getTime()));
        }
        result.setSelection(CommonTools.getPositioninArray(this,R.array.visits_results_values,visit.type));
        nextTheme.setText(visit.nextTheme);
        notes.setText(visit.notes);
        publication.setText(String.valueOf(visit.publication));
        video.setText(String.valueOf(visit.video));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_visit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ok) {
            if (result.getSelectedItemPosition() == 0 && notes.getText().toString().length() == 0 ){
                new AlertDialog.Builder(this).setMessage(R.string.fill_notes).create().show();
                return true;
            }
            Publisher me = ((MyApplication)getApplication()).getMe();
            visit.address = address;
            visit.date = calendar.getTime();
            visit.type = getResources().getStringArray(R.array.visits_results_values)[result.getSelectedItemPosition()];
            visit.nextTheme = nextTheme.getText().toString();
            visit.publication = Integer.parseInt("".equals(publication.getText().toString()) ? "0" : publication.getText().toString());
            visit.video = Integer.parseInt("".equals(video.getText().toString()) ? "0" : video.getText().toString());
            if (address.lastVisit == null || address.lastVisit.getTime() < visit.date.getTime()){
                address.lastVisit = visit.date;
                address.save();
            }
            if (!nextDate.getText().toString().equals(getString(R.string.indefinida))){
                visit.nextVisitDate = nextCalendar.getTime();
            }
            visit.notes = notes.getText().toString();
            visit.publisher = me;


            DbUpdate update = new DbUpdate();
            update.model = "VISIT";
            update.date = new Date();
            update.publisherUuid = me.uuid;
            update.uuid = visit.uuid;
            if (visit.getId() != null && visit.getId() > 0){
                visit.updaterUuid = visit.publisher.uuid;
                visit.updaterDate = new Date();
                update.updateType = "UPDATE";
            }
            else {
                visit.creationDate = new Date();
                visit.creatorUuid = visit.publisher.uuid;
                update.updateType = "CREATE";
            }
            visit.save();
            //if (address.territory.assignedPub != null && address.territory.assignedPub.uuid == me.uuid) {
                update.save();
            //}
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
