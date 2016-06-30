package im.actor.runtime.function;

public class Predicates {
    public static Predicate NULL = new Predicate() {
        @Override
        public boolean apply(Object o) {
            return o == null;
        }
    };

    public static Predicate NOT_NULL = new Predicate() {
        @Override
        public boolean apply(Object o) {
            return o != null;
        }
    };
}
