package com.wlsnb.arrowslot.client;

import com.wlsnb.arrowslot.network.SyncRigWeaponPayload;

public final class RigWeaponClientState {
    private static boolean active;
    private static boolean gMode;
    private static int index;

    private RigWeaponClientState() {
    }

    public static void accept(SyncRigWeaponPayload payload) {
        active = payload.active();
        gMode = payload.gMode();
        index = Math.max(0, payload.index());
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isGMode() {
        return active && gMode;
    }

    public static int index() {
        return index;
    }

    public static void reset() {
        active = false;
        gMode = false;
        index = 0;
    }
}
