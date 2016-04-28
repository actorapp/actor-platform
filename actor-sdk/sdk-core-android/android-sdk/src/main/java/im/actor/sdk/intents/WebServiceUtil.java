package im.actor.sdk.intents;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Administrator on 2016/4/25.
 */
public class WebServiceUtil {

    @SuppressWarnings("deprecation")
    public static void webServiceRun(final String ip,
                               final HashMap<String, String> par, final String methodName,
                               final Handler handler) {
        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                try {
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    String data = "";
                    if (ip == null || ip.length() == 0) {
                        data = DoWebService("",
                                methodName,
                                par);
                    } else {
                        data = DoWebService(ip,
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
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // dialog.dismiss();
                }
            }
        }.start();
    }


    private static String DoWebService(String ip,
                                       String methodName, HashMap<String, String> Params) {

        String serviceurl = "";
        if (ip != null && ip.length() > 0) {
            if (!ip.startsWith("http")) {
                ip = "http://" + ip;
            }
            serviceurl = ip;
        }

        String nameSpace = "http://tempuri.org/";

        String soapAction = nameSpace + methodName;
        SoapObject request = new SoapObject(nameSpace, methodName);
        serviceurl += "/actor.asmx";

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        for (Iterator<String> it = Params.keySet().iterator(); it.hasNext(); ) {
            String name = it.next();
            Object value = Params.get(name);

            // if("allowePushInNight".equals(name)){
            //
            // }else{
            request.addProperty(name, value);
            // }
            // String gbkValue="";
            // try {
            // gbkValue = new String(value.getBytes(), "GBK");
            // } catch (UnsupportedEncodingException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

        }
        HttpTransportSE ht = new HttpTransportSE(serviceurl);
        ht.debug = true;
        try {
            ht.call(soapAction, envelope);
            if (null == envelope.getResponse()) {
                return null;
            } else {
                return envelope.getResponse().toString();
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (ConnectException e) {
            e.printStackTrace();
            return "ConnectException";
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return "faceSmile";
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "ipError";
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return "timeOut";
        } catch (SocketException e) {
            e.printStackTrace();
            return "timeOut";
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return "ipError";
        } catch (SoapFault e) {
            e.printStackTrace();
            return "SoapFault";
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }
}
