package tkworld.tools.mythicitemstyrke

import eos.moe.dragoncore.api.SlotAPI
import eos.moe.dragoncore.api.event.KeyReleaseEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.util.getStringColored
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import taboolib.platform.util.sendActionBar

object MythicItemStyrke : Plugin() {

    fun cooldown(player: Player, config: ConfigurationSection): Boolean {
        val enable = config.getBoolean("Styrke.cooldown.enable", false)
        if (!enable) {
            return true
        }
        val group = config.getString("Styrke.cooldown.group", "default")!!
        val time = config.getInt("Styrke.cooldown.time", 0)
        val mythic = config.getString("Styrke.cooldown.mythic", "error") ?: "error"
        var type = CooldownAPI.CooldownType.ABOLETH
        if (mythic != "error") {
            type = CooldownAPI.CooldownType.MYTHIC
        }
        if (config.getBoolean("Styrke.cooldown.local", false)) {
            type = CooldownAPI.CooldownType.LOCAL
        }
        var key = "MICD::${group}"
        if (type == CooldownAPI.CooldownType.MYTHIC) {
            key = mythic
        }
        val save = CooldownAPI.getCooldwon(player, key, type)
        if (save > System.currentTimeMillis()) {
            if (config.getStringColored("Styrke.cooldown.message") != null) {
                val scond = "%.2f".format(((save - System.currentTimeMillis()) / 1000.0))
                if (config.getBoolean("Styrke.cooldown.actionbar", false)) {
                    player.sendActionBar(config.getStringColored("Styrke.cooldown.message")!!.replace("{Time}", scond).replacePlaceholder(player).colored())
                }
                player.sendMessage(config.getStringColored("Styrke.cooldown.message")!!.replace("{Time}", scond).replacePlaceholder(player).colored())
            }
            return false
        }
        if (type != CooldownAPI.CooldownType.MYTHIC) {
            CooldownAPI.setCooldown(player, key, time.toLong(), type)
        }
        return true
    }

    class ActionM(val list: List<String>, val cancelled: Boolean)

    private fun ConfigurationSection.getAction(key: String): ActionM {
        if (this.getStringList("Styrke.action.${key}!!").isNotEmpty()) {
            return ActionM(this.getStringList("Styrke.action.${key}"), true)
        }
        if (this.getStringList("Styrke.action.${key}").isNotEmpty()) {
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
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onBlockBreak")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    @SubscribeEvent
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        val item = event.player.inventory.itemInMainHand
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onBlockPlace")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
            if (!config.getBoolean("Styrke.setting.place", true)) {
                event.isCancelled = true
            }
        }
    }

    @Awake(LifeCycle.ACTIVE)
    fun onTimer() {
        val per = config.getLong("period", 200)
        submit(period = per) {
            Bukkit.getOnlinePlayers().forEach l@{ player ->
                getItems(player).forEach { item ->
                    if (item.isNotAir()) {
                        val mmi = item.toMythicItem() ?: return@forEach
                        val key = mmi.getItemId(item) ?: return@forEach
                        val config = mmi.getConfig(key) ?: return@forEach
                        if (!cooldown(player, config)) {
                            return@forEach
                        }
                        config.getAction("onTimer").list.ketherEval(player)
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
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            config.getAction("onItemBreak").list.ketherEval(event.player)

        }
    }

    //物品消耗
    @SubscribeEvent
    fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (!item.isAir()) {
            val mmi = item.toMythicItem() ?: return
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onItemConsume")
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
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onPickUp")
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
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onDrop")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    //跑
    @SubscribeEvent
    fun onPlayerToggleSprintEvent(event: PlayerToggleSprintEvent) {
        val list = getItems(event.player)
        list.forEach { item ->
            if (!item.isAir()) {
                val mmi = item.toMythicItem() ?: return
                val key = mmi.getItemId(item) ?: return
                val config = mmi.getConfig(key) ?: return
                if (!cooldown(event.player, config)) {
                    return
                }
                val actionM = config.getAction("onSprint")
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
        val key = mmi.getItemId(item) ?: return
        val config = mmi.getConfig(key) ?: return
        if (!cooldown(event.player, config)) {
            return
        }
        val add = config.getInt("Styrke.food.add")
        if (add >= 1) {
            //设置饥饿值
            if (player.foodLevel + add >= 20) {
                player.foodLevel = 20
            } else if (player.foodLevel + add <= 0) {
                player.foodLevel = 0
            } else {
                player.foodLevel += add
            }
        }
    }

    //物品切换
    @SubscribeEvent
    fun onPlayerSwapHandItemsEvent(event: PlayerSwapHandItemsEvent) {
        if (event.offHandItem.isNotAir()) {
            val item = event.offHandItem!!
            val mmi = item.toMythicItem() ?: return
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onSwapToOffhand")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
        if (event.mainHandItem.isNotAir()) {
            val item = event.offHandItem!!
            val mmi = item.toMythicItem() ?: return
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionM = config.getAction("onSwapToMainHand")
            actionM.list.ketherEval(event.player)
            event.isCancelled = actionM.cancelled
        }
    }

    //物品点击
    @SubscribeEvent
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (event.item.isNotAir()) {
            val item = event.item ?: return
            val mmi = item.toMythicItem() ?: return
            val key = mmi.getItemId(item) ?: return
            val config = mmi.getConfig(key) ?: return
            if (!cooldown(event.player, config)) {
                return
            }
            val actionMs = config.getAction("onStyrkeClickAll")
            if (actionMs.list.isNotEmpty()) {
                if (config.getInt("Styrke.setting.consume", 0) != 0) {
                    if (event.item!!.amount < config.getInt("Styrke.setting.consume", 0)) {
                        event.player.error("缺少物品!! 无法执行!!")
                        return
                    } else {
                        event.item!!.amount -= config.getInt("Styrke.setting.consume", 0)
                    }
                }
            }
            actionMs.list.ketherEval(event.player)
            event.isCancelled = actionMs.cancelled
            when (event.action) {
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
                    if (config.getInt("Styrke.setting.consume", 0) != 0) {
                        if (event.item!!.amount < config.getInt("Styrke.setting.consume", 0)) {
                            event.player.error("缺少物品!! 无法执行!!")
                            return
                        } else {
                            event.item!!.amount -= config.getInt("Styrke.setting.consume", 0)
                        }
                    }
                    val actionM = config.getAction("onLeftClick")
                    actionM.list.ketherEval(event.player)
                    event.isCancelled = actionM.cancelled
                    return
                }

                Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
                    if (config.getInt("Styrke.setting.consume", 0) != 0) {
                        if (event.item!!.amount < config.getInt("Styrke.setting.consume", 0)) {
                            event.player.error("缺少物品!! 无法执行!!")
                            return
                        } else {
                            event.item!!.amount -= config.getInt("Styrke.setting.consume", 0)
                        }
                    }
                    val actionM = config.getAction("onRightClick")
                    actionM.list.ketherEval(event.player)
                    event.isCancelled = actionM.cancelled
                }

                else -> {
                    if (config.getInt("Styrke.setting.consume", 0) != 0) {
                        if (event.item!!.amount < config.getInt("Styrke.setting.consume", 0)) {
                            event.player.error("缺少物品!! 无法执行!!")
                            return
                        } else {
                            event.item!!.amount -= config.getInt("Styrke.setting.consume", 0)
                        }
                    }
                    val actionM = config.getAction("onStyrkeClick")
                    actionM.list.ketherEval(event.player)
                    event.isCancelled = actionM.cancelled
                }
            }
        }
    }

    @Config
    lateinit var config: ConfigFile


    fun getItems(player: Player): MutableList<ItemStack> {
        val list = mutableListOf<ItemStack>()
        config.getIntegerList("Slot").forEach {
            list.add(player.inventory.getItem(it)?.clone() ?: return@forEach)
        }
        list.add(player.inventory.itemInMainHand.clone())
        list.add(player.inventory.itemInOffHand.clone())
        if (config.getBoolean("DSlot-Enable", false)) {
            config.getStringList("DSlot").forEach {
                val clone = SlotAPI.getCacheSlotItem(player, it)
                if (clone != null && clone.isNotAir()) {
                    list.add(clone.clone())
                }
            }
        }
        return list
    }

    @SubscribeEvent
    fun onKeyReleaseEvent(event: KeyReleaseEvent) {
        val player = event.player
        val items = getItems(player)
        if (config.getBoolean("Styrke.setting.hand", false)) {
            items.clear()
            items.add(player.inventory.itemInMainHand)
        }
        items.forEach { item ->
            if (!item.isAir()) {
                val mmi = item.toMythicItem() ?: return
                val key = mmi.getItemId(item) ?: return
                val config = mmi.getConfig(key) ?: return
                if (!cooldown(event.player, config)) {
                    return@forEach
                }
                if (config.getAction("onKeyRelease.key").list.isNotEmpty()) {
                    if (config.getAction("onKeyRelease.key").list.contains(event.key)) {
                        val actionM = config.getAction("onKeyRelease.action")
                        actionM.list.ketherEval(event.player)
                        event.isCancelled = actionM.cancelled
                    }
                }
            }
        }
    }


}
