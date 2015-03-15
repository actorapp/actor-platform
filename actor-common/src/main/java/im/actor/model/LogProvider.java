package im.actor.model;

/**
 * Created by ex3ndr on 07.02.15.
 */
public interface LogProvider {
    public void w(String tag, String message);

    public void e(String tag, Throwable throwable);

    public void d(String tag, String message);

    public void v(String tag, String message);
}
