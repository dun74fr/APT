package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Medic;
import fr.areastudio.jwterritorio.model.Medic;
import fr.areastudio.jwterritorio.model.Visit;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder>{


    public static final String TAG = "AssignMedicAdapter";
    private final SharedPreferences settings;
    private final Context context;
    private List<Medic> values;
    RecyclerView mRecyclerView;
    private MedicListener listener;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public EmergencyAdapter(Activity activity, List<Medic> medics) {
        this.context = activity;
        values = medics;
       settings = context.getSharedPreferences(
                MainActivity.PREFS, 0);


    }

    public List<Medic> getMedics(){
        return this.values;
    }
    public void setMedics(List<Medic> medics) {
        this.values = medics;
        notifyDataSetChanged();
    }

    public void setListener(MedicListener listener){
        this.listener = listener;
    }

    public interface MedicListener{
        void onClick(Medic medic);
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId : " + values.get(position).getId());
        return values.get(position).getId();
    }



    // Create new views (invoked by the layout manager)
    @Override
    public EmergencyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_medic, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Medic medic = values.get(position);
        holder.name.setText(medic.name);
        holder.address.setText(medic.address);
        if (medic.category != null) {
            holder.category.setText(medic.category.name);
        }
        holder.icon.setVisibility(View.GONE);
//        if (medic.image != null){
//            byte[] decodedString = Base64.decode(medic.image, Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            holder.icon.setImageBitmap(decodedByte);
//            holder.icon.setVisibility(View.VISIBLE);
//
//        }
//        else {
            if (medic.category != null) {
                if (medic.category.uuid.equals("1") || medic.category.uuid.equals("9")|| medic.category.uuid.equals("10")) {
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_hospital_3_48));
                    holder.icon.setVisibility(View.VISIBLE);
                } else if (medic.category.uuid.equals("2")) {
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_tooth_48));
                    holder.icon.setVisibility(View.VISIBLE);
                } else if (medic.category.uuid.equals("3")) {
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_microscope_48));
                    holder.icon.setVisibility(View.VISIBLE);
                } else if (medic.category.uuid.equals("4")) {
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_pill_48));
                    holder.icon.setVisibility(View.VISIBLE);
                }
                else if (medic.category.uuid.equals("5")) {
                    holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_ambulance_48));
                    holder.icon.setVisibility(View.VISIBLE);
                }
            }
//        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(medic);
            }
        });

    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView address;
        private final TextView category;
        private final ImageView icon;

        public ViewHolder(final View view) {
            super(view);

            name = view.findViewById(R.id.name);
            address = view.findViewById(R.id.address);
            category = view.findViewById(R.id.category);
            icon = view.findViewById(R.id.icon_medic);

        }


    }

}
