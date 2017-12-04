package work.nityc_nyuta.sirasunakondate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class SettingListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    ArrayList<SettingList> settingLists;

    public SettingListAdapter(Context context){
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSettingLists(ArrayList<SettingList> settingLists){
        this.settingLists = settingLists;
    }

    @Override
    public int getCount() {
        return settingLists.size();
    }

    @Override
    public Object getItem(int position) {
        return settingLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return settingLists.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.setting_list,parent,false);
        ((ImageView)convertView.findViewById(R.id.setting_icon)).setImageResource(settingLists.get(position).getImage_id());
        ((TextView)convertView.findViewById(R.id.Setting)).setText(settingLists.get(position).getSetting());
        ((TextView)convertView.findViewById(R.id.Value)).setText(settingLists.get(position).getValue());
        return convertView;
    }
}
