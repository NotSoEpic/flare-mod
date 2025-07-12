package wawa.flares.mixinterface;

import net.minecraft.world.entity.Entity;

public interface SetRemovedListener {
    void flares$accept(Entity.RemovalReason reason);
}
