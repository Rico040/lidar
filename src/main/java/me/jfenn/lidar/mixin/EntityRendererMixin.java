package me.jfenn.lidar.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Overwrite
    public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
        // Don't render any entities
        return false;
    }

}
