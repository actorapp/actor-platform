package im.actor.messenger.app.util.country;

public class Country {

    public final String phoneCode;
    public final String shortName;
    public final int fullNameRes;

    public Country(String phoneCode, String shortName, int fullNameRes) {
        this.phoneCode = phoneCode;
        this.shortName = shortName;
        this.fullNameRes = fullNameRes;
    }

}
