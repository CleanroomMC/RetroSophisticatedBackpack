package com.cleanroommc.retrosophisticatedbackpacks.backpack;

import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.CraftingUpgradeWrapper;
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.IToggleable;
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper;
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.UpgradeWrapper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.jetbrains.annotations.NotNull;

public final class Capabilities {
    // Implementation-specific capabilities
    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(BackpackWrapper.class)
    public static final @NotNull Capability<BackpackWrapper> BACKPACK_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(UpgradeWrapper.class)
    public static final @NotNull Capability<CraftingUpgradeWrapper> CRAFTING_ITEM_HANDLER_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(PickupUpgradeWrapper.class)
    public static final @NotNull Capability<PickupUpgradeWrapper> PICKUP_UPGRADE_CAPABILITY = null;

    // Abstract capabilities
    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(IToggleable.class)
    public static final @NotNull Capability<IToggleable> TOGGLEABLE_CAPABILITY = null;
}
