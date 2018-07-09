package com.example.aquaman.player_droid;

/**
 * Created by aquaman on 1/7/18.
 */

public class SongModel {

    private String songName;
    private int songId;

    public SongModel(String songName, int songId) {
        this.songName = songName;
        this.songId = songId;
    }

    public String getSongName() {
        return this.songName;
    }

    public int getSongId() {
        return this.songId;
    }

}
