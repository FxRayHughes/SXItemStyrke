package tkworld.tools.mythicitemstyrke

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand
import tkworld.tools.mythicitemstyrke.itemdata.ItemDataManager

@CommandHeader(name = "itemcommand", aliases = ["ic"], permission = "*")
object MythicItemCommand {

    @CommandBody
    val main = subCommand {
        dynamic("type") {
            dynamic("id") {
                player("target") {
                    execute<CommandSender> { sender, context, argument ->
                        val data = ItemDataManager.items[context["type"]] ?: return@execute
                        val action =
                            data.getConfig(context["id"])?.getStringList("Styrke.action.onCommand") ?: return@execute
                        val player = Bukkit.getPlayer(context.player("target").uniqueId) ?: return@execute
                        action.ketherEval(player)
                    }
                }
                execute<Player> { sender, context, argument ->
                    val data = ItemDataManager.items[context["type"]] ?: return@execute
                    val action =
                        data.getConfig(context["id"])?.getStringList("Styrke.action.onCommand") ?: return@execute
                    action.ketherEval(sender)
                }
            }
        }
    }

}
