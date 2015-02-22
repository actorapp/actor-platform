package im.actor.model.i18n;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class I18nEngine {
    private SupportedLocales locale;

    public I18nEngine(SupportedLocales locale) {
        this.locale = locale;
    }

    public SupportedLocales getLocale() {
        return locale;
    }
}
