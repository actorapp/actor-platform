package im.actor.core.modules.conductor;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.GlobalStateVM;

public class ConductorModule extends AbsModule {

    private AppStateVM appStateVM;
    private GlobalStateVM globalStateVM;
    private Conductor conductor;

    public ConductorModule(ModuleContext context) {
        super(context);

        globalStateVM = new GlobalStateVM(context);
    }

    public void run() {
        this.appStateVM = new AppStateVM(context());
        this.conductor = new Conductor(context());
    }

    public void runAfter() {
        this.conductor.finishLaunching();
    }

    public Conductor getConductor() {
        return conductor;
    }

    public AppStateVM getAppStateVM() {
        return appStateVM;
    }

    public GlobalStateVM getGlobalStateVM() {
        return globalStateVM;
    }
}
