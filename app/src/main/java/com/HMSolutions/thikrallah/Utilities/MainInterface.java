package com.HMSolutions.thikrallah.Utilities;

import android.app.Fragment;
import android.os.Bundle;

public interface MainInterface {

		void launchFragment(Fragment iFragment,Bundle args);
		void upgrade();
		void playAll();
		void incrementCurrentPlaying(int i);
		void pausePlayer();
		void play(int fileNumber);
		boolean isPlaying();
		void resetPlayer();
		void setCurrentPlaying(int currentPlaying);
		int getCurrentPlaying();
		void setThikrType(String thikrType);
}
