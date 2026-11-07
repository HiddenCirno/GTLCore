package org.gtlcore.gtlcore.common.data.machines;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.api.pattern.GTLPredicates;
import org.gtlcore.gtlcore.client.renderer.machine.DreamElevatorRenderer;
import org.gtlcore.gtlcore.common.data.*;
import org.gtlcore.gtlcore.common.data.machines.structure.AdvancedMultiBlockMachineStructureA;
import org.gtlcore.gtlcore.common.data.machines.structure.DreamElevatorStructure;
import org.gtlcore.gtlcore.common.data.machines.structure.MultiBlockMachineBStructure;
import org.gtlcore.gtlcore.common.machine.multiblock.electric.WorkableElectricParallelHatchMultipleRecipesMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.noenergy.EyeOfWoodMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.noenergy.PrimitiveOreMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.steam.DreamElevatorMachine;
import org.gtlcore.gtlcore.config.ConfigHolder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.common.data.GCyMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class MultiBlockMachineB {

    public static void init() {}

    private static Component[] eyeOfWoodTooltips() {
        Component[] tooltips = new Component[19];
        for (int i = 0; i < tooltips.length; i++) {
            tooltips[i] = Component.translatable("gtlcore.machine.eye_of_wood.tooltip." + i);
        }
        return tooltips;
    }

    private static TraceabilityPredicate dreamSteamCasing() {
        // PartAbility.STEAM 的 getAllBlocks() 是 memoize 的，GTL 的大型蒸汽输入仓注册较晚，
        // 可能不在缓存里，因此这里显式补上它的方块。
        Block[] steamParts = Stream.concat(
                PartAbility.STEAM.getAllBlocks().stream(),
                Stream.of(GTLMachines.LARGE_STEAM_HATCH.getBlock()))
                .distinct()
                .toArray(Block[]::new);
        return blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                .or(Predicates.blocks(steamParts).setMaxGlobalLimited(4))
                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4))
                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(4))
                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4))
                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(4));
    }

    public final static MultiblockMachineDefinition GRAVITATION_SHOCKBURST = REGISTRATE.multiblock("gravitation_shockburst", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTLRecipeTypes.GRAVITATION_SHOCKBURST_RECIPES)
            .recipeType(GTLRecipeTypes.ELECTRIC_IMPLOSION_COMPRESSOR_RECIPES)
            .tooltips(Component.translatable("gtceu.multiblock.only.laser.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.perfect_oc"))
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.gravitation_shockburst"), Component.translatable("gtceu.electric_implosion_compressor")))
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(GTLBlocks.CREATE_CASING)
            .pattern(definition -> MultiBlockMachineBStructure.GRAVITATION_SHOCKBURST
                    .where("a", Predicates.blocks(GTLBlocks.CREATE_CASING.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(3))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(3))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1)))
                    .where("b", Predicates.blocks(GTLBlocks.INFINITY_GLASS.get()))
                    .where("c", Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.block, GTLMaterials.Infinity)))
                    .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                    .where(" ", Predicates.any())
                    .build())
            .workableCasingRenderer(GTLCore.id("block/create_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public final static MultiblockMachineDefinition DISSOLVING_TANK = REGISTRATE.multiblock("dissolving_tank", WorkableElectricMultiblockMachine::new)
            .langValue("Dissolving Tank")
            .tooltips(Component.translatable("gtceu.multiblock.dissolving_tank.tooltip.0"))
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("tooltip.gtlcore.structure.source", "GTNH"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.dissolution_treatment")))
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .rotationState(RotationState.ALL)
            .recipeTypes(GTLRecipeTypes.DISSOLUTION_TREATMENT)
            .recipeModifier(GTLRecipeModifiers::dissolvingTankOverclock)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> MultiBlockMachineBStructure.DISSOLVING_TANK
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('X', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where('K', Predicates.blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                    .where('O', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('G', Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"), GTCEu.id("block/multiblock/generator/large_gas_turbine"))
            .register();

    public final static MultiblockMachineDefinition DIGESTION_TANK = REGISTRATE.multiblock("digestion_tank", CoilWorkableElectricMultiblockMachine::new)
            .langValue("Digestion Tank")
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("tooltip.gtlcore.structure.source", "GTNH"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.digestion_treatment")))
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .rotationState(RotationState.ALL)
            .recipeTypes(GTLRecipeTypes.DIGESTION_TREATMENT)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> MultiBlockMachineBStructure.DIGESTION_TANK
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('X', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where('K', Predicates.blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                    .where('Y', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where('M', Predicates.heatingCoils())
                    .where('O', Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where('A', Predicates.air())
                    .where('#', Predicates.any())
                    .build())
            .beforeWorking((machine, recipe) -> machine instanceof CoilWorkableElectricMultiblockMachine coilMachine && coilMachine.getCoilType().getCoilTemperature() >= recipe.data.getInt("ebf_temp"))
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"), GTCEu.id("block/multiblock/gcym/large_maceration_tower"))
            .register();

    public final static MultiblockMachineDefinition WOOD_DISTILLATION = REGISTRATE.multiblock("wood_distillation", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTLRecipeTypes.WOOD_DISTILLATION_RECIPES)
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.wood_distillation")))
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
            .appearanceBlock(GTBlocks.CASING_INVAR_HEATPROOF)
            .pattern(definition -> MultiBlockMachineBStructure.WOOD_DISTILLATION
                    .where("M", controller(blocks(definition.get())))
                    .where("I", blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                    .where("N", blocks(GTBlocks.HERMETIC_CASING_HV.get()))
                    .where("J", blocks(GTBlocks.CASING_STAINLESS_EVAPORATION.get()))
                    .where("E", blocks(GTBlocks.FILTER_CASING.get()))
                    .where("L", blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(6).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where("B", Predicates.blocks(GCyMBlocks.HEAT_VENT.get()))
                    .where("H", Predicates.abilities(PartAbility.MUFFLER))
                    .where("D", blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where("G", blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .where("K", blocks(GTBlocks.FIREBOX_STEEL.get()))
                    .where("C", Predicates.blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.StainlessSteel)))
                    .where("A", blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                    .where(" ", Predicates.any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_heatproof"), GTCEu.id("block/multiblock/electric_blast_furnace"))
            .register();

    public final static MultiblockMachineDefinition DESULFURIZER = REGISTRATE.multiblock("desulfurizer", WorkableElectricParallelHatchMultipleRecipesMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTLRecipeTypes.DESULFURIZER_RECIPES)
            .tooltips(Component.translatable("gtceu.machine.multiple_recipes.tooltip"))
            .tooltips(Component.translatable("gtceu.multiblock.parallelizable.tooltip"))
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_1.tooltip",
                    Component.translatable("gtceu.desulfurizer")))
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> MultiBlockMachineBStructure.DESULFURIZER
                    .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("I", blocks(GTBlocks.CASING_INVAR_HEATPROOF.get()))
                    .where("X", Predicates.blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                    .where("P", Predicates.blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                    .where("G", Predicates.blocks(GTBlocks.HERMETIC_CASING_MV.get()))
                    .where("D", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                            .setMinGlobalLimited(36)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true)))
                    .where("L", Predicates.blocks(GTBlocks.COIL_KANTHAL.get()))
                    .where("C", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where(" ", Predicates.any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"), GTCEu.id("block/multiblock/large_chemical_reactor"))
            .register();

    public final static MultiblockMachineDefinition PRIMITIVE_VOID_ORE = ConfigHolder.INSTANCE.content.enablePrimitiveVoidOre ?
            REGISTRATE.multiblock("primitive_void_ore", PrimitiveOreMachine::new)
                    .langValue("Primitive Void Ore")
                    .tooltips(Component.translatable("tooltip.gtlcore.primitive_void_ore_random_output"))
                    .tooltips(Component.translatable("tooltip.gtlcore.supports_dimensions"))
                    .tooltipBuilder(GTLMachines.GTL_ADD)
                    .rotationState(RotationState.ALL)
                    .recipeType(GTLRecipeTypes.PRIMITIVE_VOID_ORE_RECIPES)
                    .appearanceBlock(() -> Blocks.DIRT)
                    .pattern(definition -> MultiBlockMachineBStructure.PRIMITIVE_VOID_ORE
                            .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                            .where('X',
                                    Predicates.blocks(Blocks.DIRT)
                                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)))
                            .where('A', Predicates.air())
                            .build())
                    .workableCasingRenderer(new ResourceLocation("minecraft:block/dirt"),
                            GTCEu.id("block/multiblock/gcym/large_extractor"))
                    .register() :
            null;

    /**
     * 武德之眼。结构与鸿蒙之眼逐格一致（33×33×33），方块按 TST 原版：
     * 青铜砖机壳 + 原版砖块/木板/青金石块/书架/錾制石砖。
     */
    public final static MultiblockMachineDefinition EYE_OF_WOOD = REGISTRATE
            .multiblock("eye_of_wood", EyeOfWoodMachine::new)
            .langValue("Eye of Wood")
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(GTLRecipeTypes.EYE_OF_WOOD_RECIPES)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .tooltips(eyeOfWoodTooltips())
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .pattern(definition -> AdvancedMultiBlockMachineStructureA.EYE_OF_HARMONY
                    .where('~', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('A', Predicates.blocks(Blocks.BRICKS))
                    .where('B', Predicates.blocks(GTBlocks.CASING_BRONZE_BRICKS.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS)))
                    .where('D', Predicates.blockTag(BlockTags.PLANKS))
                    .where('E', Predicates.blocks(Blocks.LAPIS_BLOCK))
                    .where('F', Predicates.blocks(Blocks.BOOKSHELF))
                    .where('G', Predicates.blocks(Blocks.CHISELED_STONE_BRICKS))
                    .where(" ", Predicates.any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public final static MultiblockMachineDefinition DREAM_ELEVATOR = REGISTRATE
            .multiblock("dream_elevator", DreamElevatorMachine::new)
            .langValue("Dream Elevator")
            .rotationState(RotationState.ALL)
            .recipeType(GTLRecipeTypes.DREAM_ELEVATOR_RECIPES)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .tooltips(Component.translatable("gtlcore.machine.dream_elevator.tooltip.0"))
            .tooltips(Component.translatable("gtlcore.machine.dream_elevator.tooltip.1"))
            .tooltipBuilder(GTLMachines.GTL_ADD)
            .pattern(definition -> DreamElevatorStructure.DREAM_ELEVATOR
                    .where("~", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("A", Predicates.blocks(Blocks.STONE_BRICKS))
                    .where("B", Predicates.blocks(Blocks.BRICKS))
                    .where("P", Predicates.blocks(Blocks.POLISHED_BASALT))
                    .where("W", Predicates.blocks(
                            ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TreatedWood)))
                    .where("R", Predicates.blocks(Blocks.REDSTONE_BLOCK))
                    .where("C", GTLPredicates.tierCasings(DreamElevatorMachine.RAIL_TIER_BLOCKS,
                            DreamElevatorMachine.RAIL_TIER_KEY))
                    .where("S", dreamSteamCasing())
                    .where("X", dreamSteamCasing())
                    .where("G", dreamSteamCasing())
                    .where("I", dreamSteamCasing())
                    .where("-", Predicates.air())
                    .where(" ", Predicates.any())
                    .build())
            .renderer(DreamElevatorRenderer::new)
            .hasTESR(true)
            .register();

    public final static MultiblockMachineDefinition LARGE_FRAGMENT_WORLD_COLLECTION_MACHINE = ConfigHolder.INSTANCE.content.enableSkyBlokeMode ?
            REGISTRATE.multiblock("large_fragment_world_collection_machine", WorkableElectricMultiblockMachine::new)
                    .langValue("Large fragment world collection machine")
                    .tooltips(Component.translatable("gtlcore.machine.large_fragment_world_collection.sky_block_mode"))
                    .tooltips(Component.translatable("gtlcore.machine.large_fragment_world_collection.energy_multiplier"))
                    .tooltips(Component.translatable("gtlcore.machine.large_fragment_world_collection.duration_multiplier"))
                    .tooltips(Component.translatable("gtlcore.machine.large_fragment_world_collection.max_parallel"))
                    .tooltipBuilder(GTLMachines.GTL_ADD)
                    .recipeModifiers(
                            (machine, recipe, params, result) -> GTLRecipeModifiers.reduction(machine, recipe, 256, 0.25),
                            (machine, recipe, params, result) -> GTRecipeModifiers.accurateParallel(machine, recipe, 64, false).getFirst())
                    .rotationState(RotationState.ALL)
                    .recipeType(GTLRecipeTypes.FRAGMENT_WORLD_COLLECTION)
                    .appearanceBlock(GTBlocks.CASING_TITANIUM_STABLE)
                    .pattern(definition -> MultiBlockMachineBStructure.LARGE_FRAGMENT_WORLD_COLLECTION_MACHINE
                            .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                            .where('X', Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                            .where('I', Predicates.abilities(PartAbility.IMPORT_ITEMS))
                            .where('O', Predicates.abilities(PartAbility.EXPORT_ITEMS))
                            .where('A', Predicates.any())
                            .build())
                    .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
                            GTCEu.id("block/multiblock/gcym/large_extractor"))
                    .register() :
            null;
}
