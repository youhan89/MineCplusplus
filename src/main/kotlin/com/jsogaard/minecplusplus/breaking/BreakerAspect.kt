package com.jsogaard.minecplusplus.breaking

import com.jsogaard.minecplusplus.Plugin
import com.jsogaard.minecplusplus.deleteOne
import com.jsogaard.minecplusplus.effects.ParticleFX
import com.jsogaard.minecplusplus.effects.Sfx
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.rules.BlockBreaking
import com.jsogaard.minecplusplus.rules.Rules
import com.jsogaard.minecplusplus.toDispenserOrNull
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.random.Random

class BreakerAspect(private val plugin: Plugin): Listener {
    private val debug = false
    private val transactions = mutableMapOf<Long, BreakTransaction>()
    private var counter = 0L

    @EventHandler
    fun onEvent(event: BlockDispenseEvent) {
        if (event.item.type in Rules.BLOCK_BREAK_ENABLERS) {
            val targetBlock = event.block.facingBlock()
                ?: return

            if(targetBlock.isEmpty || targetBlock.isLiquid) {
                event.isCancelled = true
                ParticleFX.fizzle(event.block.location, event.block.getRelative(BlockFace.UP).location)
                return
            }

            if (targetBlock.type !in Rules.CANT_BE_BROKEN_BY_PLAYERS) {
                event.isCancelled = true

                val duration = BlockBreaking.getBreakTimeTicks(event.item, targetBlock)

                if(duration == 0) {
                    performBlockBreak(targetBlock, event.item)
                    return
                }

                val transaction = BreakTransaction(
                    id = nextTxId(),
                    durationTicks = duration,
                    breaker = event.block.location,
                    breakee = targetBlock.location,
                    breakeeType = targetBlock.type,
                    tool = event.item,
                )

                transactions[transaction.id] = transaction

                plugin.scheduleRun(transaction.durationTicks.toLong()) {
                    onTransactionExecute(transaction)
                }

                val animations = transaction.durationTicks / 20
                (0..animations).forEach {
                    plugin.scheduleRun(it * 20.toLong()) {
                        animateProgress(transaction)
                    }
                }
            } else {
                ParticleFX.fizzle(event.block.location, event.block.getRelative(BlockFace.UP).location)
            }
        }
    }

    @EventHandler
    fun onEvent(event: BlockRedstoneEvent) {
        if(event.newCurrent != 0)
            return

        if(transactions.isEmpty())
            return

        val subject = event.block
        val loc = subject.location

        if(!loc.isWorldLoaded)
            return

        transactions.forEach {
            val bloc = it.value.breaker

            if(!bloc.isWorldLoaded)
                return@forEach

            if(abs(bloc.x - loc.x) <= 1 && abs(bloc.y - loc.y) <= 1 && abs(bloc.z - loc.z) <= 1 && loc.world == bloc.world) {
                plugin.scheduleRun {
                    validateBreakerPower(it.value)
                }
            }
        }
    }

    @EventHandler
    fun onEvent(event: BlockBreakEvent) {
        transactions.forEach {
            if(it.value.breakee == event.block.location)
                failTransaction(it.value, "Block was broken")
        }
    }

    private fun nextTxId() = counter++

    private fun onTransactionExecute(tx: BreakTransaction) {
        if(!transactions.contains(tx.id))
            return

        val dispenserBlock = tx.breaker.block
        val targetBlock = tx.breakee.block

        val dispenser = dispenserBlock.toDispenserOrNull()
            ?: run {
                failTransaction(tx, "The breaker was not a dispenser")
                return
            }

        val newToolRef = dispenser.inventory.filterNotNull().firstOrNull { it.isSimilar(tx.tool) }
        if(newToolRef == null) {
            failTransaction(tx, "The tool wasn't found")
            return
        }

        if(targetBlock.type != tx.breakeeType) {
            failTransaction(tx, "The target material doesnt match")
            return
        }

        transactions.remove(tx.id)
        performBlockBreak(targetBlock, tx.tool)
        modifyDurability(newToolRef, dispenser.inventory)
    }

    private fun performBlockBreak(block: Block, tool: ItemStack) {
        ParticleFX.blockBroken(block.location, ItemStack(block.type))
        Sfx.blockBreak(block)
        block.breakNaturally(tool)
    }

    private fun modifyDurability(tool: ItemStack, inventory: Inventory) {
        val meta = (tool.itemMeta as? Damageable)
            ?: throw IllegalArgumentException("Tool not damageable")

        val unbreakingLevel = meta.enchants.firstNotNullOfOrNull {
            if(it.key == Enchantment.DURABILITY)
                it.value
            else null
        } ?: 0

        val applyUse = when(unbreakingLevel) {
            3 -> false
            2,1 -> Random.nextInt(1, 1 + unbreakingLevel*2) == 1
            else -> true
        }

        if(applyUse) {
            meta.damage++
            tool.itemMeta = meta
        }

        if(meta.damage >= tool.type.maxDurability) {
            inventory.deleteOne(tool)
            inventory.location?.world?.playSound(inventory.location!!, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
        }
    }

    private fun failTransaction(tx: BreakTransaction, msg: String) {
        transactions.remove(tx.id)

        if(!debug) return

        plugin.server.broadcastMessage("Breaker Tx failed: $msg")
    }

    private fun animateProgress(transaction: BreakTransaction) {
        if(!transactions.contains(transaction.id))
            return

        ParticleFX.crackAllSides(transaction.breakee, transaction.breakee.block.type)
    }

    private fun validateBreakerPower(tx: BreakTransaction) {
        if(tx.breaker.block.blockPower == 0) {
            failTransaction(tx, "Redstone power went away")
        }
    }
}