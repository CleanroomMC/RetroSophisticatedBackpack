package com.cleanroommc.retrosophisticatedbackpacks.capability;

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*;
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherUpgradeWrapper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("DataFlowIssue")
public final class Capabilities {
    // Implementation-specific capabilities
    @CapabilityInject(BackpackWrapper.class)
    public static final @NotNull Capability<BackpackWrapper> BACKPACK_CAPABILITY = null;

    @CapabilityInject(CraftingUpgradeWrapper.class)
    public static final @NotNull Capability<CraftingUpgradeWrapper> CRAFTING_ITEM_HANDLER_CAPABILITY = null;

    @CapabilityInject(PickupUpgradeWrapper.class)
    public static final @NotNull Capability<PickupUpgradeWrapper> PICKUP_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedPickupUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedPickupUpgradeWrapper> ADVANCED_PICKUP_UPGRADE_CAPABILITY = null;

    @CapabilityInject(FeedingUpgradeWrapper.class)
    public static final @NotNull Capability<FeedingUpgradeWrapper> FEEDING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedFeedingUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedFeedingUpgradeWrapper> ADVANCED_FEEDING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(DepositUpgradeWrapper.class)
    public static final @NotNull Capability<DepositUpgradeWrapper> DEPOSIT_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedDepositUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedDepositUpgradeWrapper> ADVANCED_DEPOSIT_UPGRADE_CAPABILITY = null;

    @CapabilityInject(RestockUpgradeWrapper.class)
    public static final @NotNull Capability<RestockUpgradeWrapper> RESTOCK_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedRestockUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedRestockUpgradeWrapper> ADVANCED_RESTOCK_UPGRADE_CAPABILITY = null;

    @CapabilityInject(FilterUpgradeWrapper.class)
    public static final @NotNull Capability<FilterUpgradeWrapper> FILTER_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedFilterUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedFilterUpgradeWrapper> ADVANCED_FILTER_UPGRADE_WRAPPER_CAPABILITY = null;

    @CapabilityInject(MagnetUpgradeWrapper.class)
    public static final @NotNull Capability<MagnetUpgradeWrapper> MAGNET_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedMagnetUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedMagnetUpgradeWrapper> ADVANCED_MAGNET_UPGRADE_CAPABILITY = null;

    @CapabilityInject(VoidUpgradeWrapper.class)
    public static final @NotNull Capability<VoidUpgradeWrapper> VOID_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedVoidUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedVoidUpgradeWrapper> ADVANCED_VOID_UPGRADE_CAPABILITY = null;

    @CapabilityInject(RefillUpgradeWrapper.class)
    public static final @NotNull Capability<RefillUpgradeWrapper> REFILL_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedRefillUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedRefillUpgradeWrapper> ADVANCED_REFILL_UPGRADE_CAPABILITY = null;

    @CapabilityInject(CompactingUpgradeWrapper.class)
    public static final @NotNull Capability<CompactingUpgradeWrapper> COMPACTING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedCompactingUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedCompactingUpgradeWrapper> ADVANCED_COMPACTING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(EverlastingUpgradeWrapper.class)
    public static final @NotNull Capability<EverlastingUpgradeWrapper> EVERLASTING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(ToolSwapperUpgradeWrapper.class)
    public static final @NotNull Capability<ToolSwapperUpgradeWrapper> TOOL_SWAPPER_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedToolSwapperUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedToolSwapperUpgradeWrapper> ADVANCED_TOOL_SWAPPER_UPGRADE_CAPABILITY = null;

    @CapabilityInject(TankUpgradeWrapper.class)
    public static final @NotNull Capability<TankUpgradeWrapper> TANK_UPGRADE_CAPABILITY = null;

    @CapabilityInject(JukeboxUpgradeWrapper.class)
    public static final @NotNull Capability<JukeboxUpgradeWrapper> JUKEBOX_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedJukeboxUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedJukeboxUpgradeWrapper> ADVANCED_JUKEBOX_UPGRADE_CAPABILITY = null;

    @CapabilityInject(PumpUpgradeWrapper.class)
    public static final @NotNull Capability<PumpUpgradeWrapper> PUMP_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AdvancedPumpUpgradeWrapper.class)
    public static final @NotNull Capability<AdvancedPumpUpgradeWrapper> ADVANCED_PUMP_UPGRADE_CAPABILITY = null;

    @CapabilityInject(BatteryUpgradeWrapper.class)
    public static final @NotNull Capability<BatteryUpgradeWrapper> BATTERY_UPGRADE_CAPABILITY = null;

    @CapabilityInject(AnvilUpgradeWrapper.class)
    public static final @NotNull Capability<AnvilUpgradeWrapper> ANVIL_UPGRADE_CAPABILITY = null;

    @CapabilityInject(MobCatcherUpgradeWrapper.class)
    public static final @NotNull Capability<MobCatcherUpgradeWrapper> MOB_CATCHER_UPGRADE_CAPABILITY = null;

    // Abstract capabilities
    @CapabilityInject(UpgradeWrapper.class)
    public static final @NotNull Capability<UpgradeWrapper<?>> UPGRADE_CAPABILITY = null;

    @CapabilityInject(IToggleable.class)
    public static final @NotNull Capability<IToggleable> TOGGLEABLE_CAPABILITY = null;

    @CapabilityInject(IBasicFilterable.class)
    public static final @NotNull Capability<IBasicFilterable> BASIC_FILTERABLE_CAPABILITY = null;

    @CapabilityInject(IAdvancedFilterable.class)
    public static final @NotNull Capability<IAdvancedFilterable> ADVANCED_FILTERABLE_CAPABILITY = null;

    @CapabilityInject(IPickupUpgrade.class)
    public static final @NotNull Capability<IPickupUpgrade> IPICKUP_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IFeedingUpgrade.class)
    public static final @NotNull Capability<IFeedingUpgrade> IFEEDING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IDepositUpgrade.class)
    public static final @NotNull Capability<IDepositUpgrade> IDEPOSIT_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IRestockUpgrade.class)
    public static final @NotNull Capability<IRestockUpgrade> IRESTOCK_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IFilterUpgrade.class)
    public static final @NotNull Capability<IFilterUpgrade> IFILTER_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IMagnetUpgrade.class)
    public static final @NotNull Capability<IMagnetUpgrade> IMAGNET_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IVoidUpgrade.class)
    public static final @NotNull Capability<IVoidUpgrade> IVOID_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IRefillUpgrade.class)
    public static final @NotNull Capability<IRefillUpgrade> IREFILL_UPGRADE_CAPABILITY = null;

    @CapabilityInject(ICompactingUpgrade.class)
    public static final @NotNull Capability<ICompactingUpgrade> ICOMPACTING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IEverlastingUpgrade.class)
    public static final @NotNull Capability<IEverlastingUpgrade> IEVERLASTING_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IToolSwapperUpgrade.class)
    public static final @NotNull Capability<IToolSwapperUpgrade> ITOOL_SWAPPER_UPGRADE_CAPABILITY = null;

    @CapabilityInject(ITankUpgrade.class)
    public static final @NotNull Capability<ITankUpgrade> ITANK_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IJukeboxUpgrade.class)
    public static final @NotNull Capability<IJukeboxUpgrade> IJUKEBOX_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IPumpUpgrade.class)
    public static final @NotNull Capability<IPumpUpgrade> IPUMP_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IBatteryUpgrade.class)
    public static final @NotNull Capability<IBatteryUpgrade> IBATTERY_UPGRADE_CAPABILITY = null;

    @CapabilityInject(IAnvilUpgrade.class)
    public static final @NotNull Capability<IAnvilUpgrade> IANVIL_UPGRADE_CAPABILITY = null;
}
