package im.actor.model.js.providers.crypto;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class SHA256 {
    public static native String calculate(String hexString)/*-{
        var wordArray = $wnd.CryptoJS.enc.Hex.parse(hexString);
        var hash = $wnd.CryptoJS.SHA256(wordArray);
        return $wnd.CryptoJS.enc.Hex.stringify(hash);
    }-*/;
}
