package com.birtek.cashew.database;

public final class CasesimInvStats {
    private final int casesOpened;
    private final int collectionsOpened;
    private final int capsulesOpened;
    private final int itemsInInventory;
    private boolean isPublic;

    public CasesimInvStats(int casesOpened, int collectionsOpened, int capsulesOpened, int itemsInInventory,
                           boolean isPublic) {
        this.casesOpened = casesOpened;
        this.collectionsOpened = collectionsOpened;
        this.capsulesOpened = capsulesOpened;
        this.itemsInInventory = itemsInInventory;
        this.isPublic = isPublic;
    }

    public int casesOpened() {
        return casesOpened;
    }

    public int collectionsOpened() {
        return collectionsOpened;
    }

    public int capsulesOpened() {
        return capsulesOpened;
    }

    public int itemsInInventory() {
        return itemsInInventory;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}
