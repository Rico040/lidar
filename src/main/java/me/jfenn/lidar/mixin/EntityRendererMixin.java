package me.jfenn.lidar.mixin;

import me.jfenn.lidar.Lidar;
import me.jfenn.lidar.config.LidarConfig;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z", at = @At("HEAD"), cancellable = true)
    public void shouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        LidarConfig config = Lidar.INSTANCE.getConfig();
        if (config.isActive()) {
            String entityType = Registry.ENTITY_TYPE.getId(entity.getType()).toString();
            if (!config.getEntityRender().contains(entityType)) {
                // skip all entity renders that are not in entityRender set
                ci.setReturnValue(false);
                ci.cancel();
            }
        }
    }

}
