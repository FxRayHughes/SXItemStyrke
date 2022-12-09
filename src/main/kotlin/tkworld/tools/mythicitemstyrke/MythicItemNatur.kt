package tkworld.tools.mythicitemstyrke

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.enums.ExpSource
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobItemGenerateEvent
import io.lumine.xikage.mythicmobs.items.MythicItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.random
import taboolib.common5.Coerce
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.platform.util.giveItem
import tkworld.tools.mythicitemstyrke.weight.WeightCategory
import tkworld.tools.mythicitemstyrke.weight.WeightUtil

object MythicItemNatur {

    fun getMythicItem(itemStack: ItemStack): MythicItem? {
        if (itemStack.type == Material.AIR || !itemStack.hasItemMeta()) {
            return null
        }
        val items = itemStack.clone()
        items.amount = 1
        val nbtId = items.getItemTag()["MythicItem"]
        if (nbtId != null) {
            return MythicMobs.inst().itemManager.items.firstOrNull { it.internalName == nbtId.asString() }
        }
        return MythicMobs.inst().itemManager.items.firstOrNull { it.displayName == itemStack.name() }
    }


    @SubscribeEvent
    fun onEvent(event: MythicMobItemGenerateEvent) {
        val item = event.item
        if (!item.config.getBoolean("natur", false)) {
            return
        }
        getItemStack(item).getItemTag().saveTo(event.itemStack)
    }

    fun getItemStack(mythicItem: MythicItem): ItemStack {
        val itemStack =
            MythicMobs.inst().itemManager.getItemStack(mythicItem.internalName) ?: return ItemStack(Material.STONE)
        val items = itemStack.clone()
        items.amount = 1
        items.amount = itemStack.amount
        val meta = items.itemMeta!!
        val tag = items.getItemTag()
        tag["MythicItem"] = ItemTagData(mythicItem.internalName)
        val lore = mythicItem.lore ?: listOf()
        val newLore = mutableListOf<String>()
        lore.forEachIndexed { _, info ->
            val test = info.center("[", "]")
            if (test != null) {
                val save = info.replace(test, "<-Save->").replace("[\\[\\]]".toRegex(), "")
                newLore.addAll(LoreSteam.eval(test).map { save.replace("<-Save->", it).range() })
            } else {
                newLore.add(info.range())
            }
        }
        meta.lore = newLore
        items.itemMeta = meta
        return items.setItemTag(tag)
    }

    @Config(value = "drops.yml")
    lateinit var dropConfig: ConfigFile
        private set

    @SubscribeEvent
    fun onDie(event: MythicMobDeathEvent) {
        val enable = event.mob.type.config.getBoolean("RDrop.enable")
        if (!enable) {
            return
        }
        event.drops.clear()
        val group = event.mob.type.config.getString("RDrop.group", "null")
        val number = event.mob.type.config.getString("RDrop.number", "1-5")
        val min = number.split("-").getOrNull(0)?.toIntOrNull() ?: 0
        val max = number.split("-").getOrNull(1)?.toIntOrNull() ?: 0
        val getter = if (max != min) {
            (min..max).random()
        } else {
            min
        }

        val old = event.mob.type.config.getStringList("RDrop.drops")
        val groupList = dropConfig.getStringList(group).toMutableList()
        groupList.addAll(old)


        val drops = mutableListOf<String?>()
        val ma = groupList.map {
            val info = it.split(" ")
            WeightCategory(it, info.getOrElse(2) { "0" }.toIntOrNull() ?: 0)
        }.toMutableList()
        (1..getter).forEach { _ ->
            val i = WeightUtil.getWeightRandom(ma)
            drops.add(i)
            ma.removeAll {
                it.getCategory() == i
            }
        }
        val player = event.killer as? Player ?: return
        drops.forEach {
            if (it == null) {
                return@forEach
            }
            val info = it.split(" ")
            val item = info.getOrElse(0) { "null" }
            val amount = info.getOrElse(1) { "1" }.split("-").map { a -> Coerce.toInteger(a) }
            val dropsNumber = random(amount[0], amount.getOrElse(1) { amount[0] })
            when (item) {

                "skillapi-exp" -> {
                    val playerData = SkillAPI.getPlayerData(player) ?: return@forEach
                    playerData.giveExp(dropsNumber.toDouble(), ExpSource.COMMAND, false)
                    player.sendMessage("+${dropsNumber}经验")
                }

                "money" -> {
                    Vault.addMoney(player, dropsNumber.toDouble())
                    player.sendMessage("+${dropsNumber}游戏币")
                }

                else -> {
                    player.giveItem(MythicMobs.inst().itemManager.getItemStack(item), dropsNumber)
                }
            }
        }
    }
}