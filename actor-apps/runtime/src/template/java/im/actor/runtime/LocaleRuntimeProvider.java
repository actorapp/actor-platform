package im.actor.runtime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class LocaleRuntimeProvider implements LocaleRuntime {
    @Override
    public String getCurrentLocale() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public boolean is24Hours() {
        throw new RuntimeException("Dumb");
    }

    @Override
    public String formatDate(long date) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public String formatTime(long date) {
        throw new RuntimeException("Dumb");
    }
}
