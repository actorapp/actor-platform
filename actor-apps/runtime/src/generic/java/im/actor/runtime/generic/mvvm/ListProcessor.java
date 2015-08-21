package im.actor.runtime.generic.mvvm;

import java.util.List;

public interface ListProcessor<T> {

    Object process(List<T> items);
}
