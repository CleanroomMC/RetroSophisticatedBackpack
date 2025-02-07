package com.cleanroommc.retrosophisticatedbackpack.handlers

import com.cleanroommc.retrosophisticatedbackpack.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackWrapper
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import java.util.UUID

object CapabilityHandler {
    @JvmField
    @CapabilityInject(BackpackWrapper::class)
    val BACKPACK_ITEM_HANDLER_CAPABILITY: Capability<BackpackWrapper>? = null
    
    val BACKPACK_INVENTORY_CACHE: Object2ObjectMap<UUID, BackpackWrapper> = Object2ObjectOpenHashMap()
    
    fun register() {
        CapabilityManager.INSTANCE.register(BackpackWrapper::class.java, object : Capability.IStorage<BackpackWrapper> {
            override fun writeNBT(
                capability: Capability<BackpackWrapper?>,
                instance: BackpackWrapper,
                side: EnumFacing?
            ): NBTBase =
                instance.serializeNBT()

            override fun readNBT(
                capability: Capability<BackpackWrapper?>,
                instance: BackpackWrapper,
                side: EnumFacing?,
                nbt: NBTBase
            ) {
                instance.deserializeNBT(nbt as NBTTagCompound)
                cacheBackpackInventory(instance)
            }
        }, ::BackpackWrapper)
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
}