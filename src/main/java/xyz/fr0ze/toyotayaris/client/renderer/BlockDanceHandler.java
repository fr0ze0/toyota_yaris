package xyz.fr0ze.toyotayaris.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import xyz.fr0ze.toyotayaris.ToyotaYarisMod;
import java.util.*;

@EventBusSubscriber(modid = ToyotaYarisMod.MOD_ID, value = Dist.CLIENT)
public final class BlockDanceHandler {
    // Immutable snapshot: chunk meshes are built on worker threads.
    private static volatile List<BlockPos> centers = List.of();
    private static ClientLevel previousLevel;
    private static final RandomSource RANDOM = RandomSource.create();

    public static boolean isDancing(BlockPos pos, BlockState state) {
        if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("minecraft")) return false;
        for (BlockPos center : centers) {
            if (center.distSqr(pos) <= EntityScaleHandler.RADIUS * EntityScaleHandler.RADIUS) return true;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != previousLevel) {
            centers = List.of();
            previousLevel = mc.level;
        }
        List<BlockPos> next = mc.level == null ? List.of() : EntityScaleHandler.getSongs().stream()
                .map(EntityScaleHandler.PlayingSong::jukeboxPos).distinct().toList();
        if (!next.equals(centers)) {
            centers = next;
            // Rebuild only when music starts/stops, never on each animation frame.
            if (mc.level != null) mc.levelRenderer.allChanged();
        }
    }

    public static void transform(PoseStack poses, BlockPos pos, float partialTick) {
        var song = EntityScaleHandler.getNearbySong(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (song == null) return;
        float wave = (float) EntityScaleHandler.yarisWave(song.getSeconds(partialTick));
        float xz = 1.0F - wave * (float) EntityScaleHandler.SQUASH_STRENGTH;
        poses.translate(0.5, 0.15F * (1.0F + wave), 0.5);
        poses.scale(xz, 1.0F + wave * 0.5F, xz);
        poses.translate(-0.5, 0, -0.5);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || centers.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var camera = event.getCamera().getPosition();
        var dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        PoseStack poses = event.getPoseStack();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Set<Long> visited = new HashSet<>();
        int r = (int) Math.ceil(EntityScaleHandler.RADIUS);
        for (BlockPos center : centers) {
            for (BlockPos cursor : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
                if (center.distSqr(cursor) > EntityScaleHandler.RADIUS * EntityScaleHandler.RADIUS) continue;
                if (!visited.add(cursor.asLong()) || !mc.level.hasChunkAt(cursor)) continue;
                BlockState state = mc.level.getBlockState(cursor);
                if (state.isAir() || !isDancing(cursor, state)) continue;
                double extent = 2.0 + Math.abs(EntityScaleHandler.SQUASH_STRENGTH);
                if (!event.getFrustum().isVisible(new AABB(cursor).inflate(extent))) continue;
                poses.pushPose();
                poses.translate(cursor.getX() - camera.x, cursor.getY() - camera.y, cursor.getZ() - camera.z);
                transform(poses, cursor, partial);
                try {
                    if (state.getRenderShape() == RenderShape.MODEL) {
                        var model = dispatcher.getBlockModel(state);
                        ModelData data = model.getModelData(mc.level, cursor, state, ModelData.EMPTY);
                        RANDOM.setSeed(state.getSeed(cursor));
                        for (RenderType layer : model.getRenderTypes(state, RANDOM, data)) {
                            // World tessellation preserves biome tint, model offsets, and lighting.
                            // Draw all faces: squash opens gaps between adjacent blocks.
                            dispatcher.getModelRenderer().tesselateBlock(mc.level, model, state, cursor,
                                    poses, buffers.getBuffer(RenderTypeHelper.getEntityRenderType(layer, false)),
                                    false, RANDOM, state.getSeed(cursor), OverlayTexture.NO_OVERLAY, data, layer);
                        }
                    }
                    if (!state.getFluidState().isEmpty()) {
                        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(
                                net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS));
                        dispatcher.getLiquidBlockRenderer().tesselate(mc.level, cursor,
                                new FluidVertexConsumer(consumer, poses.last(), cursor), state, state.getFluidState());
                    }
                } finally {
                    poses.popPose();
                }
            }
        }
        buffers.endBatch();
    }
}
