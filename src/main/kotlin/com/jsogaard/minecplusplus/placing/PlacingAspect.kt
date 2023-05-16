package com.jsogaard.minecplusplus.placing

import com.jsogaard.minecplusplus.*
import com.jsogaard.minecplusplus.effects.ParticleFX
import com.jsogaard.minecplusplus.effects.Sfx
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Dispenser
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

private const val PLACER_NAME = "Placer"
class PlacingAspect(private val plugin: CubematicPlugin) : Listener {

    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        if(event.block.type != Material.DISPENSER)
            return

        val item = event.item

        if(item.type == Material.SHULKER_BOX) {
            // Never place the shulker box, as it's one of the few blocks that can actually
            // be natively placed by a dispenser, and doing it with our method would ruin the contents
            return
        }

        if (item.type.isBlock) {
            val dispenser = event.block.toDispenser()

            if(!dispenser.isPlacer(plugin))
                return

            val targetBlock = event.block.facingBlock()
                ?: return

            val allowed = targetBlock.canPlace(item.type.createBlockData())
            val unoccupied = targetBlock.type == Material.AIR
                    || targetBlock.isReplaceable

            val noEntities = !item.type.isSolid ||
                event.block.world.getNearbyEntities(targetBlock.location.add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)
                    .filterIsInstance<LivingEntity>()
                    .isEmpty()

            if (allowed && unoccupied && noEntities) {
                val typeToPlace = when {
                    //Fixes an issue where powder is placed erroneously
                    //Without this, powder will be converted to concrete server side after 20-40 ticks, but client doesn't see it
                    targetBlock.type == Material.WATER && item.type.isConcretePowder() -> item.type.mapConcreteFromPowder()

                    else -> item.type
                }

                targetBlock.setType(typeToPlace, true)
                Sfx.blockPlaced(targetBlock)

                event.isCancelled = true

                val itemUpdated = event.item

                plugin.scheduleRun {
                    dispenser.inventory.removeItem(itemUpdated)
                }
            }
        }
    }

    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if(event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val item = event.item
        val block = event.clickedBlock

        if(block == null
            || item == null
            || item.type != Material.SHULKER_SHELL
            || block.type != Material.DISPENSER
            || event.hand != EquipmentSlot.HAND)
            return

        val dispenser = block.toDispenser()
        if(dispenser.isPlacer(plugin))
            return

        ParticleFX.convertToPlacerBlock(block.location)
        block.world.playSound(block.location, Sound.BLOCK_SCAFFOLDING_PLACE, 1f, 0.5f)
        event.isCancelled = true

        dispenser.setIsPlacer(true, plugin)
        event.setUseItemInHand(Event.Result.DENY)

        if(dispenser.customName() == null) {
            dispenser.customName(Component.text(PLACER_NAME))
            dispenser.update()
        }

        val itemInHand = event.player.inventory.itemInMainHand
        if(itemInHand.amount == 1)
            event.player.inventory.setItemInMainHand(null)
        else {
            event.player.inventory.setItemInMainHand(itemInHand - 1)
        }
    }

    @EventHandler
    fun onEvent(event: BlockBreakEvent) {
        val block = event.block
        if(block.type != Material.DISPENSER) {
            return
        }

        val dispenser = block.toDispenser()
        if(dispenser.isPlacer(plugin)) {
            if(dispenser.customName()?.contains(Component.text(PLACER_NAME), Component.EQUALS) == true) {
                dispenser.customName(null)
            }

            dispenser.setIsPlacer(false, plugin)

            block.world.dropItem(block.location.toCenterLocation(), ItemStack(Material.SHULKER_SHELL, 1))
            block.breakNaturally()

            event.isCancelled = true
        }
    }
}

private fun Material.isConcretePowder(): Boolean {
    return this in concretePowders
}

private fun Material.mapConcreteFromPowder(): Material {
    return concretePowdersToConcrete[this]
        ?: this
}

private fun PersistentDataContainer.setIsPlacer(state: Boolean, plugin: CubematicPlugin) {
    if(state) {
        set(plugin.namespaceKeys.placerBlockTag, PersistentDataType.BYTE, 1)
    } else {
        this.remove(plugin.namespaceKeys.placerBlockTag)
    }
}

private fun PersistentDataContainer.isPlacer(plugin: CubematicPlugin): Boolean {
    val data = this.get(plugin.namespaceKeys.placerBlockTag, PersistentDataType.BYTE)
    return data == 1.toByte()
}

private fun Dispenser.setIsPlacer(state: Boolean, plugin: CubematicPlugin) {
    this.persistentDataContainer.setIsPlacer(state, plugin)
    this.update()
}
private fun Dispenser.isPlacer(plugin: CubematicPlugin): Boolean {
    return this.persistentDataContainer.isPlacer(plugin)
}

private val concretePowders = listOf(
    Material.WHITE_CONCRETE_POWDER,
    Material.LIGHT_GRAY_CONCRETE_POWDER,
    Material.GRAY_CONCRETE_POWDER,
    Material.BLACK_CONCRETE_POWDER,
    Material.RED_CONCRETE_POWDER,
    Material.GREEN_CONCRETE_POWDER,
    Material.BLUE_CONCRETE_POWDER,
    Material.YELLOW_CONCRETE_POWDER,
    Material.LIME_CONCRETE_POWDER,
    Material.CYAN_CONCRETE_POWDER,
    Material.MAGENTA_CONCRETE_POWDER,
    Material.BROWN_CONCRETE_POWDER,
    Material.LIGHT_BLUE_CONCRETE_POWDER,
    Material.ORANGE_CONCRETE_POWDER,
    Material.PINK_CONCRETE_POWDER,
    Material.PURPLE_CONCRETE_POWDER,
)

private val concretePowdersToConcrete = mapOf(
    Material.WHITE_CONCRETE_POWDER to Material.WHITE_CONCRETE,
    Material.LIGHT_GRAY_CONCRETE_POWDER to Material.LIGHT_GRAY_CONCRETE,
    Material.GRAY_CONCRETE_POWDER to Material.GRAY_CONCRETE,
    Material.BLACK_CONCRETE_POWDER to Material.BLACK_CONCRETE,
    Material.RED_CONCRETE_POWDER to Material.RED_CONCRETE,
    Material.GREEN_CONCRETE_POWDER to Material.GREEN_CONCRETE,
    Material.BLUE_CONCRETE_POWDER to Material.BLUE_CONCRETE,
    Material.YELLOW_CONCRETE_POWDER to Material.YELLOW_CONCRETE,
    Material.LIME_CONCRETE_POWDER to Material.LIME_CONCRETE,
    Material.CYAN_CONCRETE_POWDER to Material.CYAN_CONCRETE,
    Material.MAGENTA_CONCRETE_POWDER to Material.MAGENTA_CONCRETE,
    Material.BROWN_CONCRETE_POWDER to Material.BROWN_CONCRETE,
    Material.LIGHT_BLUE_CONCRETE_POWDER to Material.LIGHT_BLUE_CONCRETE,
    Material.ORANGE_CONCRETE_POWDER to Material.ORANGE_CONCRETE,
    Material.PINK_CONCRETE_POWDER to Material.PINK_CONCRETE,
    Material.PURPLE_CONCRETE_POWDER to Material.PURPLE_CONCRETE,
)