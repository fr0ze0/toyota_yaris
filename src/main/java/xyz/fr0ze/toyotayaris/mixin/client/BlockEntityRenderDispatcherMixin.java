package xyz.fr0ze.toyotayaris.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fr0ze.toyotayaris.client.renderer.BlockDanceHandler;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void yaris$before(BlockEntity block, float partialTick, PoseStack poses,
            MultiBufferSource buffers, CallbackInfo ci) {
        poses.pushPose();
        if (BlockDanceHandler.isDancing(block.getBlockPos(), block.getBlockState())) {
            BlockDanceHandler.transform(poses, block.getBlockPos(), partialTick);
        }
    }
    @Inject(method = "render", at = @At("RETURN"))
    private void yaris$after(BlockEntity block, float partialTick, PoseStack poses,
            MultiBufferSource buffers, CallbackInfo ci) {
        poses.popPose();
    }
}
