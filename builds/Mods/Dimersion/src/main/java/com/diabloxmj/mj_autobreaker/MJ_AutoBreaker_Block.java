package com.diabloxmj.mj_autobreaker;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public class MJ_AutoBreaker_Block extends BlockWithEntity {

    // 1. Déclaration des propriétés de direction et de Redstone
    // Remplace l'ancienne ligne par celle-ci :
    public static final net.minecraft.state.property.EnumProperty<net.minecraft.util.math.Direction> FACING = net.minecraft.state.property.Properties.FACING;
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

    public static final MapCodec<MJ_AutoBreaker_Block> CODEC = createCodec(MJ_AutoBreaker_Block::new);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    // Le constructeur requis pour le Codec et l'enregistrement
    public MJ_AutoBreaker_Block(Settings settings) {
        super(settings);
        // Par défaut, le bloc regarde vers le Nord et n'est pas activé par la Redstone
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(TRIGGERED, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MJ_AutoBreaker_BlockEntity(pos, state);
    }

    @Override
    protected net.minecraft.block.BlockRenderType getRenderType(BlockState state) {
        return net.minecraft.block.BlockRenderType.MODEL;
    }

    // 2. Définir l'orientation du bloc lorsqu'on le pose au sol / mur / plafond
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getPlayerLookDirection().getOpposite()) // Face opposée au regard du joueur (comme le Dispenser)
                .with(TRIGGERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    // 3. Gestion du changement de signal de Redstone (Impulsion)
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        // Dès qu'on pose le bloc, on programme un premier tick si alimenté
        if (world.isReceivingRedstonePower(pos)) {
            world.scheduleBlockTick(pos, this, 10); // Check toutes les 10 ticks (0.5s)
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify) {
        if (world.isReceivingRedstonePower(pos) && !state.get(TRIGGERED)) {
            world.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
            world.scheduleBlockTick(pos, this, 10); // Démarre le cycle
        } else if (!world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.with(TRIGGERED, false), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.isReceivingRedstonePower(pos)) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof MJ_AutoBreaker_BlockEntity autoBreaker) {
                // NE CASSES QUE SI LE BLOC N'EST PAS DE L'AIR
                if (!world.getBlockState(pos.offset(state.get(FACING))).isAir()) {
                    autoBreaker.tryBreakBlock(world, pos, state.get(FACING));
                }
            }
            world.scheduleBlockTick(pos, this, 20); // Augmente le délai à 20 ticks (1 sec)
        }
    }

    // 4. Enregistrement obligatoire des propriétés dans le StateManager de Minecraft
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    // Permet au bloc d'être tourné proprement avec une boussole de modding ou un piston mécanique
    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.hit.BlockHitResult hit) {
        if (!world.isClient()) {
            net.minecraft.block.entity.BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MJ_AutoBreaker_BlockEntity autoBreakerEntity) {
                player.openHandledScreen(autoBreakerEntity);
            }
        }
        return net.minecraft.util.ActionResult.SUCCESS;
    }
}