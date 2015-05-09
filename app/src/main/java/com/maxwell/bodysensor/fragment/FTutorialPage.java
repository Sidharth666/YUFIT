package com.maxwell.bodysensor.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ryanhsueh on 15/2/11.
 */
public class FTutorialPage extends Fragment {

    private static final String RES_ID = "res_id";

    private int mResId;

    public static FTutorialPage newInstance(int resId) {
        FTutorialPage f = new FTutorialPage();

        Bundle b = new Bundle();
        b.putInt(RES_ID, resId);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        mResId = b.getInt(RES_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(mResId, container, false);
    }
}
