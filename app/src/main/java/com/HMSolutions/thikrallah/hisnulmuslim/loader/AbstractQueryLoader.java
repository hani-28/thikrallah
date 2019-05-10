package com.HMSolutions.thikrallah.hisnulmuslim.loader;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

import com.HMSolutions.thikrallah.hisnulmuslim.database.ExternalDbOpenHelper;

public abstract class AbstractQueryLoader<T> extends AsyncTaskLoader<T> {
    protected T mData;
    protected final ExternalDbOpenHelper mDbHelper;

    public AbstractQueryLoader(Context context) {
        super(context);
        mDbHelper = ExternalDbOpenHelper.getInstance(context);
    }

    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            return;
        }

        mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mData = null;
    }
}
