package net.bogdanvalentin.floatater.grid;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SubGrid implements BlockGetter, CollisionGetter {
    private final Level level;
    private final GridCarrier carrier;
    private SubGridBlocks blocks = new SubGridBlocks(0, 0, 0);
    private Holder<Biome> biome;
    private AABB boundingBox = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private int version;
    private @Nullable Object renderCache;

    public SubGrid(Level level, GridCarrier carrier) {
        this.level = level;
        this.carrier = carrier;
        this.biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        this.updatePosition(carrier.getX(), carrier.getY(), carrier.getZ());
    }

    public void updatePosition(double x, double y, double z) {
        this.boundingBox = new AABB(x, y, z, x + this.blocks.sizeX() + 1.0, y + this.blocks.sizeY() + 1.0, z + this.blocks.sizeZ() + 1.0);
    }

    public void setBlocks(SubGridBlocks blocks) {
        this.blocks = blocks;
        this.version++;
        this.updatePosition(this.carrier.getX(), this.carrier.getY(), this.carrier.getZ());
    }

    public void setBiome(Holder<Biome> biome) {
        this.biome = biome;
        this.version++;
    }

    public Level level() {
        return this.level;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.blocks.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return this.blocks.sizeY();
    }

    @Override
    public int getMinY() {
        return 0;
    }

    public UUID id() {
        return this.carrier.getUUID();
    }

    public GridCarrier carrier() {
        return this.carrier;
    }

    public SubGridBlocks getBlocks() {
        return this.blocks;
    }

    public Holder<Biome> getBiome() {
        return this.biome;
    }

    public int version() {
        return this.version;
    }

    public @Nullable Object renderCache() {
        return this.renderCache;
    }

    public void setRenderCache(@Nullable Object renderCache) {
        this.renderCache = renderCache;
    }

    public AABB getNextBoundingBox() {
        return this.boundingBox;
    }

    public AABB getKnownBoundingBox() {
        Vec3 lastMovement = this.getLastMovement();
        return this.boundingBox.move(-lastMovement.x, -lastMovement.y, -lastMovement.z);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public @Nullable BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity source, AABB testArea) {
        return List.of();
    }

    public Vec3 getLastMovement() {
        return new Vec3(this.carrier.getX() - this.carrier.xOld, this.carrier.getY() - this.carrier.yOld, this.carrier.getZ() - this.carrier.zOld);
    }
}
