package fr.areastudio.jwterritorio.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Territory;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class MyGroupAddressAdapter extends ExpandableRecyclerViewAdapter<MyGroupAddressAdapter.MapViewHolder, MyGroupAddressAdapter.ViewHolder> {


    public static final String TAG = "AssignAddressAdapter";
    private final Context context;
    private SharedPreferences settings;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    RecyclerView mRecyclerView;
    private MyAddressListener listener;


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public MyGroupAddressAdapter(Context context, List<? extends ExpandableGroup> mapList) {
        super(mapList);
        this.context = context;
        settings = context.getSharedPreferences(
                MainActivity.PREFS, 0);


    }

    public void setListener(MyAddressListener listener) {
        this.listener = listener;
    }

    public interface MyAddressListener {
        void onAddressClicked(Address address);

        void onRefresh();
    }


    @Override
    public MapViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_type, parent, false);
        return new MapViewHolder(view);
    }

    @Override
    public ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ViewHolder holder, int flatPosition, ExpandableGroup group,
                                      int childIndex) {
        final Address address = ((TypeGroup) group).getItems().get(childIndex);
        holder.name.setText(address.name);
        holder.address.setText(address.address);
        //Visit lastVisit = address.getLastVisit();
        holder.lastContactImg.setVisibility(View.GONE);
        holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.secondaryTextColor));
        holder.terr.setText(address.territory == null ? "" : address.territory.name);
        if ("DRAFT".equals(address.status)) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.disabled));
        } else if ("VALIDATE".equals(address.status)) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.validate));
        }
        if ("f".equals(address.gender)) {
            holder.gender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_user_female_skin_type_4_50));
        } else {
            holder.gender.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icons8_user_male_skin_type_4_50));
        }
        if (address.lastVisit != null) {
            holder.lastContact.setText(dateFormatter.format(address.lastVisit));
            holder.lastContactImg.setVisibility(View.VISIBLE);
        } else {
            holder.lastContact.setText("");
            holder.lastContactImg.setVisibility(View.GONE);
        }
        if (address.assignedPub != null) {
            holder.assigned.setText(address.assignedPub.name);
        } else {
            holder.assigned.setText("");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAddressClicked(address);
            }
        });
    }

    public List<Address> getAddresses() {
        List<Address> allAddresses = new ArrayList<>();
        for (ExpandableGroup<Address> g : this.getGroups()) {
            allAddresses.addAll(g.getItems());
        }
        return allAddresses;
    }

    @Override
    public void onBindGroupViewHolder(MapViewHolder holder, int flatPosition,
                                      ExpandableGroup mapGroup) {
        holder.setMapTitle(mapGroup);
        holder.map.setVisibility(View.INVISIBLE);
        holder.unasigned.setVisibility(View.INVISIBLE);
        final Territory territory = ((TypeGroup) mapGroup).getTerritory();
        if (territory != null && territory.image != null && territory.image.length() > 0) {
            holder.map.setVisibility(View.VISIBLE);
            holder.map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, FullScreenViewActivity.class);
                    i.putExtra("image", territory.image);
                    context.startActivity(i);
                }
            });
        }
        if (territory != null && territory.assignedPub != null) {
            holder.unasigned.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context).setMessage(R.string.confirm_unassign).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            territory.assignedPub = null;
                            territory.save();
                            DbUpdate up = new DbUpdate();
                            up.uuid = territory.uuid;
                            up.model = "TERRITORY";
                            up.date = new Date();
                            up.publisherUuid = ((MyApplication) context.getApplicationContext()).getMe().uuid;
                            up.updateType = "UPDATE";
                            up.save();
                            notifyDataSetChanged();
                            listener.onRefresh();
                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();

                }
            });
            holder.unasigned.setVisibility(View.VISIBLE);
        }

    }


    public class MapViewHolder extends GroupViewHolder {

        private final ImageView arrow;
        private TextView mapTitle;
        private final ImageView map;
        private final ImageView unasigned;

        public MapViewHolder(View itemView) {
            super(itemView);
            mapTitle = itemView.findViewById(R.id.list_item_map_name);
            arrow = itemView.findViewById(R.id.list_item_genre_arrow);
            map = itemView.findViewById(R.id.map);
            unasigned = itemView.findViewById(R.id.unasigned);
        }

        public void setMapTitle(ExpandableGroup group) {
            mapTitle.setText(group.getTitle());
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }
    }


    public class ViewHolder extends ChildViewHolder {
        private final TextView name;
        private final TextView terr;
        private final TextView address;
        private final TextView lastContact;
        private final TextView assigned;
        private final ImageView lastContactImg;
        private final ImageView gender;
        private final CardView cardView;

        public ViewHolder(final View view) {
            super(view);

            name = view.findViewById(R.id.name);
            terr = view.findViewById(R.id.terr);
            address = view.findViewById(R.id.address);
            lastContact = view.findViewById(R.id.lastContact);
            lastContactImg = view.findViewById(R.id.lastContactImg);
            assigned = view.findViewById(R.id.assigned);
            gender = view.findViewById(R.id.icon_gender);
            cardView = view.findViewById(R.id.card_view);

        }


    }

}
