package wawa.flares.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wawa.flares.mixinterface.SetRemovedListener;

@Mixin(Entity.class)
public class EntityMixin implements SetRemovedListener {
    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void listenToSetRemoved(final Entity.RemovalReason removalReason, final CallbackInfo ci) {
        this.flares$onRemoved(removalReason);
    }

    @Override
    public void flares$onRemoved(final Entity.RemovalReason reason) {}
}
