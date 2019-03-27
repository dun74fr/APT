package fr.areastudio.jwterritorio.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.CommonTools;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Visit;

public class ViewAddressActivity extends AppCompatActivity {

    TextView name;
    ImageView gender;
    TextView age;
    TextView language;
    CheckBox deaf;
    CheckBox mute;
    CheckBox blind;
    CheckBox sign;
    TextView addressText;
    TextView phone;
    TextView homeDescription;
    TextView familyDescription;
    Address address;
    //    CheckBox notAtHome;
//    CheckBox visit;
//    CheckBox course;
//    CheckBox type_phone;
    private long address_id;
    private TextView description;
    private FloatingActionButton fab;
    private VisitsAdapter mVisitAdapter;

    private boolean visitIgnore;
    private boolean studyIgnore;
    private boolean notAtHomeIgnore;
    private boolean type_phoneIgnore;
    private Menu menu;
    private Spinner contactType;
    private TextView publisher;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        setContentView(R.layout.activity_view_address);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        name = findViewById(R.id.name);
        gender = findViewById(R.id.icon_gender);
        age = findViewById(R.id.age);
        language = findViewById(R.id.language);
        deaf = findViewById(R.id.deaf);
        mute = findViewById(R.id.mute);
        blind = findViewById(R.id.blind);
        sign = findViewById(R.id.sign);
        addressText = findViewById(R.id.address);
        description = findViewById(R.id.description);
        phone = findViewById(R.id.phone);
        homeDescription = findViewById(R.id.home_description);
        familyDescription = findViewById(R.id.familyDescription);
//        notAtHome = findViewById(R.id.not_at_home);
//        type_phone = findViewById(R.id.type_phone);
//        visit = findViewById(R.id.visit);
//        course = findViewById(R.id.course);
        contactType = findViewById(R.id.contactType);
        publisher = findViewById(R.id.publisher);
        RecyclerView recyclerView = findViewById(R.id.visits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewAddressActivity.this, NewVisitActivity.class);
                intent.putExtra("address_id", address_id);
                ViewAddressActivity.this.startActivity(intent);
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras().getLong("address_id") > 0) {
            initView(intent.getExtras().getLong("address_id"));
        }

        mVisitAdapter = new VisitsAdapter(this, address.getVisits());
        mVisitAdapter.setListener(new VisitsAdapter.VisitListener() {
            @Override
            public void onClick(Visit visit) {
                Intent intent = new Intent(ViewAddressActivity.this, NewVisitActivity.class);
                intent.putExtra("address_id", address.getId());
                intent.putExtra("visit_id", visit.getId());
                ViewAddressActivity.this.startActivity(intent);
            }
        });
        recyclerView.setAdapter(mVisitAdapter);


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getExtras().getLong("address_id") > 0) {
            initView(intent.getExtras().getLong("address_id"));
        }
    }

    private void initView(final long address_id) {

        fab.setVisibility(View.VISIBLE);
        this.address_id = address_id;
        address = new Select().from(Address.class).where("id = ?", address_id).executeSingle();


        name.setText(address.name);
        deaf.setChecked(address.deaf);
        mute.setChecked(address.mute);
        sign.setChecked(address.sign);
        phone.setText(address.phone);
        description.setText(address.description);
        homeDescription.setText(address.homeDescription);
        familyDescription.setText(address.familyDescription);
        language.setText(getResources().getStringArray(R.array.languages)[CommonTools.getPositioninArray(this, R.array.languages_values, address.language)]);
        age.setText(getResources().getStringArray(R.array.ages)[CommonTools.getPositioninArray(this, R.array.ages_values, address.age)]);
//        visit.setChecked(address.type.contains("VISIT"));
//        course.setChecked(address.type.contains("COURSE"));
//        notAtHome.setChecked(address.type.contains("NOT_AT_HOME"));
//        type_phone.setChecked(address.type.contains("PHONE"));
        contactType.setSelection(CommonTools.getPositioninArray(this,R.array.contact_type_values,address.type));

        if (address.assignedPub != null){
            publisher.setText(address.assignedPub.name);
        }
        contactType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = getResources().getStringArray(R.array.contact_type_values)[contactType.getSelectedItemPosition()];
                if (!type.equals(address.type)) {
                    address.type = type;
                    address.save();
                    DbUpdate update = new DbUpdate();
                    update.publisherUuid = ((MyApplication) getApplication()).getMe().uuid;
                    update.uuid = address.uuid;
                    update.model = "ADDRESS";
                    update.updateType = "UPDATE";
                    update.save();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        course.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (studyIgnore){
//                    return;
//                }
//                new AlertDialog.Builder(ViewAddressActivity.this).setMessage(R.string.confirm_type_change).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        String type = "";
//                        if (notAtHome.isChecked()){
//                            type = "NOT_AT_HOME";
//                        }
//                        if (visit.isChecked()){
//                            if (type.length() > 0){
//                                type += "|";
//                            }
//                            type += "VISIT";
//                        }
//                        if (course.isChecked()){
//                            if (type.length() > 0){
//                                type += "|";
//                            }
//                            type += "BIBLE_COURSE";
//                        }
//                        if (type_phone.isChecked()){
//                            if (type.length() > 0){
//                                type += "|";
//                            }
//                            type += "PHONE";
//                        }
//                        address.type = type;
//                        address.save();
//                        DbUpdate update = new DbUpdate();
//                        update.publisherUuid = ((MyApplication)getApplication()).getMe().uuid;
//                        update.uuid = address.uuid;
//                        update.model = "ADDRESS";
//                        update.updateType = "UPDATE";
//                        update.save();
//
//                    }
//                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        studyIgnore = true;
//                        course.setChecked(!course.isChecked());
//                        studyIgnore = false;
//                    }
//                }).create().show();
//            }
//        });

        if("f".

            equals(address.gender))

            {
                gender.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icons8_user_female_skin_type_4_50));
            }
        else

            {
                gender.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icons8_user_male_skin_type_4_50));
            }
        this.addressText.setText(address.address);

        }


        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.view_address, menu);
            this.menu = menu;
            if (address == null || !((MyApplication)getApplicationContext()).getMe().type.equals("ADMINISTRATOR") && (!"1".equals(settings.getString("allow_modify","0")) && !"DRAFT".equals(address.status) && !"VALIDATE".equals(address.status))) {
                menu.removeItem(R.id.action_edit);
            }
            if (address != null && address.myLocalDir) {
                menu.findItem(R.id.action_add_to_my_dir).setTitle(R.string.remove_from_my_dir);
            }
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            Intent intent;
            if (id == R.id.action_map) {
                long[] ids = new long[1];
                ids[0] = address_id;
                Intent map = new Intent(this, MapsActivity.class);
                map.putExtra("ids", ids);
                this.startActivity(map);
                return true;
            } else if (id == R.id.action_edit) {
                intent = new Intent(ViewAddressActivity.this, NewAddressActivity.class);
                intent.putExtra("address_id", address_id);
                ViewAddressActivity.this.startActivity(intent);
                return true;
            } else if (id == R.id.action_navigate) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + address.lat + "," + address.lng);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.need_google_maps, Toast.LENGTH_SHORT).show();
                }
                return true;

            } else if (id == R.id.action_add_to_my_dir) {
                address.myLocalDir = !address.myLocalDir;
                address.save();
                menu.findItem(R.id.action_add_to_my_dir).setTitle(address.myLocalDir ? R.string.remove_from_my_dir : R.string.action_add_to_my_dir);
                Toast.makeText(this, address.myLocalDir ? R.string.added_to_my_dir : R.string.removed_from_my_dir, Toast.LENGTH_SHORT).show();
                ;
                return true;

            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        protected void onResume () {
            super.onResume();
            mVisitAdapter.setVisit(address.getVisits());
        }

        @Override
        public boolean onSupportNavigateUp () {
            onBackPressed();
            return true;
        }
    }
