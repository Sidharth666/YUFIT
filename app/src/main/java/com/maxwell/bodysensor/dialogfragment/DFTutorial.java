package com.maxwell.bodysensor.dialogfragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxwell.bodysensor.MainActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.fragment.FTutorialPage;
import com.maxwell.bodysensor.ui.ViewDotProgress;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/2/11.
 */
public class DFTutorial extends DFBase {

    private final int SIZE_TUTORIAL = 6;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ViewDotProgress mDotProgress;

    @Override
    public String getDialogTag() {
        return MainActivity.DF_TUTORIAL;
    }

    @Override
    public int getDialogTheme() {
        return R.style.app_df_trans_rr;
    }

    @Override
    public void saveData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        UtilDBG.logMethod();

        View view = inflater.inflate(R.layout.df_tutorial, container);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // initial ViewPager
        mViewPager = (ViewPager) view.findViewById(R.id.pager_tutorial);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int podsition) {
            }

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                mDotProgress.setProgress(position);
            }

        });

        mDotProgress = (ViewDotProgress) view.findViewById(R.id.progress_tutorial);
        mDotProgress.setSize(SIZE_TUTORIAL);
        mDotProgress.setProgress(0);

        setupTitleText(view, R.string.fcTutorial);
        setupButtons(view);
        hideButtonOK();

        return view;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            // switch fragment
            switch (position) {
                case 0:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_01);
                    break;
                case 1:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_05);
                    break;
                case 2:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_02);
                    break;
                case 3:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_03);
                    break;
                case 4:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_04);
                    break;
                case 5:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_08);
                    break;
                /*case 5:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_06);
                    break;
                case 6:
                    fragment = FTutorialPage.newInstance(R.layout.fragment_tutorial_07);
                    break;*/
            }

            return fragment;

        }

        @Override
        public int getCount() {
            // get number of page
            return SIZE_TUTORIAL;
        }
    }
}
