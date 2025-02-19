package com.cleanroommc.retrosophisticatedbackpacks.handlers

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagByte
import net.minecraft.nbt.NBTTagCompound
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
            IToggleable::class.java,
            object : RefinedStorage<IToggleable> {
                override fun writeNBT(
                    capability: Capability<IToggleable>,
                    instance: IToggleable,
                    side: EnumFacing?
                ): NBTBase {
                    return NBTTagByte(if (instance.enabled) 1 else 0)
                }

                override fun readNBT(
                    capability: Capability<IToggleable>,
                    instance: IToggleable,
                    side: EnumFacing?,
                    nbt: NBTBase
                ) {
                    instance.enabled = (nbt as NBTTagByte).byte == 1.toByte()
                }
            }
        ) { IToggleable.Impl }
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

    private open class CapabilityStorageProvider<T> :
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