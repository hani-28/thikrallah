package com.HMSolutions.thikrallah.Utilities;

import android.app.Fragment;
import android.os.Bundle;

public interface MainInterface {

		void launchFragment(Fragment iFragment,Bundle args,String tag);
		void share();
        void upgrade();
		void playAll(String AssetFolder);
		void incrementCurrentPlaying(String AssetFolder,int i);
		void pausePlayer();
		void play(String AssetFolder,int fileNumber);
		boolean isPlaying();
		void resetPlayer();
		void setCurrentPlaying(String AssetFolder,int currentPlaying);
		int getCurrentPlaying();
		void setThikrType(String thikrType);
}
