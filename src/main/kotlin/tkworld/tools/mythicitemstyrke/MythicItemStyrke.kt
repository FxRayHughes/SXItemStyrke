package tkworld.tools.mythicitemstyrke

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import io.lumine.xikage.mythicmobs.io.MythicConfig
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import ray.mintcat.aboleth.api.AbolethAPI
import ray.mintcat.aboleth.api.AbolethAction
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.random
import taboolib.common5.Coerce
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import tkworld.tools.mythicitemstyrke.MythicItemStyrke.getAction

object MythicItemStyrke : Plugin() {

    fun cooldown(player: Player, config: MythicConfig): Boolean {
        val enable = config.getBoolean("Styrke.cooldown.enable", false)
        if (!enable) {
            return true
        }
        val group = config.getString("Styrke.cooldown.group", "default")
        val time = config.getInteger("Styrke.cooldown.time", 0)
        val save = AbolethAPI.get(player.uniqueId, "MICD::${group}", "0").toLongOrNull() ?: 0L
        if (save > System.currentTimeMillis()) {
            return false
        }
        AbolethAPI.edit(player.uniqueId, group, AbolethAction.SET, time + System.currentTimeMillis())
        return true
    }

    class ActionM(val list: MutableList<String>, val cancelled: Boolean)

    private fun MythicConfig.getAction(key: String): ActionM {
        if (this.getStringList("Styrke.action.${key}!!").size != 0) {
            return ActionM(this.getStringList("Styrke.action.${key}"), true)
        }
        if (this.getStringList("Styrke.action.${key}").size != 0) {
            return ActionM(this.getStringList("Styrke.action.${key}"), false)
        }
        return ActionM(mutableListOf(), false)
    }

    //破坏方块
    @SubscribeEvent
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val item = event.player.inventory.itemInMainHand
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onBlockBreak")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    @SubscribeEvent
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        val item = event.player.inventory.itemInMainHand
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onBlockPlace")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
            if (!mmi.config.getBoolean("Styrke.setting.place", true)) {
                event.isCancelled = true
            }
        }
    }

    @Awake(LifeCycle.ACTIVE)
    fun onTimer() {
        submit(period = 200) {
            Bukkit.getOnlinePlayers().forEach l@{ player ->
                player.inventory.toList().forEach { item ->
                    if (item != null && item.isNotAir()) {
                        val mmi = item.toMythicItem() ?: return@forEach
                        if (!cooldown(player, mmi.config)) {
                            return@forEach
                        }
                        mmi.config.getAction("onTimer").list.ketherEval(player)
                    }
                }
            }
        }
    }

    //物品损坏
    @SubscribeEvent
    fun onPlayerItemBreakEvent(event: PlayerItemBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            mmi.config.getAction("onItemBreak").list.ketherEval(event.player)

        }
    }

    //物品消耗
    @SubscribeEvent
    fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onItemConsume")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled

        }
    }

    //物品捡起
    @SubscribeEvent
    fun onEntityPickupItemEvent(event: PlayerPickupItemEvent) {
        val player = event.player
        val item = event.item.itemStack
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onPickUp")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    //物品丢弃
    @SubscribeEvent
    fun onEntityDropItemEvent(event: PlayerDropItemEvent) {
        val item = event.itemDrop.itemStack
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onDrop")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    //跑
    @SubscribeEvent
    fun onPlayerToggleSprintEvent(event: PlayerToggleSprintEvent) {
        val list = mutableListOf<ItemStack>()
        list.addAll(event.player.inventory.armorContents)
        list.add(event.player.inventory.itemInMainHand)
        list.add(event.player.inventory.itemInOffHand)
        list.forEach { item ->
            if (!item.isAir()) {
                val mmi = item.toMythicItem() ?: return
                if (!cooldown(event.player, mmi.config)) {
                    return
                }
                val actionM = mmi.config.getAction("onSprint")
                actionM.list.ketherEval(event.player)
            }
        }
    }

    //吃掉物品
    @SubscribeEvent
    fun onEat(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (item.isAir()) {
            return
        }
        val mmi = item.toMythicItem() ?: return
        if (!cooldown(event.player, mmi.config)) {
            return
        }
        val add = mmi.config.getInteger("Styrke.food.add")
        if (add >= 1) {
            //设置饥饿值
            if (player.foodLevel + add >= 20) {
                player.foodLevel = 20
            } else if (player.foodLevel + add <= 0) {
                player.foodLevel = 0
            } else {
                player.foodLevel = player.foodLevel + add
            }
        }
    }

    //物品切换
    @SubscribeEvent
    fun onPlayerSwapHandItemsEvent(event: PlayerSwapHandItemsEvent) {
        if (event.offHandItem.isNotAir()) {
            val mmi = event.offHandItem!!.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onSwapToOffhand")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
        if (event.mainHandItem.isNotAir()) {
            val mmi = event.offHandItem!!.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            val actionM = mmi.config.getAction("onSwapToMainHand")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    //物品点击
    @SubscribeEvent
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.item.isNotAir()) {
            val mmi = event.item!!.toMythicItem() ?: return
            if (!cooldown(event.player, mmi.config)) {
                return
            }
            if (mmi.config.getInteger("Styrke.setting.consume", 0) != 0) {
                if (event.item!!.amount < mmi.config.getInteger("Styrke.setting.consume", 0)) {
                    event.player.error("缺少物品!! 无法执行!!")
                    return
                } else {
                    event.item!!.amount = event.item!!.amount - mmi.config.getInteger("Styrke.setting.consume", 0)
                }
            }
            val actionMs = mmi.config.getAction("onStyrkeClickAll")
            actionMs.list.ketherEval(event.player)
            event.isCancelled = actionMs.cancelled
            when (event.action) {
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                    val actionM = mmi.config.getAction("onLeftClick")
                    actionM.list.ketherEval(event.player)
                    event.isCancelled = actionM.cancelled
                }

                Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
                    val actionM = mmi.config.getAction("onRightClick")
                    actionM.list.ketherEval(event.player)
                    event.isCancelled = actionM.cancelled
                }

                else -> {
                    val actionM = mmi.config.getAction("onStyrkeClick")
                    actionM.list.ketherEval(event.player)
                    event.isCancelled = actionM.cancelled
                }
            }
        }
    }

    @SubscribeEvent
    fun onMythicMobDeathEvent(e: MythicMobDeathEvent) {
        e.mob.type.config.getStringList("Styrke.drops").forEach {
            val args = it.split(" ")
            if (args.size == 3 && !random(Coerce.toDouble(args[2]))) {
                return@forEach
            }
            val item = args[0].getItemStackM()
            val amount = args.getOrElse(1) { "1" }.split("-").map { a -> Coerce.toInteger(a) }
            item.amount = random(amount[0], amount.getOrElse(1) { amount[0] })
            e.drops.add(item)
        }
    }
}