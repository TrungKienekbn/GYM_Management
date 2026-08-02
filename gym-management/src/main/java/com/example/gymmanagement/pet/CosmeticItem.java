package com.example.gymmanagement.pet;

public enum CosmeticItem {
    SHIRT_ORANGE(CosmeticSlot.SHIRT, "Áo cam", "#E8641E", true),
    SHIRT_BLUE(CosmeticSlot.SHIRT,   "Áo xanh nước biển", "#1E5FBF", false),
    SHIRT_RED(CosmeticSlot.SHIRT,    "Áo đỏ", "#C0392B", false),

    PANTS_ORANGE(CosmeticSlot.PANTS, "Quần cam", "#B84A12", true),
    PANTS_BLUE(CosmeticSlot.PANTS,   "Quần xanh nước biển", "#164A94", false),
    PANTS_RED(CosmeticSlot.PANTS,    "Quần đỏ", "#A5281F", false),

    HAIR_YELLOW(CosmeticSlot.HAIR, "Tóc vàng", "#F5C400", true),
    HAIR_BLUE(CosmeticSlot.HAIR,   "Tóc xanh nước biển", "#2461C7", false),
    HAIR_SILVER(CosmeticSlot.HAIR, "Tóc bạc", "#C0C0C0", false),
    HAIR_RED(CosmeticSlot.HAIR,    "Tóc đỏ", "#B22222", false);

    public static final long PRICE = 5000L;

    private final CosmeticSlot slot;
    private final String displayName;
    private final String colorHex;
    private final boolean free;

    CosmeticItem(CosmeticSlot slot, String displayName, String colorHex, boolean free) {
        this.slot = slot;
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.free = free;
    }

    public CosmeticSlot getSlot() { return slot; }
    public String getDisplayName() { return displayName; }
    public String getColorHex() { return colorHex; }
    public boolean isFree() { return free; }

    public static CosmeticItem fromCode(String code) {
        if (code == null) return null;
        try {
            return CosmeticItem.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Mã trang phục không hợp lệ: " + code);
        }
    }

    public static CosmeticItem defaultOf(CosmeticSlot slot) {
        for (CosmeticItem i : values()) {
            if (i.slot == slot && i.free) return i;
        }
        throw new IllegalStateException("Thiếu item mặc định cho slot " + slot);
    }
}