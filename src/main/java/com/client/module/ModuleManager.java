package com.client.module;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(new Module("Test Optimizer", "Phase 1 test module.", ModuleCategory.PERFORMANCE) {});
        register(new Module("Armor HUD", "Phase 1 placeholder HUD module.", ModuleCategory.HUD) {});
        register(new Module("Toggle Sprint", "Phase 1 placeholder utility module.", ModuleCategory.PVP) {});
    }

    public void register(Module module) { modules.add(module); }
    public List<Module> getModules() { return List.copyOf(modules); }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }
}
