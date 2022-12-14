package vip.floatationdevice.msu.spawn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static vip.floatationdevice.msu.I18nUtil.translate;

public class RequestManager
{
    final static boolean[] interruptors = ConfigManager.getInterruptors();
    final static ConcurrentHashMap<UUID, CooldownThread> cooldownPlayers = new ConcurrentHashMap<UUID, CooldownThread>();
    final static ConcurrentHashMap<UUID, TeleportThread> warmupPlayers = new ConcurrentHashMap<UUID, TeleportThread>();

    public static void addCooldown(UUID u)
    {
        if(ConfigManager.getCooldownSec() < 1 || Bukkit.getPlayer(u).hasPermission("spawn.nocooldown")) return;
        cooldownPlayers.put(u, new CooldownThread(u, cooldownPlayers));
        cooldownPlayers.get(u).start();
    }

    public static long getCooldownRemaining(UUID u){return cooldownPlayers.get(u).getCooldownRemaining();}

    public static boolean hasCooldown(UUID u){return cooldownPlayers.containsKey(u);}

    public static boolean hasWarmup(UUID u){return warmupPlayers.containsKey(u);}

    public static boolean makeTeleportRequest(Player p)
    {
        if(p.hasPermission("spawn.spawn"))
        {
            if(hasCooldown(p.getUniqueId()))
            {
                p.sendMessage(translate("err-cooldown").replace("{0}", String.valueOf(getCooldownRemaining(p.getUniqueId()) / 1000L)));
                return false;
            }
            else if(hasWarmup(p.getUniqueId()))
            {
                p.sendMessage(translate("err-warmup"));
                return false;
            }
            else
            {
                TeleportThread w = new TeleportThread(p.getUniqueId(), warmupPlayers);
                warmupPlayers.put(p.getUniqueId(), w);
                w.start();
                return true;
            }
        }
        else
        {
            p.sendMessage(translate("err-permission-denied"));
            return false;
        }
    }
}
