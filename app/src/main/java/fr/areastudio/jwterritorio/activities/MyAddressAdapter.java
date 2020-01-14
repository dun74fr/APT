package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;

public class MyAddressAdapter extends RecyclerView.Adapter<MyAddressAdapter.ViewHolder>{


    public static final String TAG = "AssignAddressAdapter";
    private final SharedPreferences settings;
    private final Context context;
    private List<Address> values;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private ArrayList<Address> selectedToAssign = new ArrayList<>();
    RecyclerView mRecyclerView;
    private MyAddressListener listener;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public MyAddressAdapter(Activity activity, List<Address> addresses) {
        this.context = activity;
        values = addresses;
       settings = context.getSharedPreferences(
                MainActivity.PREFS, 0);


    }

    public List<Address> getAddresses(){
        return this.values;
    }
    public void setAddresses(List<Address> addresses) {
        this.values = addresses;
        notifyDataSetChanged();
    }

    public void setListener(MyAddressListener listener){
        this.listener = listener;
    }

    public interface MyAddressListener{
        void onClick(Address address);
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId : " + values.get(position).getId());
        return values.get(position).getId();
    }



    // Create new views (invoked by the layout manager)
    @Override
    public MyAddressAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_myaddress, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Address address = values.get(position);
//        if (address.optIn) {
            holder.name.setText(address.name);
//        }
//        else {
//            holder.name.setText(R.string.private_data);
//        }
        holder.address.setText(address.address);
        //Visit lastVisit = address.lastVisit;
        holder.lastContactImg.setVisibility(View.GONE);
        holder.lastContact.setText("");
        holder.assigned.setText("");
        holder.terr.setText(address.territory == null ? "" : address.territory.name);
        holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.secondaryTextColor));

        if ("DRAFT".equals(address.status)){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.disabled));
        }
        else if ("VALIDATE".equals(address.status)){
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.validate));
        }
        if ("f".equals(address.gender)) {
            holder.gender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_user_female_skin_type_4_50));
        }
        else {
            holder.gender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_user_male_skin_type_4_50));
        }
        if (address.lastVisit != null) {
            holder.lastContact.setText(dateFormatter.format(address.lastVisit));
            holder.lastContactImg.setVisibility(View.VISIBLE);
        }
        if (address.assignedPub != null) {
            holder.assigned.setText(address.assignedPub.name);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(address);
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
        private final TextView lastContact;
        private final TextView assigned;
        private final TextView terr;
        private final ImageView lastContactImg;
        private final ImageView gender;
        private final CardView cardView;

        public ViewHolder(final View view) {
            super(view);

            name = view.findViewById(R.id.name);
            address = view.findViewById(R.id.address);
            lastContact = view.findViewById(R.id.lastContact);
            lastContactImg = view.findViewById(R.id.lastContactImg);
            assigned = view.findViewById(R.id.assigned);
            gender = view.findViewById(R.id.icon_gender);
            cardView = view.findViewById(R.id.card_view);
            terr = view.findViewById(R.id.terr);
        }


    }

}
