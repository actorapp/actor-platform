/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.crypto;

public class SHA256 {
    public static native String calculate(String hexString)/*-{
        var wordArray = $wnd.CryptoJS.enc.Hex.parse(hexString);
        var hash = $wnd.CryptoJS.SHA256(wordArray);
        return $wnd.CryptoJS.enc.Hex.stringify(hash);
    }-*/;
}
