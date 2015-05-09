package com.maxwell.bodysensor.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.List;

/**
 * Created by ryanhsueh on 15/2/10.
 */
public class UtilConst {

    public static final int INVALID_INT = -1;
    public static final String NULL_STRING = null;
    public static final String EMPTY_STRING = "";
    public static final int TRUE_INT = 1;
    public static final int FALSE_INT = 0;
    public static final String HME_DEBUG_PACKAGE_NAME = "com.healthifyme.basic.debug";
    public static final String HME_MAIN_PACKAGE_NAME = "com.healthifyme.basic";
    public static final String HME_PACKAGE_NAME = HME_DEBUG_PACKAGE_NAME;

    public static boolean isHMPackageInstalled(Context context){
        //check for HM package
        String name =UtilConst.HME_PACKAGE_NAME;
        if(isPackageInstalled(name, context)){
            return true;

        }else {
            return false;
        }
    }

    public static boolean isPackageInstalled(String name, Context context){
        final PackageManager pm = context.getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(name)){
                return true;
            }
        }
        return false;
    }

}
