package tkworld.tools.mythicitemstyrke

import ink.ptms.um.Mythic
import org.bukkit.entity.Player
import tkworld.tools.mythicitemstyrke.CooldownAPI.CooldownType.*
import top.maplex.abolethcore.AbolethCore
import java.util.concurrent.ConcurrentHashMap

object CooldownAPI {

    val map = ConcurrentHashMap<String, Long>()

    enum class CooldownType {
        MYTHIC, ABOLETH, LOCAL
    }

    //获取剩余时间 s
    fun getCooldwon(player: Player, key: String, type: CooldownType): Float {

        return when (type) {
            MYTHIC -> {
                (Mythic.API.getSkillMechanic(key)?.getCooldown(player) ?: 0F) * 1000
            }

            ABOLETH -> {
                val save = AbolethCore.api.get(player.uniqueId, "MICD::${key}", "0").toLongOrNull() ?: 0L
                if (save <= 0) {
                    return 0F
                }
                val nowTime = System.currentTimeMillis()
                ((save - nowTime) / 1000).toFloat()
            }

            LOCAL -> {
                val mapKey = "${player.uniqueId}__${key}"
                val get = map.getOrDefault(mapKey, 0L)
                if (get <= 0) {
                    return 0F
                }
                val nowTime = System.currentTimeMillis()
                ((get - nowTime) / 1000).toFloat()
            }
        }
    }


    // true 是正在冷却 false不在冷却
    fun onCooldown(player: Player, key: String, type: CooldownType): Boolean {

        return when (type) {
            MYTHIC -> {
                Mythic.API.getSkillMechanic(key)?.onCooldown(player) ?: false
            }

            ABOLETH -> {
                val save = AbolethCore.api.get(player.uniqueId, "MICD::${key}", "0").toLongOrNull() ?: 0L
                if (save <= 0) {
                    return false
                }
                val nowTime = System.currentTimeMillis()
                save > nowTime
            }

            LOCAL -> {
                val mapKey = "${player.uniqueId}__${key}"
                val get = map.getOrDefault(mapKey, -1L)
                val nowTime = System.currentTimeMillis()
                get > nowTime
            }
        }
    }

    fun setCooldown(player: Player, key: String, ms: Long, type: CooldownType) {

        when (type) {
            MYTHIC -> {
                // 实际上并不会设置mm的冷却 mm冷却由mm自己控制
                Mythic.API.getSkillMechanic(key)?.setCooldown(player, ms.toDouble())
            }

            ABOLETH -> {
                AbolethCore.api.edit(player.uniqueId, "MICD::${key}", "=", ms + System.currentTimeMillis())
            }

            LOCAL -> {
                val mapKey = "${player.uniqueId}__${key}"
                val nowTime = System.currentTimeMillis()
                map[mapKey] = nowTime + ms
            }
        }
    }

}
