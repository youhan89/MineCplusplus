package com.jsogaard.minecplusplus.breaking

import com.jsogaard.minecplusplus.Plugin
import com.jsogaard.minecplusplus.effects.Effects
import com.jsogaard.minecplusplus.facingBlock
import com.jsogaard.minecplusplus.rules.BlockBreaking
import com.jsogaard.minecplusplus.rules.Rules
import com.jsogaard.minecplusplus.toDispenserOrNull
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.random.Random

class BreakingAspect(private val plugin: Plugin): Listener {
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
                Effects.fizzle(event.block.location, event.block.getRelative(BlockFace.UP).location)
                return
            }

            if (targetBlock.type !in Rules.CANT_BE_BROKEN_BY_PLAYERS) {
                event.isCancelled = true

                val duration = BlockBreaking.getBreakTimeTicks(event.item, targetBlock)

                if(duration == 0) {
                    Effects.blockBroken(targetBlock.location, ItemStack(targetBlock.type))
                    targetBlock.breakNaturally(event.item)
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
                        animateTransaction(transaction)
                    }
                }

                //TODO -> SFX/VFX
                //TODO -> Consume durability
            } else {
                Effects.fizzle(event.block.location, event.block.getRelative(BlockFace.UP).location)
            }
        }
    }

    @EventHandler
    fun onEvent(event: BlockRedstoneEvent) {
        //plugin.server.broadcastMessage("${event.block.location.toString()} ${event.newCurrent}")

        if(event.newCurrent != 0)
            return

        val subject = event.block
        val loc = subject.location

        transactions.forEach {
            val bloc = it.value.breaker
            if(abs(bloc.x - loc.x) <= 1 && abs(bloc.y - loc.y) <= 1 && abs(bloc.z - loc.z) <= 1) {
                plugin.scheduleRun {
                    validateTransactionPower(it.value)
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

    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if(!debug) return

        if(event.clickedBlock != null && event.item?.type == Material.STICK) {
            val type = event.clickedBlock?.type
            val loot = event.clickedBlock?.getDrops(ItemStack(Material.WOODEN_PICKAXE))
            val time = event.clickedBlock?.getDestroySpeed(ItemStack(Material.WOODEN_PICKAXE))

            plugin.server.broadcastMessage("Hardness ${type?.hardness}, requireSpecial: ${event.clickedBlock?.blockData?.requiresCorrectToolForDrops()}, loot: $loot, time: $time")
        }

        if(event.clickedBlock != null && event.item?.type in Rules.ALL_TOOLS) {
            val block = event.clickedBlock!!
            val tool = event.item!!

            val breakTime = BlockBreaking.getBreakTimeTicks(event.item!!, event.clickedBlock!!) / 20 //to seconds
            val requireSpecial = event.clickedBlock?.blockData?.requiresCorrectToolForDrops()
            val isPreferred = event.clickedBlock!!.isPreferredTool(event.item!!)

            val toolMultiplier = block.getDestroySpeed(tool)
            val canHarvest = !block.blockData.requiresCorrectToolForDrops() || block.blockData.isPreferredTool(tool)
            val efficiencyLevel = tool.itemMeta.enchants.firstNotNullOfOrNull {
                if(it.key == Enchantment.DIG_SPEED)
                    it.value
                else null
            } ?: 0
            val blockHardness = block.type.hardness

            plugin.server.broadcastMessage("Breaktime: $breakTime. requireSpecial: $requireSpecial, isPreferred: $isPreferred, canHarvest: $canHarvest, hardness: $blockHardness, toolMultiplier: $toolMultiplier, efficiency: $efficiencyLevel")
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
        Effects.blockBroken(tx.breakee, ItemStack(tx.breakee.block.type))
        targetBlock.breakNaturally(tx.tool)
        modifyDurability(newToolRef)
    }

    private fun modifyDurability(tool: ItemStack) {
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
    }

    private fun failTransaction(tx: BreakTransaction, msg: String) {
        transactions.remove(tx.id)

        if(!debug) return

        plugin.server.broadcastMessage("Breaker Tx failed: $msg")
    }

    private fun animateTransaction(transaction: BreakTransaction) {
        if(!transactions.contains(transaction.id))
            return

        Effects.crackAllSides(transaction.breakee, transaction.breakee.block.type)
    }

    private fun validateTransactionPower(tx: BreakTransaction) {
        if(tx.breaker.block.blockPower == 0) {
            failTransaction(tx, "Redstone power went away")
        }
    }
}