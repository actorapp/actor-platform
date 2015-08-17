package im.actor.core.modules;

public interface ModuleCreator<T extends Module> {
    T createModule(ModuleContext context);
}
