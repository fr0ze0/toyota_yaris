package xyz.fr0ze.toyotayaris.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import xyz.fr0ze.toyotayaris.item.custom.DancingBlockEntity;

import static xyz.fr0ze.toyotayaris.client.renderer.EntityScaleHandler.*;

public class DancingBlockRenderer extends EntityRenderer<DancingBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public DancingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(DancingBlockEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.isAir()) return;

        // Ищем музыку вокруг Entity
        PlayingSong song = getNearbySong(entity.getX(), entity.getY(), entity.getZ());

        poseStack.pushPose();

        // Смещаем Pivot Point в центр нижней грани (0.5, 0.0, 0.5)
        poseStack.translate(0.5, 0.0, 0.5);

        if (song != null) {
            double time = song.getSeconds(partialTick);
            float wave = (float) yarisWave(time);

            float scaleY = 1.0F + wave * 0.5F;
            float scaleXZ = 1.0F - wave * (float)SQUASH_STRENGTH;
            float bounce = 0.15F * (1.0F + wave);

            // ТЕ САМЫЕ ТРАНСФОРМАЦИИ ИЗ ВАШЕГО КОДА
            poseStack.translate(0.0, bounce, 0.0);
            poseStack.scale(scaleXZ, scaleY, scaleXZ);
        }

        // Возвращаем сетку обратно
        poseStack.translate(-0.5, 0.0, -0.5);

        RenderType renderType =
                ItemBlockRenderTypes.getChunkRenderType(state);

        // Рендерим модель ванильного блока динамически в кадре
        this.blockRenderer.renderSingleBlock(
                state,
                poseStack,
                bufferSource,
                packedLight,
                655360,
                ModelData.EMPTY,
                renderType
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DancingBlockEntity entity) {
        return InventoryMenu.BLOCK_ATLAS; // Блоки используют общий атлас текстур
    }
}