package org.gtlcore.gtlcore.mixin.gtceu;

import org.gtlcore.gtlcore.integration.ae2.pattern.VirtualIngredientEncoding;
import org.gtlcore.gtlcore.mixin.emi.GTEmiRecipeAccessor;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.emi.recipe.Ae2PatternTerminalHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;

import net.minecraft.client.gui.screens.Screen;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.api.recipe.EmiRecipe;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.Set;

/**
 * GTCEu registers its own EMI pattern-terminal handler for the standard AE2 and AE2WTLib wireless pattern
 * terminals, so the AE2 {@code EmiEncodePatternHandler} (and its mixin) is not used there. This is the actual
 * transfer path for those terminals.
 */
@Mixin(Ae2PatternTerminalHandler.class)
public abstract class GtceuEmiPatternTerminalHandlerMixin {

    @ModifyArg(
               method = "craft(Ldev/emi/emi/api/recipe/EmiRecipe;Ldev/emi/emi/api/recipe/handler/EmiCraftContext;)Z",
               at = @At(
                        value = "INVOKE",
                        target = "Lappeng/integration/modules/jeirei/EncodingHelper;encodeProcessingRecipe(Lappeng/menu/me/items/PatternEncodingTermMenu;Ljava/util/List;Ljava/util/List;)V"),
               index = 1,
               remap = false,
               require = 1)
    private List<List<GenericStack>> gtlcore$virtualiseNotConsumedInputs(
                                                                        List<List<GenericStack>> genericIngredients,
                                                                        @Local(argsOnly = true) EmiRecipe emiRecipe) {
        if (!Screen.hasControlDown()) return genericIngredients;
        GTRecipe recipe = gtlcore$getRecipe(emiRecipe);
        if (recipe == null) return genericIngredients;

        Set<AEKey> notConsumed = VirtualIngredientEncoding.notConsumedKeys(recipe);
        return notConsumed.isEmpty() ? genericIngredients :
                VirtualIngredientEncoding.wrapNotConsumedInputs(genericIngredients, notConsumed);
    }

    @Nullable
    private static GTRecipe gtlcore$getRecipe(EmiRecipe emiRecipe) {
        return emiRecipe instanceof GTEmiRecipe gtEmiRecipe ?
                ((GTEmiRecipeAccessor) gtEmiRecipe).gtlcore$getRecipe() : null;
    }
}
