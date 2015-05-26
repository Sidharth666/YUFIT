package com.maxwell.bodysensor.fragment.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxwell.bodysensor.GroupActivity;
import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.util.UtilDBG;

/**
 * Created by ryanhsueh on 15/3/16.
 */
public class FContainerGroup extends Fragment {

    private FragmentTabHost mTabHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UtilDBG.logMethod();

        View view = inflater.inflate(R.layout.container_main, container, false);

        // tab host, tab pages (fragments)
        mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        mTabHost.getTabWidget().setDividerDrawable(null);
        // tabs

        return view;
    }

    private void addTabHelper(Class<?> ccls, String name, String label, Integer iconId) {
        // Intent intent = new Intent().setClass(this, ccls);

        View tab = LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
        ImageView image = (ImageView) tab.findViewById(R.id.tabIcon);
        TextView text = (TextView) tab.findViewById(R.id.tabText);
        if(iconId != null){
            image.setImageResource(iconId);
        }
        text.setText(label);

        mTabHost.addTab(mTabHost.newTabSpec(name).setIndicator(tab), ccls, null );
    }

    public void setCurrentTab(String tabTag) {
        if (tabTag==null) {
            return ;
        }

        if (tabTag.compareToIgnoreCase(GroupActivity.TAB_SPEC_GROUP_STATS)==0 ||
                tabTag.compareToIgnoreCase(GroupActivity.TAB_SPEC_GROUPS)==0 ) {
            mTabHost.setCurrentTabByTag(tabTag);
        }
    }

}
