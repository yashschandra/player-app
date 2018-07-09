package com.example.aquaman.player_droid;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by aquaman on 24/6/18.
 */

public class VolleyNetwork {

    private static VolleyNetwork mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private VolleyNetwork(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyNetwork getInstance(Context context) {

        if(mInstance == null) {
            mInstance = new VolleyNetwork(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        return mRequestQueue;
    }

    public<T> void addtoRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
