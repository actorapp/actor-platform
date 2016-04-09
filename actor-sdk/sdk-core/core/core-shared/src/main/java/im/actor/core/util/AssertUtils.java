package im.actor.core.util;

public class AssertUtils {
    public static void assertTrue(boolean isTrue) {
        if (!isTrue) {
            throw new RuntimeException();
        }
    }
}
