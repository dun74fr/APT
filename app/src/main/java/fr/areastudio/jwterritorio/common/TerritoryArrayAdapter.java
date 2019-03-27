package fr.areastudio.jwterritorio.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Territory;

public class TerritoryArrayAdapter extends ArrayAdapter<Territory> {

    private Context mContext;
    private List<Territory> territoryList;

    public TerritoryArrayAdapter( Context context, List<Territory> list) {
        super(context, 0 , list);
        mContext = context;
        territoryList = list;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.territorylist_item,parent,false);

        Territory currentTerritory = territoryList.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.territory_name);
        name.setText(currentTerritory.name);
        return listItem;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.territorylist_item,parent,false);

        Territory currentTerritory = territoryList.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.territory_name);
        name.setText(currentTerritory.name);

        return listItem;
    }
}
