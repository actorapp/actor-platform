package im.actor.core.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.actor.core.api.ApiUpdateOptimization;

public class Configuration {

    public static final List<ApiUpdateOptimization> OPTIMIZATIONS;

    static {
        List<ApiUpdateOptimization> opts = new ArrayList<>();
        opts.add(ApiUpdateOptimization.STRIP_ENTITIES);
        opts.add(ApiUpdateOptimization.STRIP_COUNTERS);
        opts.add(ApiUpdateOptimization.COMPACT_USERS);
        OPTIMIZATIONS = Collections.unmodifiableList(opts);
    }
}
