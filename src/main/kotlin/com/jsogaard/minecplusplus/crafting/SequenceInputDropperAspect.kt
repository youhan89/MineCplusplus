package com.jsogaard.minecplusplus.crafting

import com.jsogaard.minecplusplus.*
import com.jsogaard.minecplusplus.effects.ParticleFX
import com.jsogaard.minecplusplus.effects.Sfx
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Dropper
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

private const val CUSTOM_NAME = "Sequence Dropper"
private val ACTIVATOR_MATERIAL = Material.CLOCK

/**
 * Dropper with sequentially incremented input slot when getting items from another block.
 */
class SequenceInputDropperAspect(private val plugin: CubematicPlugin): Listener {
    @EventHandler
    fun onMoveInventoryEvent(event: InventoryMoveItemEvent) {
        if(event.destination.type == InventoryType.DROPPER) {
            val source = event.source
            val dropperInventory = event.destination
            val dropper = (dropperInventory.holder as Dropper)

            if(!dropper.isSequentialMode()) {
                return
            } else {
                //Cancel the native move, this returns the item to the source.
                event.isCancelled = true
            }

            var targetSlot = when(val last = dropper.getDropSlot(plugin)) {
                null -> 0
                else -> {
                    if(last.coerceIn(0,8) + 1 <= 8)
                        last + 1
                    else 0
                }
            }

            var triesLeft = 9
            var canStack = false
            while(triesLeft > 0) {
                val slot = dropperInventory.contents[targetSlot]
                if (slot.canStackWith(event.item)) {
                    canStack = true
                    break
                } else {
                    triesLeft--
                    targetSlot = if(targetSlot == 8) 0 else targetSlot + 1
                }
            }

            if(!canStack) {
                //Can't place item - dropper full
                return
            }

            dropper.setDropSlot(plugin, targetSlot.toByte())

            //Make sure the target can receive the item
            if(incrementedStackOrNull(dropperInventory, targetSlot, event.item.type) != null) {
                // When executing this code, the item is "up in the air" and can't be subtracted from the source...?
                // it appears to be overwritten with original stack after cancelling the event.
                // So we tell the server to cancel event and queue our custom transaction asap instead.
                plugin.scheduleRun {
                    transferOne(source, dropperInventory, event.item.type, targetSlot)
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
            || item.type != ACTIVATOR_MATERIAL
            || block.type != Material.DROPPER
            || event.hand != EquipmentSlot.HAND)
            return

        val dropper = block.toDropper()
        if(dropper.isSequentialMode())
            return

        ParticleFX.convertToSequential(block.location, event.blockFace, plugin)
        Sfx.blockToSequential(block.location, plugin)

        event.isCancelled = true

        dropper.setIsSequentialMode(true)
        event.setUseItemInHand(Event.Result.DENY)

        if(dropper.customName() == null) {
            dropper.customName(Component.text(CUSTOM_NAME))
            dropper.update()
        }

        val itemInHand = event.player.inventory.itemInMainHand
        if(itemInHand.amount == 1) {
            event.player.inventory.setItemInMainHand(null)
        } else {
            event.player.inventory.setItemInMainHand(itemInHand - 1)
        }
    }

    @EventHandler
    fun onEvent(event: BlockBreakEvent) {
        val block = event.block
        if(block.type != Material.DROPPER) {
            return
        }

        val dropper = block.toDropper()
        if(dropper.isSequentialMode()) {
            if(dropper.customName()?.contains(Component.text(CUSTOM_NAME), Component.EQUALS) == true) {
                dropper.customName(null)
            }

            dropper.setIsSequentialMode(false)

            block.world.dropItem(block.location.toCenterLocation(), ItemStack(ACTIVATOR_MATERIAL, 1))
            block.breakNaturally()

            event.isCancelled = true
        }
    }

    private fun Dropper.isSequentialMode() = this.isCraftingDropper(plugin)
    private fun Dropper.setIsSequentialMode(state: Boolean) = this.setCraftingDropper(state, plugin)

    private fun incrementedStackOrNull(target: Inventory, targetSlot: Int, type: Material): ItemStack? {
        val transferStack = ItemStack(type, 1)
        val targetStack = target.getItem(targetSlot)
        return if (targetStack == null || targetStack.amount == 0) {
            transferStack
        } else {
            targetStack.stackWithOrNull(transferStack)
        }
    }
    private fun transferOne(source: Inventory, target: Inventory, type: Material, targetSlot: Int) {
        val finalStack = incrementedStackOrNull(target, targetSlot, type)

        if (finalStack != null) {
            val slot = source.contents.indexOfFirst { itemStack ->
                ItemStack(type, 1).isSimilar(itemStack)
            }

            if(slot != -1) {
                val stack = source.getItem(slot)!!

                source.clear(slot)
                source.setItem(slot, stack - 1)

                target.setItem(targetSlot, finalStack)
            }
        }
    }

}