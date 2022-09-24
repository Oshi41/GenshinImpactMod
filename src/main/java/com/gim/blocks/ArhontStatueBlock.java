package com.gim.blocks;

import com.gim.GenshinImpactMod;
import com.gim.blocks.state.Fluids;
import com.gim.menu.WishMenu;
import com.gim.registry.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ArhontStatueBlock extends Block implements SimpleLiquidBlockContainer {
    protected static final VoxelShape SHAPE;

    static {
        VoxelShape head = Block.box(
                4, 24, 4,
                4 + 8, 24 + 8, 4 + 8);

        VoxelShape rightHand = Block.box(
                12, 12, 6,
                12 + 4, 12 + 12, 6 + 4);

        VoxelShape leftHand = Block.box(
                0, 12, 6,
                0 + 4, 12 + 12, 6 + 4);

        VoxelShape rightLeg = Block.box(
                7.9, 0, 6,
                7.9 + 4, 0 + 12, 6 + 4);

        VoxelShape leftLeg = Block.box(
                4.1, 0, 6,
                4.1 + 4, 0 + 12, 6 + 4);

        VoxelShape body = Block.box(
                4, 12, 6,
                4 + 8, 12 + 12, 6 + 4);

        SHAPE = Shapes.or(head, rightLeg, leftLeg, body, rightHand, leftHand);
    }

    // Well-known wish ID
    private static ResourceLocation ALL = new ResourceLocation(GenshinImpactMod.ModID, "wish");
    private static LootTable _allRecipes;

    public ArhontStatueBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIQUIDLOGGED, Fluids.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIQUIDLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(hand);

        // no item
        if (itemInHand.isEmpty() || !Objects.equals(itemInHand.getItem(), Items.wish)) {
            return InteractionResult.FAIL;
        }

        // fill loot tables
        if (_allRecipes == null && player.getLevel().getServer() != null) {
            _allRecipes = player.getLevel().getServer().getLootTables().get(ALL);
        }

        if (_allRecipes == null || _allRecipes == LootTable.EMPTY) {
            GenshinImpactMod.LOGGER.error("There is no any wish loot tables.");
            return InteractionResult.FAIL;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            LootContext.Builder builder = new LootContext.Builder(serverPlayer.getLevel())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ()))
                    .withRandom(player.getRandom())
                    .withLuck(player.getLuck());

            LootContext lootContext = builder.create(LootContextParamSets.EMPTY);
            List<ItemStack> items = new ArrayList<>();

            // trying to execute 10 times or error
            for (int i = 0; i < 10 && items.isEmpty(); i++) {
                items = _allRecipes.getRandomItems(lootContext);
            }

            if (items.isEmpty()) {
                GenshinImpactMod.LOGGER.error("Seems like there is no results from wish! Recheck recipes");
                return InteractionResult.FAIL;
            }

            // finding very first item
            ItemStack itemStack = items.get(0);

            // server side menu provider
            SimpleMenuProvider menuProvider = new SimpleMenuProvider(
                    (containerId, inventory, player1) -> new WishMenu(containerId, itemStack),
                    new TextComponent(""));

            // server->client transmission
            Consumer<FriendlyByteBuf> consumer = friendlyByteBuf -> friendlyByteBuf.writeItem(itemStack);

            // requesting to open GUI
            NetworkHooks.openGui(serverPlayer, menuProvider, consumer);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(LIQUIDLOGGED).getFluid().defaultFluidState();
    }

    @Override
    public BlockState updateShape(BlockState state1, Direction direction, BlockState state2, LevelAccessor level, BlockPos pos1, BlockPos pos2) {
        Fluids fluidEnum = state1.getValue(LIQUIDLOGGED);

        if (!fluidEnum.isEmpty()) {
            level.scheduleTick(pos1, fluidEnum.getFluid(), fluidEnum.getFluid().getTickDelay(level));
        }

        return state1;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor levelaccessor = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Fluid fluid = levelaccessor.getFluidState(blockpos).getType();
        Fluids fluidEnum = Fluids.from(fluid);
        if (fluidEnum == null)
            fluidEnum = Fluids.EMPTY;


        return defaultBlockState().setValue(LIQUIDLOGGED, fluidEnum);
    }
}
