package com.example.aquaman.player_droid;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by aquaman on 1/7/18.
 */

public class Utils {

    public static final String SERVER_PORT = "8003";

    public static final String SOCKET_PORT = "8008";

    public static final String SHARED_PREFS = "player_prefs";

    private Context mContext;

    public Utils(Context context) {
        mContext = context;
    }

    public void sendSimpleRequest(String url, JSONObject jsonObject) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyNetwork.getInstance(mContext).addtoRequestQueue(jsonObjectRequest);

    }

    public void saveSharedPrefs(String s, String s1) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(s, s1);
        editor.commit();
    }

    public String getSharedPrefs(String s) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(s, "");
    }

}
