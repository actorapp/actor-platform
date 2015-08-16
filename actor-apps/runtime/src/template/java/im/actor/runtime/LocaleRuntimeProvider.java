package im.actor.runtime;

public class LocaleRuntimeProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
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
