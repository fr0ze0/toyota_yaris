package xyz.fr0ze.toyotayaris.item.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DancingBlockEntity extends Entity {
    private static final EntityDataAccessor<BlockState> BLOCK_STATE =
            SynchedEntityData.defineId(DancingBlockEntity.class, EntityDataSerializers.BLOCK_STATE);

    public DancingBlockEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setBlockState(BlockState state) {
        this.entityData.set(BLOCK_STATE, state);
    }

    public BlockState getBlockState() {
        return this.entityData.get(BLOCK_STATE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (!isMusicPlayingHere(this.getX(), this.getY(), this.getZ())) {
                this.level().setBlock(this.blockPosition(), this.getBlockState(), 3);
                this.discard();
            }
        }
    }

    private boolean isMusicPlayingHere(double x, double y, double z) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState", 10)) {
            this.setBlockState(NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("BlockState", NbtUtils.writeBlockState(this.getBlockState()));
    }
}
