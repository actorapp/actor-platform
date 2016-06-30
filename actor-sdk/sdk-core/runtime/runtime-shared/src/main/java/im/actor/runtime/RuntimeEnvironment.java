package im.actor.runtime;

public abstract class RuntimeEnvironment {

    private static boolean isProduction;

    public static boolean isProduction() {
        return isProduction;
    }

    public static void setIsProduction(boolean isProduction) {
        RuntimeEnvironment.isProduction = isProduction;
    }
}
