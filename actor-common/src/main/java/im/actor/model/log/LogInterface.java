package im.actor.model.log;

/**
 * Created by ex3ndr on 07.02.15.
 */
public interface LogInterface {
    public void w(String tag, String message);

    public void e(String tag, Throwable throwable);

    public void d(String tag, String message);

    public void v(String tag, String message);
}
