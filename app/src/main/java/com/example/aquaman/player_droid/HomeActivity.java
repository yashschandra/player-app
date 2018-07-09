package com.example.aquaman.player_droid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by aquaman on 1/7/18.
 */

public class HomeActivity extends Activity {

    private String mServer;
    private String mSocketServer;
    private String mIp;
    private Context mContext;
    private ListView mSongList;
    private ListView mSearchResults;
    private SongListAdapter mSongListAdapter;
    private ArrayList<SongModel> mSongModels;
    private ArrayList<SongModel> mSearchSongModels;
    private OkHttpClient mClient;
    private Utils mUtils;
    private int mCurrentSongId;
    private EditText mSearchText;
    private TextView mUploadText;

    private final class SocketClientListener extends WebSocketListener {

        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("TAG", "message:" + text);
            try {
                JSONObject jsonObject = new JSONObject(text);
                String type = jsonObject.getString("type");
                String songName = jsonObject.getJSONArray("song").getJSONObject(0).getString("songName");
                int songId = jsonObject.getJSONArray("song").getJSONObject(0).getInt("songId");
                if(type.equals("play")) {
                    final String song = songName;
                    mCurrentSongId = songId;
                    Log.d("TAG", "song:" + song);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateSearchText(song);
                        }
                    });
                }
                else if(type.equals("add")) {
                    Log.d("TAG", "add:" + songName);
                    mSongModels.add(new SongModel(songName, songId));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSongListAdapter();
                        }
                    });
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d("TAG", "socket closed");
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable throwable, okhttp3.Response response) {
            Log.d("TAG", "error:" + throwable.getMessage());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final Button playButton = findViewById(R.id.play_button);
        final Button pauseButton = findViewById(R.id.pause_button);
        final Button stopButton = findViewById(R.id.stop_button);
        final Button volUpButton = findViewById(R.id.vol_up_button);
        final Button volDownButton = findViewById(R.id.vol_down_button);

        mUploadText = findViewById(R.id.upload_button);

        mContext = getApplicationContext();
        mUtils = new Utils(mContext);
        if(getIntent().getBooleanExtra("new_ip", false)) {
            mIp = getIntent().getStringExtra("server_ip");
            mUtils.saveSharedPrefs("server_ip", mIp);
            Log.d("TAG", "new true:"+ mIp);
        }
        else {
            mIp = mUtils.getSharedPrefs("server_ip");
            Log.d("TAG", "new false:"+mIp);
        }
        mServer = "http://" + mIp + ":" + mUtils.SERVER_PORT;
        mSocketServer = "ws://" + mIp + ":" + mUtils.SOCKET_PORT;
        mSongList = findViewById(R.id.song_list);
        mSearchText = findViewById(R.id.search);
        mSearchResults = findViewById(R.id.search_results);
        mClient = new OkHttpClient();

        connectToSocketServer();
        getHomePage();

        mUploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UploadActivity.class);
                intent.putExtra("server", mServer);
                startActivity(intent);
            }
        });

        mSearchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null && editable.length()>0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TAG", "focus text:"+ mSearchText.hasFocus());
                            if(mSearchText.hasFocus()) {
                                searchText();
                            }
                            else {
                                hideSearchResults();
                            }
                        }
                    }, 1000);

                }
            }
        });

        mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d("TAG", "focus listener:"+b);
                if(b) {
                    searchText();
                }
                else {
                    hideSearchResults();
                }
            }
        });


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong(mCurrentSongId);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseSong();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSong();
            }
        });

        volDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volDown();
            }
        });

        volUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volUp();
            }
        });


    }

    private void connectToSocketServer() {
        String uri = mSocketServer;
        Log.d("TAG", "uri:" + uri);
        okhttp3.Request request = new okhttp3.Request.Builder().url(uri).build();
        SocketClientListener listener = new SocketClientListener();
        WebSocket ws = mClient.newWebSocket(request, listener);
        mClient.dispatcher().executorService().shutdown();

    }

    private void getHomePage() {
        getSongData();
        getCurrentSong();
    }

    private void searchText() {

        String text = mSearchText.getText().toString();
        JSONObject jsonObject = new JSONObject();
        if(!text.isEmpty()) {
            try {
                jsonObject.put("search", text);
                String url = mServer + "/song/searchdata/";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "response:" + response.toString());
                        displaySearchResults(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                VolleyNetwork.getInstance(mContext).addtoRequestQueue(jsonObjectRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    private void displaySearchResults(JSONObject jsonObject) {

        String songName;
        int songId;
        JSONObject object;
        mSearchSongModels = new ArrayList<>();

        try {
            JSONArray arr = jsonObject.getJSONArray("data");
            for(int i = 0; i<arr.length(); i++) {
                //Toast.makeText(mContext, arr.get(i).toString(), Toast.LENGTH_LONG).show();
                object = arr.getJSONObject(i);
                songId = object.getInt("songId");
                songName = object.getString("songName");
                mSearchSongModels.add(new SongModel(songName, songId));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSongListAdapter = new SongListAdapter(mSearchSongModels, mContext);
        mSearchResults.setAdapter(mSongListAdapter);
        mSearchResults.setVisibility(View.VISIBLE);
        mSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongModel song = mSearchSongModels.get(i);
                mCurrentSongId = song.getSongId();
                updateSearchText(song.getSongName());
            }

        });

    }

    private void hideSearchResults() {
        mSearchText.clearFocus();
        mSearchResults.setVisibility(View.GONE);
        Log.d("TAG", "hide results");
    }

    private void getCurrentSong() {
        String url = mServer + "/song/current/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show();
                try {
                    String song = response.getJSONArray("song").getJSONObject(0).getString("songName");
                    mCurrentSongId = response.getJSONArray("song").getJSONObject(0).getInt("songId");
                    updateSearchText(song);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyNetwork.getInstance(mContext).addtoRequestQueue(jsonObjectRequest);
    }

    private void getSongData() {
        String url = mServer + "/song/allsongs/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show();
                displaySongs(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        VolleyNetwork.getInstance(mContext).addtoRequestQueue(jsonObjectRequest);
    }

    private void displaySongs(JSONObject response) {

        String songName;
        int songId;
        JSONObject object;
        mSongModels = new ArrayList<>();
        try {
            JSONArray arr = response.getJSONArray("songs");
            for(int i = 0; i<arr.length(); i++) {
                //Toast.makeText(mContext, arr.get(i).toString(), Toast.LENGTH_LONG).show();
                object = arr.getJSONObject(i);
                songId = object.getInt("songId");
                songName = object.getString("songName");
                mSongModels.add(new SongModel(songName, songId));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setSongListAdapter();

    }

    private void setSongListAdapter() {

        mSongListAdapter = new SongListAdapter(mSongModels, mContext);
        mSongList.setAdapter(mSongListAdapter);
        mSongList.setLongClickable(true);

        mSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SongModel song = mSongModels.get(i);
                playSong(song.getSongId());
            }

        });
    }

    private void playSong(int songId) {
        String url = mServer + "/song/play/";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("songId", songId);
            mUtils.sendSimpleRequest(url, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void pauseSong() {
        String url = mServer + "/song/pause/";
        mUtils.sendSimpleRequest(url, null);
    }

    private void stopSong() {
        String url = mServer + "/song/stop/";
        mUtils.sendSimpleRequest(url, null);
    }

    private void volUp() {
        String url = mServer + "/song/volumeup/";
        mUtils.sendSimpleRequest(url, null);
    }

    private void volDown() {
        String url = mServer + "/song/volumedown/";
        mUtils.sendSimpleRequest(url, null);
    }

    private void updateSearchText(String song) {
        mSearchText.setText(song);
        hideSearchResults();
    }

}
