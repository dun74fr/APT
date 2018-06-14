package fr.areastudio.jwterritorio.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.Date;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.CommonTools;
import fr.areastudio.jwterritorio.common.UUIDGenerator;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;

public class NewAddressActivity extends AppCompatActivity {

    EditText name;
    RadioGroup gender;
    Spinner age;
    Spinner language;
    CheckBox deaf;
    CheckBox mute;
    CheckBox blind;
    CheckBox sign;
    CheckBox notAtHome;
    CheckBox visit;
    CheckBox course;
    CheckBox type_phone;
    EditText address;
    EditText phone;
    EditText lat;
    EditText lng;
    EditText homeDescription;
    EditText description;


    private ImageButton mapBtn;
    int PLACE_PICKER_REQUEST = 1;
    private SharedPreferences settings;
    private Address currentaddress;
    private TextView familyDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_address);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        name = findViewById(R.id.name);
        gender = findViewById(R.id.gender);
        age = findViewById(R.id.age);
        language = findViewById(R.id.language);
        deaf = findViewById(R.id.deaf);
        mute = findViewById(R.id.mute);
        blind = findViewById(R.id.blind);
        sign = findViewById(R.id.sign);
        address = findViewById(R.id.address);
        mapBtn = findViewById(R.id.map_btn);
        lat = findViewById(R.id.lat);
        lng = findViewById(R.id.lng);
        phone = findViewById(R.id.phone);
        homeDescription = findViewById(R.id.home_description);
        familyDescription = findViewById(R.id.familyDescription);
        description = findViewById(R.id.notes);
        notAtHome = findViewById(R.id.not_at_home);
        type_phone = findViewById(R.id.type_phone);
        visit = findViewById(R.id.visit);
        course = findViewById(R.id.course);


        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                 try {
                    startActivityForResult(builder.build(NewAddressActivity.this), PLACE_PICKER_REQUEST);
                }catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){

                }
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras().getLong("address_id") > 0){
            initView(intent.getExtras().getLong("address_id"));
        }
    }

    private void initView(long address_id) {
        currentaddress = new Select().from(Address.class).where("id = ?",address_id).executeSingle();
        getSupportActionBar().setTitle(R.string.action_edit);
//        if (currentaddress.optIn) {
            name.setText(currentaddress.name);
//        }
//        else {
//            name.setText(R.string.private_data);
//        }
        address.setText(currentaddress.address);
        gender.check(currentaddress.gender.equals("m") ? R.id.male:R.id.female);
        deaf.setChecked(currentaddress.deaf);
        mute.setChecked(currentaddress.mute);
        sign.setChecked(currentaddress.sign);
        phone.setText(currentaddress.phone);
        lat.setText(currentaddress.lat);
        lng.setText(currentaddress.lng);
        description.setText(currentaddress.description);
        homeDescription.setText(currentaddress.homeDescription);
        familyDescription.setText(currentaddress.familyDescription);
        language.setSelection(CommonTools.getPositioninArray(this,R.array.languages_values,currentaddress.language));
        age.setSelection(CommonTools.getPositioninArray(this,R.array.ages_values,currentaddress.age));
        visit.setChecked(currentaddress.type.contains("VISIT"));
        course.setChecked(currentaddress.type.contains("COURSE"));
        notAtHome.setChecked(currentaddress.type.contains("NOT_AT_HOME"));

        this.address.setText(currentaddress.address);


    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                lat.setText(String.valueOf(place.getLatLng().latitude));
                lng.setText(String.valueOf(place.getLatLng().longitude));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_address, menu);
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
            if (((lat.getText().length() == 0 || lng.getText().length() == 0 || address.getText().length() == 0) && !type_phone.isChecked()) ||(type_phone.isChecked() && phone.getText().length() == 0)){
                new AlertDialog.Builder(this).setMessage(R.string.not_enough_info).create().show();
                return true;
            }
            Publisher me = ((MyApplication)getApplication()).getMe();
            Address a;
            DbUpdate update = new DbUpdate();
            update.publisherUuid = me.uuid;
            update.date = new Date();
            update.model = "ADDRESS";

            if (currentaddress != null) {
                a= currentaddress;
                a.updaterDate = new Date();
                a.updaterUuid = me.uuid;
                update.updateType = "UPDATE";
            }
            else {
                a = new Address();
                a.creationDate = new Date();
                a.creatorUuid = me.uuid;
                update.updateType = "CREATE";
            }
            a.gender = gender.getCheckedRadioButtonId() == R.id.female ? "f" :"m";
            a.name = name.getText().toString();
            a.language = getResources().getStringArray(R.array.languages_values)[language.getSelectedItemPosition()];
            a.age = getResources().getStringArray(R.array.ages_values)[age.getSelectedItemPosition()];
            a.phone = phone.getText().toString();
            a.deaf = deaf.isChecked();
            a.mute = mute.isChecked();
            a.sign = sign.isChecked();
            a.blind = blind.isChecked();
            a.address = address.getText().toString();
            a.homeDescription = homeDescription.getText().toString();
            a.familyDescription = familyDescription.getText().toString();
            a.description = description.getText().toString();
            a.status = a.status != null && a.status.equals("VALIDATE") ? "VALIDATE" : "DRAFT";
            String type = "";
            if (notAtHome.isChecked()){
                type = "NOT_AT_HOME";
            }
            if (visit.isChecked()){
                if (type.length() > 0){
                    type += "|";
                }
                type += "VISIT";
            }
            if (course.isChecked()){
                if (type.length() > 0){
                    type += "|";
                }
                type += "BIBLE_COURSE";
            }
            if (type_phone.isChecked()){
                if (type.length() > 0){
                    type += "|";
                }
                type += "PHONE";
            }
            a.type = type;
            a.lat = lat.getText().toString();
            a.lng = lng.getText().toString();


            if (a.territory == null) {
                Territory undefined = new Select().from(Territory.class).where("number = -1").executeSingle();
                a.territory = undefined;
            }
            if (a.getId() == null) {
                a.assignedPub = me;
            }
            a.save();
            update.uuid = a.uuid;
            update.save();

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
