package im.actor.runtime.webrtc.sdp.entities;

import java.util.ArrayList;
import java.util.HashMap;

public class SDPCodec {

    private int index;
    private String name;
    private int clockRate;
    private String args;
    private HashMap<String, String> format;
    private ArrayList<String> codecFeedback;

    public SDPCodec(int index, String name, int clockRate, String args) {
        this.index = index;
        this.name = name;
        this.clockRate = clockRate;
        this.args = args;
        this.format = new HashMap<>();
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getClockRate() {
        return clockRate;
    }

    public String getArgs() {
        return args;
    }

    public HashMap<String, String> getFormat() {
        return format;
    }

    public void setFormat(HashMap<String, String> format) {
        this.format = format;
    }

    public ArrayList<String> getCodecFeedback() {
        return codecFeedback;
    }

    public void setCodecFeedback(ArrayList<String> codecFeedback) {
        this.codecFeedback = codecFeedback;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setClockRate(int clockRate) {
        this.clockRate = clockRate;
    }

    public String toSDP() {
        String res = "a=rtpmap:" + index + " " + name + "/" + clockRate;
        if (args != null) {
            res += "/" + args;
        }
        res += "\r\n";
        if (format != null && format.size() > 0) {
            res += "a=fmtp:" + index;
            for (String s : format.keySet()) {
                res += " " + s + "=" + format.get(s) + ";";
            }
            res += "\r\n";
        }
        if (codecFeedback != null) {
            for (String s : codecFeedback) {
                res += "a=rtcp-fb:" + index + " " + s + "\r\n";
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return toSDP();
    }
}
