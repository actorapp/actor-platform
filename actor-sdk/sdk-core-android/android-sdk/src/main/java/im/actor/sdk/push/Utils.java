package im.actor.sdk.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Utils {
    public static final String TAG = "PushDemoActivity";
    public static final String RESPONSE_METHOD = "method";
    public static final String RESPONSE_CONTENT = "content";
    public static final String RESPONSE_ERRCODE = "errcode";
    protected static final String ACTION_LOGIN = "com.baidu.pushdemo.action.LOGIN";
    public static final String ACTION_MESSAGE = "com.baiud.pushdemo.action.MESSAGE";
    public static final String ACTION_RESPONSE = "bccsclient.action.RESPONSE";
    public static final String ACTION_SHOW_MESSAGE = "bccsclient.action.SHOW_MESSAGE";
    protected static final String EXTRA_ACCESS_TOKEN = "access_token";
    public static final String EXTRA_MESSAGE = "message";

    public static String logStringCache = "";

    // 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "error " + e.getMessage());
        }
        return apiKey;
    }

    public static List<String> getTagsList(String originalText) {
        if (originalText == null || originalText.equals("")) {
            return null;
        }
        List<String> tags = new ArrayList<String>();
        int indexOfComma = originalText.indexOf(',');
        String tag;
        while (indexOfComma != -1) {
            tag = originalText.substring(0, indexOfComma);
            tags.add(tag);

            originalText = originalText.substring(indexOfComma + 1);
            indexOfComma = originalText.indexOf(',');
        }

        tags.add(originalText);
        return tags;
    }

    public static String getLogText(Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString("log_text", "");
    }

    public static void setLogText(Context context, String text) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putString("log_text", text);
        editor.commit();
    }

    public static int isWhatPhone() {
        try {
            //BOARD 主板
//            String phoneInfo = "BOARD: " + android.os.Build.BOARD;
//            phoneInfo += "\r\n"+", BOOTLOADER: " + android.os.Build.BOOTLOADER;
////BRAND 运营商
//            phoneInfo += "\r\n"+", BRAND: " + android.os.Build.BRAND;
//            phoneInfo += "\r\n"+", CPU_ABI: " + android.os.Build.CPU_ABI;
//            phoneInfo += "\r\n"+", CPU_ABI2: " + android.os.Build.CPU_ABI2;
////DEVICE 驱动
//            phoneInfo += "\r\n"+", DEVICE: " + android.os.Build.DEVICE;
////DISPLAY 显示
//            phoneInfo += "\r\n"+", DISPLAY: " + android.os.Build.DISPLAY;
////指纹
//            phoneInfo += "\r\n"+", FINGERPRINT: " + android.os.Build.FINGERPRINT;
////HARDWARE 硬件
//            phoneInfo += "\r\n"+", HARDWARE: " + android.os.Build.HARDWARE;
//            phoneInfo += "\r\n"+", HOST: " + android.os.Build.HOST;
//            phoneInfo += "\r\n"+", ID: " + android.os.Build.ID;
////MANUFACTURER 生产厂家
//            phoneInfo += "\r\n"+", MANUFACTURER: " + android.os.Build.MANUFACTURER;
////MODEL 机型
//            phoneInfo += "\r\n"+", MODEL: " + android.os.Build.MODEL;
//            phoneInfo += "\r\n"+"\r\n"+", PRODUCT: " + android.os.Build.PRODUCT;
//            phoneInfo += "\r\n"+", RADIO: " + android.os.Build.RADIO;
//            phoneInfo += "\r\n"+", RADITAGSO: " + android.os.Build.TAGS;
//            phoneInfo +="\r\n"+ ", TIME: " + android.os.Build.TIME;
//            phoneInfo += "\r\n"+", TYPE: " + android.os.Build.TYPE;
//            phoneInfo += "\r\n"+", USER: " + android.os.Build.USER;
////VERSION.RELEASE 固件版本
//            phoneInfo += "\r\n"+", VERSION.RELEASE: " + android.os.Build.VERSION.RELEASE;
//            phoneInfo += "\r\n"+", VERSION.CODENAME: " + android.os.Build.VERSION.CODENAME;
////VERSION.INCREMENTAL 基带版本
//            phoneInfo += "\r\n"+", VERSION.INCREMENTAL: " + android.os.Build.VERSION.INCREMENTAL;
////VERSION.SDK SDK版本
//            phoneInfo += "\r\n"+", VERSION.SDK: " + android.os.Build.VERSION.SDK;
//            phoneInfo += "\r\n"+", VERSION.SDK_INT: " + android.os.Build.VERSION.SDK_INT;
//            writeTxtToFile(phoneInfo,"logBuild.txt");
            if (Build.MANUFACTURER.indexOf("HUAWEI") > -1) {
                return 1;
            }else if (Build.MANUFACTURER.indexOf("Xiaomi") > -1) {
                return 2;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    static String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/Actor/log/";

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent,String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        try {
            FileOutputStream writer = new FileOutputStream(strFilePath);
            BufferedReader input = new BufferedReader(new FileReader(strFilePath));
            String str,s1= "";
            while ((str = input.readLine()) != null) {
                s1 += str + "\r\n";
            }
            System.out.println(s1);
            s1 += strcontent;
            input.close();

            writer.write(s1.getBytes());
            writer.close();
        } catch (Exception e) {
            System.out.println("写文件内容操作出错");
            e.printStackTrace();
        }
    }

    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
        }
    }

}