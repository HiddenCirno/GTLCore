package org.gtlcore.gtlcore.mixin.ae2.gui;

import org.gtlcore.gtlcore.integration.ae2.crafting.GtlCpuCoProcessorData;
import org.gtlcore.gtlcore.integration.ae2.crafting.ICraftingCpuLongCoProcessorView;
import org.gtlcore.gtlcore.integration.ae2.crafting.transfinite.TransfiniteCraftingCPU;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.crafting.CraftingStatusMenu;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mixin(CraftingStatusMenu.class)
public class CraftingStatusMenuMixin implements ICraftingCpuLongCoProcessorView {

    @Shadow(remap = false)
    private ImmutableSet<ICraftingCPU> lastCpuSet;

    /**
     * 与 CPU 列表分开同步，避免修改 CraftingCpuListEntry 的包格式。
     * key 120 避开 AE2 自身和 GTLCore 已使用的 0/1/8/9/10/11。
     */
    @Unique
    @GuiSync(120)
    private GtlCpuCoProcessorData gtlcore$longCoProcessors = GtlCpuCoProcessorData.EMPTY;

    @Inject(method = "createCpuList",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;sort(Ljava/util/Comparator;)V"),
            remap = false)
    private void gtlcore$collectLongCoProcessors(
                                                CallbackInfoReturnable<CraftingStatusMenu.CraftingCpuList> cir,
                                                @Local(name = "entries") ArrayList<CraftingStatusMenu.CraftingCpuListEntry> entries) {
        Iterator<ICraftingCPU> cpus = this.lastCpuSet.iterator();
        Map<Integer, Long> coProcessors = new HashMap<>(entries.size());
        for (CraftingStatusMenu.CraftingCpuListEntry entry : entries) {
            if (!cpus.hasNext()) {
                break;
            }
            ICraftingCPU cpu = cpus.next();
            long value = cpu instanceof TransfiniteCraftingCPU transfiniteCpu ?
                    transfiniteCpu.getLongCoProcessors() : cpu.getCoProcessors();
            coProcessors.put(entry.serial(), value);
        }
        this.gtlcore$longCoProcessors = new GtlCpuCoProcessorData(Map.copyOf(coProcessors));
    }

    @Override
    public long gtlcore$getLongCoProcessors(int serial) {
        return this.gtlcore$longCoProcessors.coProcessors().getOrDefault(serial, 0L);
    }
}
