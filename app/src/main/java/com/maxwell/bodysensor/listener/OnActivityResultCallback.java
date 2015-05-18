package com.maxwell.bodysensor.listener;

import android.content.Intent;

/**
 * Created by ryanhsueh on 15/4/13.
 */
public interface OnActivityResultCallback {
    void onResult(int requestCode, int resultCode, Intent data);
}
