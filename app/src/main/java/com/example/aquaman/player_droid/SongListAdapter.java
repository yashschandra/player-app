package com.example.aquaman.player_droid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by aquaman on 1/7/18.
 */

public class SongListAdapter extends ArrayAdapter<SongModel> {

    private ArrayList<SongModel> mSongs;
    private Context mContext;

    public static class ViewHolder {
        TextView songName;
    }

    public SongListAdapter(ArrayList<SongModel> songs, Context context) {
        super(context, R.layout.song_list_view, songs);
        this.mSongs = songs;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SongModel songModel = getItem(position);
        final View result;
        ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.song_list_view, parent, false);
            viewHolder.songName = (TextView) convertView.findViewById(R.id.song_name);
            result = convertView;
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.songName.setText(songModel.getSongName());

        return result;

    }
}
