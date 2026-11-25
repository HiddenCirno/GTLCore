package org.gtlcore.gtlcore.mixin.emi;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the backing GT recipe of an EMI recipe so the EMI pattern transfer can identify non-consumed inputs.
 */
@Mixin(value = GTEmiRecipe.class, remap = false)
public interface GTEmiRecipeAccessor {

    @Accessor("recipe")
    GTRecipe gtlcore$getRecipe();
}
