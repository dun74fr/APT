package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.News;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>{


    public static final String TAG = "AssignMedicAdapter";
    private final SharedPreferences settings;
    private final Context context;
    private List<News> values;
    RecyclerView mRecyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    public NewsAdapter(Activity activity, List<News> news) {
        this.context = activity;
        values = news;
        settings = context.getSharedPreferences(
                MainActivity.PREFS, 0);
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId : " + values.get(position).getId());
        return values.get(position).getId();
    }



    // Create new views (invoked by the layout manager)
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_news, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final News news = values.get(position);
        holder.readButton.setVisibility(View.VISIBLE);
        ((CardView)(holder.itemView)).setCardBackgroundColor(context.getResources().getColor(R.color.red));

        if(news.read){
            holder.readButton.setVisibility(View.GONE);
            ((CardView)(holder.itemView)).setCardBackgroundColor(context.getResources().getColor(R.color.secondaryTextColor));
        }
        holder.title.setText(news.title);
        holder.content.setText(Html.fromHtml(news.content));
        holder.date.setText(new SimpleDateFormat("dd-MM-yyyy").format(news.date));
        holder.readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                news.read = true;
                news.save();
                notifyDataSetChanged();
            }
        });

    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView date;
        private final TextView content;
        private final Button readButton;

        public ViewHolder(final View view) {
            super(view);

            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.date);
            content = view.findViewById(R.id.content);
            readButton = view.findViewById(R.id.readButton);

        }


    }

}
