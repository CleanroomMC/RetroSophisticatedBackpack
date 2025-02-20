package com.cleanroommc.retrosophisticatedbackpacks.capability;

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*;
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

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(AdvancedPickupUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedPickupUpgradeWrapper> ADVANCED_PICKUP_UPGRADE_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(PickupUpgradeWrapper.class)
    public static final @NotNull Capability<FeedingUpgradeWrapper> FEEDING_UPGRADE_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(AdvancedPickupUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedFeedingUpgradeWrapper> ADVANCED_FEEDING_UPGRADE_CAPABILITY = null;

    // Abstract capabilities
    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(IToggleable.class)
    public static final @NotNull Capability<IToggleable> TOGGLEABLE_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(IBasicFilterable.class)
    public static final @NotNull Capability<IBasicFilterable> BASIC_FILTERABLE_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(IAdvanceFilterable.class)
    public static final @NotNull Capability<IAdvanceFilterable> ADVANCE_FILTERABLE_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(IBasicFilterable.class)
    public static final @NotNull Capability<IPickupUpgrade> IPICKUP_UPGRADE_CAPABILITY = null;

    @SuppressWarnings("DataFlowIssue")
    @CapabilityInject(IFeedingUpgrade.class)
    public static final @NotNull Capability<IFeedingUpgrade> IFEEDING_UPGRADE_CAPABILITY = null;
}
