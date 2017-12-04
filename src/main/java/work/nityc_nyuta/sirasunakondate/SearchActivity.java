package work.nityc_nyuta.sirasunakondate;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Button button = (Button)findViewById(R.id.search_button);
        final EditText editText = (EditText)findViewById(R.id.editText);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String API_URL = "http://nityc-nyuta.work/sirasuna_kondateAPI_prototype/search?menu="+ editText.getText().toString();
                try {
                    API_URL =  new URI(API_URL).toASCIIString();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                mQueue = Volley.newRequestQueue(SearchActivity.this);
                final String finalAPI_URL = API_URL;
                mQueue.add(new JsonObjectRequest(Request.Method.GET, API_URL, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if(response.getInt("code") == 0) {
                                        String date = response.getString("date");
                                        String type = response.getString("type");
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SearchActivity.this);
                                        alertDialogBuilder.setTitle("検索結果");
                                        alertDialogBuilder.setMessage("次に[ " + editText.getText().toString() + " ]が出るのは" + date + "の" + type + "です");
                                        alertDialogBuilder.setPositiveButton("分かりました",null);
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                        Log.d("Search",date + " " + type);
                                    }else {
                                        Toast.makeText(SearchActivity.this, editText.getText().toString() + "は見つかりませんでした", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(SearchActivity.this, "エラーが発生しました", Toast.LENGTH_LONG).show();
                                }
                            }
                        },

                        new Response.ErrorListener() {
                            @Override public void onErrorResponse(VolleyError error) {
                                if(error.networkResponse == null){
                                    Toast.makeText(SearchActivity.this, "HTTP Error", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                int ErrorCode = error.networkResponse.statusCode;
                                Toast.makeText(SearchActivity.this, "HTTP Error : " + String.valueOf(ErrorCode), Toast.LENGTH_LONG).show();
                            }
                        }));
            }
        });
    }
}
