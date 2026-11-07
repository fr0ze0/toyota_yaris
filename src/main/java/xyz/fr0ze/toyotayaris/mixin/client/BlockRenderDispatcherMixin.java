package xyz.fr0ze.toyotayaris.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.fr0ze.toyotayaris.client.renderer.BlockDanceHandler;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {
    @Inject(method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"), cancellable = true)
    private void yaris$hideModel(BlockState state, BlockPos pos, BlockAndTintGetter level,
            PoseStack poses, VertexConsumer consumer, boolean sides, RandomSource random,
            ModelData data, RenderType type, CallbackInfo ci) {
        if (BlockDanceHandler.isDancing(pos, state)) ci.cancel();
    }

    @Inject(method = "renderLiquid", at = @At("HEAD"), cancellable = true)
    private void yaris$hideFluid(BlockPos pos, BlockAndTintGetter level, VertexConsumer consumer,
            BlockState state, FluidState fluid, CallbackInfo ci) {
        if (BlockDanceHandler.isDancing(pos, state)) ci.cancel();
    }
}
