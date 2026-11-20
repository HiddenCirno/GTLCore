package org.gtlcore.gtlcore.integration.ae2.crafting;

import net.minecraft.network.FriendlyByteBuf;

import appeng.menu.guisync.PacketWritable;

import java.util.HashMap;
import java.util.Map;

/**
 * CPU 列表的协处理器数量（按 CPU serial 索引）。
 *
 * <p>单独作为 {@code @GuiSync} 字段同步，避免向 AE2 的
 * {@code CraftingCpuListEntry} 追加序列化字段而与其它附属模组冲突。</p>
 */
public record GtlCpuCoProcessorData(Map<Integer, Long> coProcessors) implements PacketWritable {

    public static final GtlCpuCoProcessorData EMPTY = new GtlCpuCoProcessorData(Map.of());

    public GtlCpuCoProcessorData(FriendlyByteBuf data) {
        this(read(data));
    }

    private static Map<Integer, Long> read(FriendlyByteBuf data) {
        int count = data.readVarInt();
        Map<Integer, Long> result = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            result.put(data.readVarInt(), data.readVarLong());
        }
        return Map.copyOf(result);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf data) {
        data.writeVarInt(this.coProcessors.size());
        for (Map.Entry<Integer, Long> entry : this.coProcessors.entrySet()) {
            data.writeVarInt(entry.getKey());
            data.writeVarLong(entry.getValue());
        }
    }
}
