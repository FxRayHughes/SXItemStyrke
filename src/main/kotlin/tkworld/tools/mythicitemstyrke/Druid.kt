package tkworld.tools.mythicitemstyrke

import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.adaptPlayer
import taboolib.common5.Baffle
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.nms.getI18nName
import taboolib.platform.util.hasName
import tkworld.tools.mythicitemstyrke.itemdata.ItemData
import tkworld.tools.mythicitemstyrke.weight.WeightCategory
import tkworld.tools.mythicitemstyrke.weight.WeightUtil


fun List<String>.ketherEval(senderPlayer: Player) {

    if (firstOrNull()?.startsWith("random: ") == true) {
        val number = firstOrNull()?.replace("random: ", "")?.toIntOrNull() ?: return
        val list = this.drop(1).mapNotNull {
            val keys = it.split(" | ")
            WeightCategory(
                keys.getOrNull(1) ?: return@mapNotNull null,
                (keys.getOrNull(0) ?: return@mapNotNull null).toIntOrNull() ?: return@mapNotNull null
            )
        }
        val runs = (1..number).mapNotNull { WeightUtil.getWeightRandom(list) }
        try {
            KetherShell.eval(runs) {
                sender = adaptPlayer(senderPlayer)
            }
        } catch (e: LocalizedException) {
            e.printKetherErrorMessage()
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
        }
        return
    }

    try {
        KetherShell.eval(this) {
            sender = adaptPlayer(senderPlayer)
        }
    } catch (e: LocalizedException) {
        e.printKetherErrorMessage()
    } catch (e: Throwable) {
        e.printKetherErrorMessage()
    }
}

fun ItemStack.name(): String {
    return if (this.hasName()) {
        this.itemMeta!!.displayName
    } else {
        this.getI18nName()
    }
}

fun String.center(prefix: String, suffix: String): String? {
    val start = this.indexOfLast { it.toString() == prefix }
    val end = this.indexOfFirst { it.toString() == suffix }
    if (start == -1 || end == -1) {
        return null
    }
    return this.subSequence(start + 1, end).toString()

}

fun ItemData.getItemData(id: String, player: Player): ItemStack {
    return MythicItemNatur.getItemStack(this, id, player)
}

fun ItemStack.toMythicItem(): ItemData? {
    return MythicItemNatur.getItemData(this)
}


fun Player.info(vararg block: String) {
    block.forEach {
        toInfo(this, it)
    }
}

fun Player.error(vararg block: String) {
    block.forEach {
        toError(this, it)
    }
}

fun debug(vararg block: String) {
    val player = Bukkit.getPlayerExact("Ray_Hughes") ?: return
    block.forEach {
        toError(player, it)
    }
}

fun toInfo(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§a Natur §8] §7${message.replace("&", "§")}")
    if (sender is Player && !cooldown.hasNext(sender.name)) {
        sender.playSound(sender.location, Sound.UI_BUTTON_CLICK, 1f, (1..2).random().toFloat())
    }
}

fun toError(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§4 Natur §8] §7${message.replace("&", "§")}")
    if (sender is Player && !cooldown.hasNext(sender.name)) {
        sender.playSound(sender.location, Sound.ENTITY_VILLAGER_NO, 1f, (1..2).random().toFloat())
    }
}

fun toDone(sender: CommandSender, message: String) {
    sender.sendMessage("§8[§6 Natur §8] §7${message.replace("&", "§")}")
    if (sender is Player && !cooldown.hasNext(sender.name)) {
        sender.playSound(sender.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, (1..2).random().toFloat())
    }
}

fun toConsole(message: String) {
    Bukkit.getConsoleSender().sendMessage("§8[§e Natur §8] §7${message.replace("&", "§")}")
}

val cooldown = Baffle.of(100)
