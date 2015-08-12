package im.actor.runtime.cocoa;

import im.actor.runtime.LocaleRuntime;

public class CocoaLocaleProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        return "En";
    }

    @Override
    public native String formatDate(long date)/*-[
        var dt = [NSDate timeIntervalSince1970: date/1000.0];
        var formatter = [NSDateFormatter init];
        formatter.dateStyle = NSDateFormatterStyleShortStyle;
        return [formatter stringFromDate(dt)];
    ]-*/;

    @Override
    public String formatTime(long date) {
        // TODO: Implement
        return null;
    }
}
