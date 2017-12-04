package work.nityc_nyuta.sirasunakondate;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("設定");

        showList();
    }

    public void showList(){

        //更新時間
        String update_time = "";
        if("!!!error!!!".equals(readData("UpdateTime_hh")) || "!!!error!!!".equals(readData("UpdateTime_mm"))){
            writeData("UpdateTime_hh","23");
            writeData("UpdateTime_mm","59");
        }
        int update_time_hh = Integer.valueOf(readData("UpdateTime_hh"));
        int update_time_mm = Integer.valueOf(readData("UpdateTime_mm"));
        update_time = String.format("%02d",update_time_hh) + ":" + String.format("%02d",update_time_mm) + "から";
        //ランダム献立
        if("!!!error!!!".equals(readData("isRandom"))){
            writeData("isRandom","OFF");
        }
        String isRandom_str = "";
        isRandom_str = readData("isRandom");

        //設定一覧
        String settings[] = {"翌日の献立を表示","ランダム献立"};
        int icons[] = {R.drawable.ic_access_time_black_24dp,R.drawable.ic_shuffle_black_24dp};
        String values[] = {update_time,isRandom_str};

        ListView setting_list_view = (ListView)findViewById(R.id.setting_list_view);
        ArrayList<SettingList> list = new ArrayList<>();
        SettingListAdapter adapter = new SettingListAdapter(this);
        adapter.setSettingLists(list);
        setting_list_view.setAdapter(adapter);
        final String finalIsRandom_str = isRandom_str;
        setting_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0: //更新時間
                        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                writeData("UpdateTime_hh",String.valueOf(hourOfDay));
                                writeData("UpdateTime_mm",String.valueOf(minute));
                                showList();
                            }
                        };
                        TimePickerDialog dialog = new TimePickerDialog(SettingActivity.this,
                                                android.R.style.Theme_DeviceDefault_Light_Dialog,onTimeSetListener,
                                                Integer.valueOf(readData("UpdateTime_hh")),Integer.valueOf(readData("UpdateTime_mm")),true);
                        dialog.show();
                        break;
                    case 1: //ランダム献立
                        if("ON".equals(finalIsRandom_str)){
                            writeData("isRandom","OFF");
                        }else{
                            writeData("isRandom","ON");
                        }
                        showList();
                        break;
                }
            }
        });


        for(int i = 0; i < settings.length; i++){
            SettingList settingList = new SettingList();
            settingList.setImage_id(icons[i]);
            settingList.setSetting(settings[i]);
            settingList.setValue(values[i]);
            list.add(settingList);
        }
        adapter.notifyDataSetChanged();
    }

    public void writeData(String key, String data){
        SharedPreferences sp = getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public String readData(String key){
        SharedPreferences sp = getSharedPreferences("Settings",MODE_PRIVATE);
        return sp.getString(key, "!!!error!!!");
    }
}
