package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Visit;

public class VisitsAdapter extends RecyclerView.Adapter<VisitsAdapter.ViewHolder> {


    public static final String TAG = "VisitAdapter";
    private final SharedPreferences settings;
    private final Context context;
    private List<Visit> values;
    RecyclerView mRecyclerView;
    private VisitListener listener;
    private SimpleDateFormat dateformater;
    private SimpleDateFormat timeformater;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public VisitsAdapter(Activity activity, List<Visit> visits) {
        this.context = activity;
        dateformater = new SimpleDateFormat("dd-MMM-yyyy");
        timeformater = new SimpleDateFormat("HH:mm");
        values = visits;
        settings = context.getSharedPreferences(
                MainActivity.PREFS, 0);


    }

    public List<Visit> getVisits() {
        return this.values;
    }

    public void setVisit(List<Visit> visits) {
        this.values = visits;
        notifyDataSetChanged();
    }

    public void setListener(VisitListener listener) {
        this.listener = listener;
    }

    public interface VisitListener {
        void onClick(Visit visit);
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId : " + values.get(position).getId());
        return values.get(position).getId();
    }


    // Create new views (invoked by the layout manager)
    @Override
    public VisitsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_visit, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Visit visit = values.get(position);
        holder.date.setText(visit.date == null? "" : dateformater.format(visit.date));
        holder.time.setText(visit.date == null? "" : timeformater.format(visit.date));
        holder.publisher.setText(visit.publisher == null ? "" : visit.publisher.name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(visit);
            }
        });

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView publisher;
        private final TextView date;
        private final TextView time;



        public ViewHolder(final View view) {
            super(view);

            publisher = view.findViewById(R.id.publisher);
            date = view.findViewById(R.id.date);
            time = view.findViewById(R.id.time);

        }


    }

}
