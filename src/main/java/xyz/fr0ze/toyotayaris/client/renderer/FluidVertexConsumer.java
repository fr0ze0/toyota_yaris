package xyz.fr0ze.toyotayaris.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.texture.OverlayTexture;

// Vanilla fluid vertices use section-local coordinates, not block-local ones.
final class FluidVertexConsumer implements VertexConsumer {
    private final VertexConsumer target;
    private final PoseStack.Pose pose;
    private final int x, y, z;
    FluidVertexConsumer(VertexConsumer target, PoseStack.Pose pose, BlockPos pos) {
        this.target = target;
        this.pose = pose;
        x = pos.getX() & 15; y = pos.getY() & 15; z = pos.getZ() & 15;
    }
    @Override public VertexConsumer addVertex(float vx, float vy, float vz) {
        target.addVertex(pose.pose(), vx - x, vy - y, vz - z);
        target.setOverlay(OverlayTexture.NO_OVERLAY);
        return this;
    }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) { target.setColor(r,g,b,a); return this; }
    @Override public VertexConsumer setUv(float u, float v) { target.setUv(u,v); return this; }
    @Override public VertexConsumer setUv1(int u, int v) { target.setUv1(u,v); return this; }
    @Override public VertexConsumer setUv2(int u, int v) { target.setUv2(u,v); return this; }
    @Override public VertexConsumer setNormal(float nx, float ny, float nz) {
        target.setNormal(pose, nx, ny, nz); return this;
    }
}
