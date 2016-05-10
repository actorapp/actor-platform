package im.actor.sdk.intents;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;


//import org.ksoap2.SoapEnvelope;
//import org.ksoap2.SoapFault;
//import org.ksoap2.serialization.SoapObject;
//import org.ksoap2.serialization.SoapSerializationEnvelope;
//import org.ksoap2.transport.HttpTransportSE;
//import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by Administrator on 2016/4/25.
 */
public class WebHttpUtil {
//    public WebServiceUtil(BaseFragmentActivity con){
//        this.con = con;
//    }


//    private static String DoWebService(String ip,
//                                       String methodName, HashMap<String, String> Params) {
//
//        String serviceurl = "";
//        if (ip != null && ip.length() > 0) {
//            if (!ip.startsWith("http")) {
//                ip = "http://" + ip;
//            }
//            serviceurl = ip;
//        }
//
//        String nameSpace = "http://www.eaglesoft.cn/";
//
//        String soapAction = nameSpace + methodName;
//        SoapObject request = new SoapObject(nameSpace, methodName);
//        serviceurl += "/actor.asmx";
//
//        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
//                SoapEnvelope.VER11);
//        envelope.bodyOut = request;
//        envelope.dotNet = true;
//        for (Iterator<String> it = Params.keySet().iterator(); it.hasNext(); ) {
//            String name = it.next();
//            Object value = Params.get(name);
//
//            // if("allowePushInNight".equals(name)){
//            //
//            // }else{
//            request.addProperty(name, value);
//            // }
//            // String gbkValue="";
//            // try {
//            // gbkValue = new String(value.getBytes(), "GBK");
//            // } catch (UnsupportedEncodingException e) {
//            // // TODO Auto-generated catch block
//            // e.printStackTrace();
//            // }
//
//        }
//        HttpTransportSE ht = new HttpTransportSE(serviceurl);
//        ht.debug = true;
//        try {
//            ht.call(soapAction, envelope);
//            if (null == envelope.getResponse()) {
//                return null;
//            } else {
//                return envelope.getResponse().toString();
//            }
//
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//            return null;
//        } catch (ConnectException e) {
//            e.printStackTrace();
//            return "ConnectException";
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            return "faceSmile";
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            return "ipError";
//        } catch (SocketTimeoutException e) {
//            e.printStackTrace();
//            return "timeOut";
//        } catch (SocketException e) {
//            e.printStackTrace();
//            return "timeOut";
//        } catch (XmlPullParserException e) {
//            e.printStackTrace();
//            return "ipError";
//        } catch (SoapFault e) {
//            e.printStackTrace();
//            return "SoapFault";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return e.toString();
//        }
//    }


    public static String webHttpResponse(String ip,
                                           String methodName, HashMap<String, String> Params) {
        // 提交Post请求
        HttpURLConnection conn = null;
        try {

            String par = "";
            for (Iterator<String> it = Params.keySet().iterator(); it.hasNext(); ) {
                String name = it.next();
                String value = Params.get(name);
                if (par.length() == 0) {
                    par = name + "=" + value;
                } else {
                    par += "&" + name + "=" + value;
                }

//                soap += "<" + name + ">" + value + "</" + name + ">\n";
            }
            String httpUrl = ip + "/actor.asmx/" + methodName;
            if (par.length() != 0) {
                httpUrl += "?" + par;
            }
            URL url = new URL(httpUrl);
            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5 * 1000);
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("SOAPAction", "http://www.eaglesoft.cn/" + methodName);

//            String soap = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
//                    "\n<soap:Body>\n< " + methodName + " xmlns=\"http://www.eaglesoft.cn/\"\n";

//            soap += "</" + methodName + ">" + " </soap:Body>\n" +
//                    "</soap:Envelope>";
//            conn.setRequestProperty("Content-Length", String.valueOf(par.getBytes().length));
//            OutputStream  outStream = conn.getOutputStream();
//            outStream.write(par.getBytes());
//            outStream.flush();
//            outStream.close();
            if (conn.getResponseCode() == 200) {
                // 解析返回信息
                InputStream is = conn.getInputStream();
                byte[] b = new byte[1024];
                int len = 0;
                String s = "";
                while ((len = is.read(b)) != -1) {
                    String ss = new String(b, 0, len, "UTF-8");
                    s += ss;
                }
                System.out.println(s);
                is.close();
                return s;
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
        }  catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            conn.disconnect();
        }

        return "Error";
    }


}
