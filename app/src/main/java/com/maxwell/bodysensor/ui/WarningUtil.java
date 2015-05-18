package com.maxwell.bodysensor.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.dialogfragment.dialog.DlgMessageOK;

public class WarningUtil {

	public static void showToastLong(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
	}
	public static void showToastLong(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	public static void showToastShort(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}
	public static void showToastShort(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showDFMessageOK(FragmentActivity activity, int titleId, int contentId) {
		DlgMessageOK dlg = new DlgMessageOK();
		if (titleId!=0) {
		    dlg.setTitle(activity.getString(titleId));
		}
		if (contentId!=0) {
		    dlg.addDesString(activity.getString(contentId));
		}

		dlg.showHelper(activity);
	}

	public static Dialog getDialogOK(FragmentActivity activity, int titleId,
                                     int contentId, int btnTextId, View.OnClickListener listener) {
		return getDialogOK(activity, activity.getString(titleId),
                activity.getString(contentId), btnTextId, listener);
	}

    public static Dialog getDialogOK(FragmentActivity activity, String title,
                                     String content, int btnTextId, View.OnClickListener listener) {
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_message_ok);

        TextView txtDlgTitle = (TextView) dialog.findViewById(R.id.ynTitle);
        txtDlgTitle.setText(title);

        TextView txtDlgDescription = (TextView) dialog.findViewById(R.id.ynDescription);
        txtDlgDescription.setText(content);

        Button btnStopAlert = (Button) dialog.findViewById(R.id.btnOK);
        btnStopAlert.setText(btnTextId);
        btnStopAlert.setOnClickListener(listener);

        return dialog;
    }
}
