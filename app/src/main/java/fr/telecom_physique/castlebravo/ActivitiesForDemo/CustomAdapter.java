package fr.telecom_physique.castlebravo.ActivitiesForDemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fr.telecom_physique.castlebravo.R;

/**
 * Created by Guillaumee on 26/03/2016.
 */
public class CustomAdapter extends BaseAdapter {

    private ArrayList<String> _listItems;
    private LayoutInflater _layoutInflater;

    public CustomAdapter(Context context, ArrayList<String> arrayList){

        _listItems = arrayList;
        //get the layout inflater
        _layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _listItems.size();
    }

    @Override
    public String getItem(int position) {
        return _listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = _layoutInflater.inflate(R.layout.content_activity_demo_list_view, null);

        }

        String stringItem = _listItems.get(position) ;

        if (stringItem != null) {

            TextView itemName = (TextView) view.findViewById(R.id.list_item_text_view);

            if (itemName != null) {
                //set the item name on the TextView
                itemName.setText(stringItem);
            }
        }

        return view;
    }

}