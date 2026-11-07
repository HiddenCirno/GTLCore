package org.gtlcore.gtlcore.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.List;
import java.util.function.Supplier;

/**
 * 梦想电梯主机。
 *
 * <p>
 * 第一版只实现结构与加速轨道识别：蒸汽多方块的模块系统后续接入。
 * </p>
 */
public class DreamElevatorMachine extends LargeSteamParallelMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DreamElevatorMachine.class, LargeSteamParallelMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final int[] MODULE_SLOTS = { 6, 12, 15, 18, 24 };

    /**
     * 加速轨道等级。与 GTL 的 SEPM 外壳检测相同：所有轨道位共用同一个 match context key，
     * 第一个轨道方块决定等级，后续任意一块不一致都会让整座结构匹配失败。
     */
    public static final String RAIL_TIER_KEY = "DreamElevatorRailTier";

    public static final Int2ObjectMap<Supplier<?>> RAIL_TIER_BLOCKS = new Int2ObjectOpenHashMap<>();

    static {
        RAIL_TIER_BLOCKS.put(1, () -> Blocks.COPPER_BLOCK);
        RAIL_TIER_BLOCKS.put(2, () -> Blocks.LAPIS_BLOCK);
        RAIL_TIER_BLOCKS.put(3, () -> Blocks.DIAMOND_BLOCK);
        RAIL_TIER_BLOCKS.put(4, () -> Blocks.EMERALD_BLOCK);
        RAIL_TIER_BLOCKS.put(5, () -> Blocks.NETHERITE_BLOCK);
    }

    @Persisted
    @DescSynced
    private int railTier;

    @Persisted
    @DescSynced
    private boolean animationEnabled = true;

    public DreamElevatorMachine(IMachineBlockEntity holder) {
        super(holder, 8);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        railTier = getMultiblockState().getMatchContext().get(RAIL_TIER_KEY);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        railTier = 0;
    }

    public int getRailTier() {
        return railTier;
    }

    public int getModuleSlots() {
        return railTier > 0 ? MODULE_SLOTS[railTier - 1] : 0;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (playerIn.isShiftKeyDown()) {
            if (!isRemote()) {
                animationEnabled = !animationEnabled;
                markDirty();
                notifyBlockUpdate();
                playerIn.displayClientMessage(Component.translatable(animationEnabled ?
                        "gtlcore.machine.dream_elevator.animation.enabled" :
                        "gtlcore.machine.dream_elevator.animation.disabled"), true);
            }
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        textList.add(Component.translatable("gtlcore.machine.dream_elevator.rail_tier", railTier));
        textList.add(Component.translatable("gtlcore.machine.dream_elevator.module_slots", getModuleSlots()));
        textList.add(Component.translatable(animationEnabled ?
                "gtlcore.machine.dream_elevator.animation.status.enabled" :
                "gtlcore.machine.dream_elevator.animation.status.disabled"));
    }
}
