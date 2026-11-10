package net.matteo.custom_name;

import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        JsonConfig.load();
    }
}
