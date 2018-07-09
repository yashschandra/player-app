package com.example.aquaman.player_droid;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        mUtils = new Utils(mContext);
        final Button connectButton = findViewById(R.id.connect_button);
        final EditText serverIp = findViewById(R.id.server_ip);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkRequest(serverIp.getText().toString());
            }
        });

    }

    private void checkRequest(final String serverIp) {

        String url = "http://" + serverIp + ":" + mUtils.SERVER_PORT + "/song/allsongs/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Intent intent = new Intent(mContext, HomeActivity.class);
                intent.putExtra("server_ip", serverIp);
                intent.putExtra("new_ip", true);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyNetwork.getInstance(mContext).addtoRequestQueue(stringRequest);
    }


}