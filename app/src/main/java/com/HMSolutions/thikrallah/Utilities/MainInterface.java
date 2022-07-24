package com.HMSolutions.thikrallah.Utilities;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

public interface MainInterface {

	void launchFragment(Fragment iFragment, Bundle args, String tag);

    void share();

    void playAll(String AssetFolder);

    void incrementCurrentPlaying(String AssetFolder, int i);

    void pausePlayer(String DataType);

    void play(String AssetFolder, int fileNumber);

    void play(String path);

    void play(Uri path);

    boolean isPlaying();

    void resetPlayer(String thikrtype);

    void setCurrentPlaying(String AssetFolder, int currentPlaying);

    int getCurrentPlaying();

    void setThikrType(String thikrType);

	void requestLocationUpdate();

	void requestMediaServiceStatus();

}
