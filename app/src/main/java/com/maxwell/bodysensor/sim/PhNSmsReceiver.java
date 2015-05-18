package com.maxwell.bodysensor.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

// import com.mxw.ble.BleWrapper;
import com.maxwell.bodysensor.util.UtilDBG;

// Ref. http://stackoverflow.com/questions/4637821/how-to-analyze-incoming-sms-on-android

public class PhNSmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        UtilDBG.logMethod();
        if (intent.getAction() == "android.provider.Telephony.SMS_RECEIVED") {
            UtilDBG.i(intent.getAction());
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                if (messages.length > -1) {
                    UtilDBG.i("Message recieved: [Address] " + messages[0].getOriginatingAddress() + " [Body] " + messages[0].getMessageBody());
                    // BleWrapper w = BleWrapper.getInstance();
                    // w.incomingSMS(messages[0].getOriginatingAddress());
                }
            }
        }
/*
      //this stops notifications to others
        this.abortBroadcast();

        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        String from = "";
        String msg = "";
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                str += "SMS from " + msgs[i].getOriginatingAddress();
                from = msgs[i].getOriginatingAddress();
                str += " :";
                str += msgs[i].getMessageBody().toString();
                msg = msgs[i].getMessageBody().toString();
                str += "\n";
            }

            GV.logD(str);

//            if(checksomething){
                //make your actions
                //and no alert notification and sms not in inbox
//            }
//            else{
                //continue the normal process of sms and will get alert and reaches inbox
                this.clearAbortBroadcast();
//            }
        }
*/
    }
}
