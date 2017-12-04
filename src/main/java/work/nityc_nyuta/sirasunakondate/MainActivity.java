package work.nityc_nyuta.sirasunakondate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.multidex.MultiDex;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity{

    private RequestQueue mQueue;
    String Date = "";
    ArrayList<String> Breakfast = new ArrayList<>();
    ArrayList<String> Lunch = new ArrayList<>();
    ArrayList<String> Dinner = new ArrayList<>();
    boolean isConnecting = false;
    boolean isWriting = false;
    int ErrorCode = 0;
    ProgressDialog progressDialog;
    Calendar calendar = Calendar.getInstance();
    int date[] = {calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)};
    int old_date[] = {calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)};

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { //アプリ起動時
        super.onCreate(savedInstanceState);
        if("!!!error!!!".equals(readData("UpdateTime_hh")) || "!!!error!!!".equals(readData("UpdateTime_mm"))){
            writeData("UpdateTime_hh","23");
            writeData("UpdateTime_mm","59");
        }
        if("!!!error!!!".equals(readData("isRandom"))){
            writeData("isRandom","OFF");
        }
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        FirebaseMessaging.getInstance().subscribeToTopic("2017shosendemo");
        if(!AppLaunchChecker.hasStartedFromLauncher(this)){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("ようこそ");
            alertDialogBuilder.setMessage(
                            "白砂寮の献立を取得し、表示することの出来るアプリです。\n\n" +
                            "必要以上のリロードなどサーバに負荷をかける行為はやめて下さい。\n\n" +
                            "白砂寮のHPから情報を取得して表示しています。白砂寮HPが更新されない場合に、最新の情報が取得できない可能性があります。\n\n" +
                            "このアプリはインターネット接続のみを行い、その他権限などは必要としません。\n\n" +
                            "(このメッセージは初回起動時のみ表示されます)"
            );
            alertDialogBuilder.setPositiveButton("分かりました",null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setup();
    }

    public void setup(){
        Calendar calendar = Calendar.getInstance();
        AppLaunchChecker.onActivityCreate(this);
        int now_time = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);
        int set_time = Integer.valueOf(readData("UpdateTime_hh")) * 100 + Integer.valueOf(readData("UpdateTime_mm"));
        if(isNetworkConnect()){
            //更新時間を過ぎているか
            if(now_time >= set_time){
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                date[0] = calendar.get(Calendar.YEAR);
                date[1] = calendar.get(Calendar.MONTH)+1;
                date[2] = calendar.get(Calendar.DAY_OF_MONTH);
            }
            getMenu(date[0],date[1],date[2]);
        }
        TextView title = (TextView)findViewById(R.id.title);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"font.ttf");
        title.setTypeface(typeface);
        title.setText(getString(R.string.title,"-", "-", "-"));
        setTitle(R.string.title_name);
    }

    public boolean isNetworkConnect(){ //ネットワークに接続されているかをboolean型で返す
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return true;
        }else{
            Toast.makeText(MainActivity.this, "ネットワークに接続して下さい", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void Menu_View(){ //献立を表示する
        isWriting = true;
        String breakfast_str = "", lunch_str = "", dinner_str = "";
        String[] month_day;
        for(int idx = 0; idx < Breakfast.size(); idx++){
            if(!"".equals(Breakfast.get(idx))) {
                breakfast_str += Breakfast.get(idx) + "\n";
            }
        }
        for(int idx = 0; idx < Lunch.size(); idx++){
            if(!"".equals(Lunch.get(idx))) {
                lunch_str += Lunch.get(idx) + "\n";
            }
        }
        for(int idx = 0; idx < Dinner.size(); idx++){
            if(!"".equals(Dinner.get(idx))) {
                dinner_str += Dinner.get(idx) + "\n";
            }
        }
        month_day = Date.split("/",0);
        TextView title = (TextView)findViewById(R.id.title);
        TextView breakfast_view = (TextView)findViewById(R.id.breakfast_view);
        TextView lunch_view = (TextView)findViewById(R.id.lunch_view);
        TextView dinner_view = (TextView)findViewById(R.id.dinner_view);
        breakfast_view.setText(breakfast_str);
        lunch_view.setText(lunch_str);
        dinner_view.setText(dinner_str);
        Calendar calendar_add = Calendar.getInstance();
        calendar_add.add(Calendar.DAY_OF_MONTH, 1);

        //ランダム献立がONならタイトル設定処理を飛ばす
        if("xx".equals(month_day[1])){
            title.setText(R.string.title_random);
            isConnecting = false;
            isWriting = false;
            setTitle("白砂寮献立");
            return;
        }
        //ランダム献立がオフの場合
        int month = Integer.parseInt(month_day[1]), day = Integer.parseInt(month_day[2]);
        if(month == calendar.get(Calendar.MONTH)+1 && day == calendar.get(Calendar.DAY_OF_MONTH)){
            title.setText(R.string.title_today);
        }else if(month == calendar_add.get(Calendar.MONTH)+1 && day == calendar_add.get(Calendar.DAY_OF_MONTH)){
            title.setText(R.string.title_tomorrow);
        }else {
            title.setText(getString(R.string.title, month_day[0], month_day[1], month_day[2]));
        }
        setTitle("白砂寮献立");
        isConnecting = false;
        isWriting = false;
        return;
    }

    public void getMenu(int year, int month, int day){ //APIに接続し献立を取得する
        isConnecting = true;
        setTitle("白砂寮献立 Loading...");
        Breakfast.clear();
        Lunch.clear();
        Dinner.clear();

        String API_URL = "http://nityc-nyuta.work/sirasuna_kondateAPI_prototype/all?year=" + String.valueOf(year) +
                "&month=" + String.valueOf(month) + "&day=" + String.valueOf(day);

        //ランダム献立かどうか
        if("ON".equals(readData("isRandom"))){
            API_URL = "http://nityc-nyuta.work/sirasuna_kondateAPI_prototype/random";
        }

        //データ受信
        mQueue = Volley.newRequestQueue(this);
        mQueue.add(new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getInt("code") == 0) {
                                Date = response.getString("date");
                                JSONObject menu = response.getJSONObject("menu");
                                JSONArray breakfast = menu.getJSONArray("breakfast");
                                JSONArray lunch = menu.getJSONArray("lunch");
                                JSONArray dinner = menu.getJSONArray("dinner");
                                for (int i = 0; i < breakfast.length(); i++) {
                                    Breakfast.add(breakfast.getString(i));
                                }
                                for (int i = 0; i < lunch.length(); i++) {
                                    Lunch.add(lunch.getString(i));
                                }
                                for (int i = 0; i < dinner.length(); i++) {
                                    Dinner.add(dinner.getString(i));
                                }
                                Menu_View();
                            }else {
                                //progressDialog.dismiss();
                                isConnecting = false;
                                setTitle("白砂寮献立");
                                date[0] = old_date[0];
                                date[1] = old_date[1];
                                date[2] = old_date[2];
                                Toast.makeText(MainActivity.this, response.getString("error_message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            //progressDialog.dismiss();
                            isConnecting = false;
                            setTitle("白砂寮献立");
                            date[0] = old_date[0];
                            date[1] = old_date[1];
                            date[2] = old_date[2];
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse == null){
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "HTTP Error", Toast.LENGTH_LONG).show();
                            return;
                        }
                        ErrorCode = error.networkResponse.statusCode;
                        date[0] = old_date[0];
                        date[1] = old_date[1];
                        date[2] = old_date[2];
                        setTitle("白砂寮献立");
                        isConnecting = false;
                        Toast.makeText(MainActivity.this, "HTTP Error : " + String.valueOf(ErrorCode), Toast.LENGTH_LONG).show();
                    }
                }));
    }

    public String readData(String key){ //設定ファイル再読み込み
        SharedPreferences sp = getSharedPreferences("Settings",MODE_PRIVATE);
        return sp.getString(key, "!!!error!!!");
    }

    public void writeData(String key, String data){
        SharedPreferences sp = getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public void calendar_popup(){ //カレンダーポップアップ
        //日付情報引き継ぎ
        old_date[0] = date[0];
        old_date[1] = date[1];
        old_date[2] = date[2];
        LayoutInflater factory = LayoutInflater.from(this);
        View calendar_view = factory.inflate(R.layout.calendar, null);
        final CalendarView calendarView = (CalendarView)calendar_view.findViewById(R.id.calendarView);
        //カレンダーの日時セット
        Calendar set_calendar = Calendar.getInstance();
        set_calendar.set(Calendar.YEAR,date[0]);
        set_calendar.set(Calendar.MONTH,date[1]-1);
        set_calendar.set(Calendar.DAY_OF_MONTH,date[2]);
        calendarView.setDate(set_calendar.getTimeInMillis());
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                date[0] = year;
                date[1] = month + 1;
                date[2] = day;
            }
        });
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("日付選択");
        alertDialogBuilder.setView(calendar_view);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getMenu(date[0],date[1],date[2]);
            }});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //オプションメニュー登録
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //オプションメニューが選択されたら
        int id = item.getItemId();
        if(id == R.id.action_today && isNetworkConnect() && !isConnecting && !isWriting){
            Calendar calendar = Calendar.getInstance();
            int now_time = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);
            int set_time = Integer.valueOf(readData("UpdateTime_hh")) * 100 + Integer.valueOf(readData("UpdateTime_mm"));
            if(now_time >= set_time){
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                date[0] = calendar.get(Calendar.YEAR);
                date[1] = calendar.get(Calendar.MONTH)+1;
                date[2] = calendar.get(Calendar.DAY_OF_MONTH);
            }else{
                date[0] = calendar.get(Calendar.YEAR);
                date[1] = calendar.get(Calendar.MONTH)+1;
                date[2] = calendar.get(Calendar.DAY_OF_MONTH);
            }
            getMenu(date[0],date[1],date[2]);
        }
        if(id == R.id.action_search && isNetworkConnect() && !isConnecting && !isWriting){
            Intent intent = new Intent(getApplication(), SearchActivity.class);
            startActivity(intent);
        }
        if(id == R.id.action_reload && isNetworkConnect() && !isConnecting && !isWriting){
            Calendar calendar = Calendar.getInstance();
            if(calendar.get(Calendar.HOUR_OF_DAY) >= Integer.valueOf(readData("UpdateTime_hh")) && calendar.get(Calendar.MINUTE) >= Integer.valueOf(readData("UpdateTime_mm"))){
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                date[0] = calendar.get(Calendar.YEAR);
                date[1] = calendar.get(Calendar.MONTH)+1;
                date[2] = calendar.get(Calendar.DAY_OF_MONTH);
            }
            getMenu(date[0],date[1],date[2]);
        }
        if(id == R.id.action_calendar && isNetworkConnect() && !isConnecting && !isWriting){
            calendar_popup();
        }
        if(id == R.id.action_credit){
            Intent intent = new Intent(getApplication(), CreditActivity.class);
            startActivity(intent);
        }
        if(id == R.id.action_setting){
            Intent intent = new Intent(getApplication(), SettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
