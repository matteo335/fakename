package net.matteo.custom_name;

import net.neoforged.fml.common.Mod;

@Mod("custom_name")
public class Main {

    public Main() {
        JsonConfig.load();
    }
}
