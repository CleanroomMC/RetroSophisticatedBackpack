package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagEnd
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable
import java.util.*

object CapabilityHandler {
    val BACKPACK_INVENTORY_CACHE: Object2ObjectMap<UUID, BackpackWrapper> = Object2ObjectOpenHashMap()

    fun register() {
        val instance = CapabilityManager.INSTANCE

        instance.register(
            BackpackWrapper::class.java,
            object : CapabilityStorageProvider<BackpackWrapper>() {
                override fun readNBT(
                    capability: Capability<BackpackWrapper>,
                    instance: BackpackWrapper,
                    side: EnumFacing?,
                    nbt: NBTBase
                ) {
                    super.readNBT(capability, instance, side, nbt)
                    cacheBackpackInventory(instance)
                }
            },
            ::BackpackWrapper
        )

        instance.register(
            CraftingUpgradeWrapper::class.java,
            CapabilityStorageProvider<CraftingUpgradeWrapper>(),
            ::CraftingUpgradeWrapper
        )

        instance.register(
            PickupUpgradeWrapper::class.java,
            CapabilityStorageProvider<PickupUpgradeWrapper>(),
            ::PickupUpgradeWrapper
        )

        instance.register(
            AdvancedPickupUpgradeWrapper::class.java,
            CapabilityStorageProvider<AdvancedPickupUpgradeWrapper>(),
            ::AdvancedPickupUpgradeWrapper
        )

        instance.register(
            FeedingUpgradeWrapper::class.java,
            CapabilityStorageProvider<FeedingUpgradeWrapper>(),
            ::FeedingUpgradeWrapper
        )

        instance.register(
            AdvancedFeedingUpgradeWrapper::class.java,
            CapabilityStorageProvider<AdvancedFeedingUpgradeWrapper>(),
            ::AdvancedFeedingUpgradeWrapper
        )

        instance.register(
            DepositUpgradeWrapper::class.java,
            CapabilityStorageProvider<DepositUpgradeWrapper>(),
            ::DepositUpgradeWrapper
        )

        instance.register(
            AdvancedDepositUpgradeWrapper::class.java,
            CapabilityStorageProvider<AdvancedDepositUpgradeWrapper>(),
            ::AdvancedDepositUpgradeWrapper
        )

        instance.register(
            RestockUpgradeWrapper::class.java,
            CapabilityStorageProvider<RestockUpgradeWrapper>(),
            ::RestockUpgradeWrapper
        )

        instance.register(
            AdvancedRestockUpgradeWrapper::class.java,
            CapabilityStorageProvider<AdvancedRestockUpgradeWrapper>(),
            ::AdvancedRestockUpgradeWrapper
        )

        // Interfaces
        instance.register(
            UpgradeWrapper::class.java,
            NOPCapabilityStorage<UpgradeWrapper<*>>(),
        ) { UpgradeWrapper.Impl }

        instance.register(
            IToggleable::class.java,
            NOPCapabilityStorage<IToggleable>()
        ) { IToggleable.Impl }

        instance.register(
            IBasicFilterable::class.java,
            NOPCapabilityStorage<IBasicFilterable>()
        ) { IBasicFilterable.Impl }

        instance.register(
            IAdvancedFilterable::class.java,
            NOPCapabilityStorage<IAdvancedFilterable>()
        ) { IAdvancedFilterable.Impl }

        instance.register(
            IPickupUpgrade::class.java,
            NOPCapabilityStorage<IPickupUpgrade>(),
            ::PickupUpgradeWrapper
        )

        instance.register(
            IFeedingUpgrade::class.java,
            NOPCapabilityStorage<IFeedingUpgrade>(),
            ::FeedingUpgradeWrapper
        )

        instance.register(
            IDepositUpgrade::class.java,
            NOPCapabilityStorage<IDepositUpgrade>(),
            ::DepositUpgradeWrapper
        )

        instance.register(
            IRestockUpgrade::class.java,
            NOPCapabilityStorage<IRestockUpgrade>(),
            ::RestockUpgradeWrapper
        )
    }

    fun cacheBackpackInventory(backpackWrapper: BackpackWrapper) {
        if (BACKPACK_INVENTORY_CACHE.containsKey(backpackWrapper.uuid)) {
            backpackWrapper.isCached = true
            return
        }

        BACKPACK_INVENTORY_CACHE.put(backpackWrapper.uuid, backpackWrapper)
        backpackWrapper.isCached = true
        RetroSophisticatedBackpacks.LOGGER.info("Backpack ${backpackWrapper.uuid} is cached")
    }

    fun updateBackpackInventory(backpackWrapper: BackpackWrapper) {
        val backpack = BACKPACK_INVENTORY_CACHE[backpackWrapper.uuid]

        if (backpack == null) {
            return
        }

        backpack.deserializeNBT(backpackWrapper.serializeNBT())
        RetroSophisticatedBackpacks.LOGGER.info("Backpack ${backpackWrapper.uuid} is updated")
    }

    private interface RefinedStorage<T> : Capability.IStorage<T> {
        override fun writeNBT(
            capability: Capability<T>,
            instance: T,
            side: EnumFacing?
        ): NBTBase

        override fun readNBT(
            capability: Capability<T>,
            instance: T,
            side: EnumFacing?,
            nbt: NBTBase
        )
    }

    private class NOPCapabilityStorage<T> : RefinedStorage<T> {
        override fun writeNBT(
            capability: Capability<T>,
            instance: T,
            side: EnumFacing?
        ): NBTBase = NBTTagEnd()

        override fun readNBT(
            capability: Capability<T>,
            instance: T,
            side: EnumFacing?,
            nbt: NBTBase
        ) {
        }
    }

    internal open class CapabilityStorageProvider<T> :
        RefinedStorage<T> where T : INBTSerializable<NBTTagCompound> {
        override fun writeNBT(
            capability: Capability<T>,
            instance: T,
            side: EnumFacing?
        ): NBTBase = instance.serializeNBT()

        override fun readNBT(
            capability: Capability<T>,
            instance: T,
            side: EnumFacing?,
            nbt: NBTBase
        ) {
            instance.deserializeNBT(nbt as NBTTagCompound)
        }
    }
}