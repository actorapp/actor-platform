package im.actor.sdk.intents;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

//import org.ksoap2.SoapEnvelope;
//import org.ksoap2.SoapFault;
//import org.ksoap2.serialization.SoapObject;
//import org.ksoap2.serialization.SoapSerializationEnvelope;
//import org.ksoap2.transport.HttpTransportSE;
//import org.xmlpull.v1.XmlPullParserException;


import java.util.HashMap;

import im.actor.sdk.controllers.activity.BaseFragmentActivity;

/**
 * Created by Administrator on 2016/4/25.
 */
public class WebServiceUtil {
    BaseFragmentActivity con;
//    public WebServiceUtil(BaseFragmentActivity con){
//        this.con = con;
//    }

    @SuppressWarnings("deprecation")
    public static void webServiceRun(final String ip,
                                     final HashMap<String, String> par, final String methodName,
                                     Handler handler) {
        // TODO Auto-generated method stub
        final Handler netHandler = handler;
        new Thread() {
            @Override
            public void run() {
                try {
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    String data = "";
                    if (ip == null || ip.length() == 0) {
                        data = WebHttpUtil.webHttpResponse("",
                                methodName,
                                par);
                    } else {
                        data = WebHttpUtil.webHttpResponse(ip,
                                methodName,
                                par);
                    }
                    if (data == null
                            || "java.lang.NullPointerException".equals(data
                            .trim())) {
                        b.putString("datasource", "false");
                    } else {
                        b.putString("datasource", data);
                    }
                    msg.setData(b);

                    netHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // dialog.dismiss();
                }
            }
        }.start();
    }
}
