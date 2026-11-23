package org.gtlcore.gtlcore.mixin.ldlib;

import org.gtlcore.gtlcore.GTLCore;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.lowdragmc.lowdraglib.async.AsyncThreadData;
import com.lowdragmc.lowdraglib.async.IAsyncLogic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(AsyncThreadData.class)
public abstract class AsyncThreadDataMixin {

    @Unique
    private static final Set<String> gtlcore$asyncSyncFailures = ConcurrentHashMap.newKeySet();

    @ModifyArg(method = "createExecutorService",
               at = @At(value = "INVOKE",
                        target = "Ljava/util/concurrent/ScheduledExecutorService;scheduleAtFixedRate(Ljava/lang/Runnable;JJLjava/util/concurrent/TimeUnit;)Ljava/util/concurrent/ScheduledFuture;"),
               index = 2,
               remap = false)
    private long modifyDelay(long delay) {
        return 500L;
    }

    /**
     * LDLib 的 searchingTask 把整个 for 循环包在一个 try/catch 里。任意一个
     * IAsyncLogic.asyncTick 抛异常，这一轮后面所有 block entity 的同步都会被跳过。
     *
     * <p>这里把每次 asyncTick 单独隔离，避免一个坏掉的 AE2/LDLib 字段拖死整批仓室同步。</p>
     */
    @Redirect(method = "searchingTask",
              at = @At(value = "INVOKE",
                       target = "Lcom/lowdragmc/lowdraglib/async/IAsyncLogic;asyncTick(J)V"),
              remap = false,
              require = 1)
    private void gtlcore$isolateAsyncSyncFailure(IAsyncLogic logic, long periodID) {
        try {
            logic.asyncTick(periodID);
        } catch (VirtualMachineError error) {
            throw error;
        } catch (Throwable throwable) {
            String detail = logic.getClass().getName();
            if (logic instanceof IMachineBlockEntity machineHolder) {
                var machine = machineHolder.getMetaMachine();
                detail = "%s at %s (%s)".formatted(
                        machine.getDefinition().getId(),
                        machine.getPos(),
                        machine.getClass().getSimpleName());
            }
            if (gtlcore$asyncSyncFailures.add(detail)) {
                GTLCore.LOGGER.error("Isolated async sync failure in {}; later block entities will still sync",
                        detail, throwable);
            } else {
                GTLCore.LOGGER.debug("Isolated async sync failure in {}: {}", detail, throwable.toString());
            }
        }
    }
}
