package com.cleanroommc.airlock;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Interface that defines a mod that depends on Airlock.
 *
 */
public interface AirlockMod {

    default int getPriority() {
        return 0;
    }

    String getModId();

    default <T extends IForgeRegistryEntry<T>> void onRegistryEvent(RegistryEvent<T> event) { }

}
