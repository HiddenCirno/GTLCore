package org.gtlcore.gtlcore.integration.ae2.pattern;

import org.gtlcore.gtlcore.GTLCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public final class PatternEncoderMetadata {

    private static final String ROOT_KEY = GTLCore.MOD_ID;
    private static final String ENCODER_ID_KEY = "patternEncoderId";
    private static final String ENCODER_NAME_KEY = "patternEncoderName";
    private static final String ENCODED_AT_KEY = "patternEncodedAt";
    private static final String LAST_MODIFIER_NAME_KEY = "patternLastModifierName";
    private static final String LAST_MODIFIED_AT_KEY = "patternLastModifiedAt";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private PatternEncoderMetadata() {}

    public static void writeEncoder(ItemStack patternStack, UUID encoderId, String encoderName) {
        if (patternStack.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        CompoundTag tag = patternStack.getOrCreateTag();
        CompoundTag gtlcoreTag = getOrCreateRoot(tag);
        gtlcoreTag.putUUID(ENCODER_ID_KEY, encoderId);
        if (encoderName != null && !encoderName.isBlank()) {
            gtlcoreTag.putString(ENCODER_NAME_KEY, encoderName);
            gtlcoreTag.putString(LAST_MODIFIER_NAME_KEY, encoderName);
        } else {
            gtlcoreTag.remove(ENCODER_NAME_KEY);
            gtlcoreTag.remove(LAST_MODIFIER_NAME_KEY);
        }
        gtlcoreTag.putLong(ENCODED_AT_KEY, now);
        gtlcoreTag.putLong(LAST_MODIFIED_AT_KEY, now);
        tag.put(ROOT_KEY, gtlcoreTag);
    }

    /** Records a modification without touching the original encoder or encoding time. */
    public static void touchModified(ItemStack patternStack, String modifierName) {
        if (patternStack.isEmpty() || modifierName == null || modifierName.isBlank()) {
            return;
        }

        CompoundTag tag = patternStack.getOrCreateTag();
        CompoundTag gtlcoreTag = getOrCreateRoot(tag);
        gtlcoreTag.putString(LAST_MODIFIER_NAME_KEY, modifierName);
        gtlcoreTag.putLong(LAST_MODIFIED_AT_KEY, System.currentTimeMillis());
        tag.put(ROOT_KEY, gtlcoreTag);
    }

    /** Copies the whole GTLCore metadata compound, including fields added in the future. */
    public static void copyMetadata(ItemStack source, ItemStack target) {
        if (source.isEmpty() || target.isEmpty()) {
            return;
        }
        CompoundTag sourceTag = source.getTag();
        if (sourceTag == null || !sourceTag.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return;
        }
        target.getOrCreateTag().put(ROOT_KEY, sourceTag.getCompound(ROOT_KEY).copy());
    }

    public static Optional<Encoder> readEncoder(ItemStack patternStack) {
        CompoundTag tag = patternStack.getTag();
        if (tag == null || !tag.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        CompoundTag gtlcoreTag = tag.getCompound(ROOT_KEY);
        if (!gtlcoreTag.hasUUID(ENCODER_ID_KEY)) {
            return Optional.empty();
        }
        String encoderName = gtlcoreTag.contains(ENCODER_NAME_KEY, Tag.TAG_STRING) ?
                gtlcoreTag.getString(ENCODER_NAME_KEY) : "";
        String lastModifierName = gtlcoreTag.contains(LAST_MODIFIER_NAME_KEY, Tag.TAG_STRING) ?
                gtlcoreTag.getString(LAST_MODIFIER_NAME_KEY) : "";
        return Optional.of(new Encoder(
                gtlcoreTag.getUUID(ENCODER_ID_KEY),
                encoderName,
                gtlcoreTag.getLong(ENCODED_AT_KEY),
                lastModifierName,
                gtlcoreTag.getLong(LAST_MODIFIED_AT_KEY)));
    }

    public static String formatTimestamp(long epochMillis) {
        if (epochMillis <= 0) {
            return "";
        }
        return TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()));
    }

    private static CompoundTag getOrCreateRoot(CompoundTag tag) {
        return tag.contains(ROOT_KEY, Tag.TAG_COMPOUND) ? tag.getCompound(ROOT_KEY) : new CompoundTag();
    }

    static void removeEncoder(ItemStack patternStack) {
        CompoundTag tag = patternStack.getTag();
        if (tag == null || !tag.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag gtlcoreTag = tag.getCompound(ROOT_KEY);
        gtlcoreTag.remove(ENCODER_ID_KEY);
        gtlcoreTag.remove(ENCODER_NAME_KEY);
        gtlcoreTag.remove(ENCODED_AT_KEY);
        gtlcoreTag.remove(LAST_MODIFIER_NAME_KEY);
        gtlcoreTag.remove(LAST_MODIFIED_AT_KEY);
        if (gtlcoreTag.isEmpty()) {
            tag.remove(ROOT_KEY);
        } else {
            tag.put(ROOT_KEY, gtlcoreTag);
        }
    }

    public record Encoder(UUID id, String name, long encodedAt, String lastModifierName, long lastModifiedAt) {

        public String displayName() {
            return name.isBlank() ? id.toString() : name;
        }

        public String lastModifierDisplayName() {
            return lastModifierName.isBlank() ? displayName() : lastModifierName;
        }
    }
}
