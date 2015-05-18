package com.maxwell.bodysensor.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.database.Cursor;

public class UtilCVT {
    public static String dbColumnTypeToString(int type) {
        switch (type)
        {
        case Cursor.FIELD_TYPE_BLOB:
            return "BLOB";
        case Cursor.FIELD_TYPE_FLOAT:
            return "FLOAT";
        case Cursor.FIELD_TYPE_INTEGER:
            return "INTEGER";
        case Cursor.FIELD_TYPE_NULL:
            return "NULL";
        case Cursor.FIELD_TYPE_STRING:
            return "STRING";
        };
        return "UNKNOWN";
    }

    public static String doubleToString(double d, int iNumDecimal) {
        String strFormat = "%." + Integer.toString(iNumDecimal) + "f";
        return String.format(strFormat, d);
    }

    public static String intToStringLeadingZero(int num, int digits) {
        String strFormat = "%0" + Integer.toString(digits) + "d";
        return String.format(strFormat, num);
    }

    // if digits is >0, need to add leading zeros
    public static String longToStringEvery3Digit(long num) {
        String strTemp = Long.toString(num);
        String strResult = "";
        int len = strTemp.length();
        int iDigits = 0;
        for (int i = len - 1; i >= 0; i--) {
            strResult = strTemp.charAt(i) + strResult;
            iDigits++;
            if (((iDigits % 3) == 0) && (i > 0)) {
                strResult = " " + strResult;
            }
        }

        return strResult;
    }

    public static int doubleToInt(double d) {
        return (int)(d+0.5);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int bytesToInt(byte[] bytes) {
       if (bytes.length>4) {
            UtilDBG.e("bytesToInt, bytes.length= " + bytes.length);
            return 0;
        }

        byte [] bCVT = new byte[4];
        int iCVT = 4-1;
        int iOriginal = bytes.length-1;
        for (; iOriginal>=0; --iCVT, --iOriginal) {
            bCVT[iCVT] = bytes[iOriginal];
        }
        for (; iCVT>=0; --iCVT) {
            bCVT[iCVT] = 0x00;
        }

        ByteBuffer bb = ByteBuffer.wrap(bCVT);
        bb.order(ByteOrder.BIG_ENDIAN);

        return bb.getInt();
    }

    public static long bytesToLong(byte[] bytes) {
        if (bytes.length>8) {
            UtilDBG.e("bytesToLong, bytes.length= " + bytes.length);
            return 0;
        }

        byte [] bCVT = new byte[8];
        int iCVT = 8-1;
        int iOriginal = bytes.length-1;
        for (; iOriginal>=0; --iCVT, --iOriginal) {
            bCVT[iCVT] = bytes[iOriginal];
        }
        for (; iCVT>=0; --iCVT) {
            bCVT[iCVT] = 0x00;
        }

        ByteBuffer bb = ByteBuffer.wrap(bCVT);
        bb.order(ByteOrder.BIG_ENDIAN);

        return bb.getLong();
    }

    public static boolean bytesAllZero(byte [] bytes) {
        for (byte b : bytes) {
            if (b!=0x00)
                return false;
        }
        return true;
    }

    public static String addColonOnMacAddress(String strAddress) {
        String strAddressNew = "";
        switch (getMacAddressType(strAddress)) {
        case 1: // 12 -> 17
            {
                for (int i=0; i<12; ++i) {
                    if (i%2==0) {
                        strAddressNew += strAddress.charAt(i);
                    } else {
                        strAddressNew += strAddress.charAt(i);
                        if (i!=(12-1))
                            strAddressNew += ':';
                    }
                }
            }
            break;
        case 2: // 17 -> 17
            UtilDBG.e("check the code, should not call, performance impact");
            strAddressNew = strAddress;
            break;
        }
        return strAddressNew;
    }

    public static String removeColonOnMacAddress(String strAddress) {
        String strAddressNew = "";
        switch (getMacAddressType(strAddress)) {
        case 1: // 12 -> 12
            {
                UtilDBG.e("check the code, should not call, performance impact");
                strAddressNew = strAddress;
            }
            break;
        case 2: // 17 -> 12
            {
                for (int i=0; i<17; ++i) {
                    if (i%3==0 || i%3==1) {
                        strAddressNew += strAddress.charAt(i);
                    }
                }
            }
            break;
        }
        return strAddressNew;
    }

    public static int getMacAddressType(String strAddress) {
        switch (strAddress.length()) {
        case 12:
            {
                boolean b = true;
                for (int i=0; i<12; ++i) {
                    if (!isCharNumOrLetter(strAddress.charAt(i)))
                    {
                        b = false;
                        break;
                    }
                }
                if (b)
                    return 1;
            }
            break;
        case 17:
            {
                boolean b = true;
                for (int i=0; i<17; ++i) {
                    if (i%3==0 || i%3==1) {
                        if (!isCharNumOrLetter(strAddress.charAt(i)))
                        {
                            b = false;
                            break;
                        }
                    } else {
                        if (strAddress.charAt(i)!=':') {
                            b = false;
                            break;
                        }
                    }
                }
                if (b)
                    return 2;
            }
            break;
        }
        return 0;
    }

    private static boolean isCharNumOrLetter(char c) {
        if (c>='a' && c<='z')
            return true;
        if (c>='A' && c<='Z')
            return true;
        if (c>='0' && c<='9')
            return true;
        return false;
    }

    private static final double unitCmPerInch = 2.54;
    private static final double unitKgPerLb = 0.45359237;
    private static final double unitKmPerMile = 1.609344;
    public static double cmToInch(double cm) {
        return cm / unitCmPerInch;
    }

    public static double inchToCm(double inch) {
        return inch * unitCmPerInch;
    }

    public static double kgToLb(double kg) {
        return kg / unitKgPerLb;
    }

    public static double lbToKg(double lb) {
        return lb * unitKgPerLb;
    }

    public static double kmToMile(double km) {
        return km / unitKmPerMile;
    }

    public static double mileToKm(double mile) {
        return mile * unitKmPerMile;
    }

    public static String encodeUTF8(String src) {
        String dst = null;
        try {
            dst = URLEncoder.encode(src, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return dst;
    }

    public static String decodeUTF8(String src) {
        String dst = null;
        try {
            dst = URLDecoder.decode(src, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return dst;
    }
}
