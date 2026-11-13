package com.cursedtides.dialogue;

import com.cursedtides.character.CharacterData;
import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import com.cursedtides.quest.HorizonArc;
import com.cursedtides.quest.QuestData;
import com.cursedtides.quest.SideQuests;
import com.cursedtides.social.CrewState;
import com.cursedtides.world.ModWorld;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * The tests a line or a reply can depend on. Each is a short token; "!" in front negates it.
 * <pre>
 *   dawn day dusk night | clear raining thundering | wanted | crew | gate | chance:0.3
 *   navy>=100 brethren<0 smugglers>=300 | story>=2 arc==2 relics>=4 notoriety>=50 cleared>=10 discovered>=8
 *   blades>=10 firearms>=5 sailing>=20 fishing>=15 (any proficiency or profession)
 *   ledger==2 specimens>=1 shade==3 seam==2 | relic:lantern arcflag:offered ending:sealed
 *   has:cursedtides:governors_ledger | flag:met_rook (remembered by this NPC) | background:hexer
 * </pre>
 */
public final class Conditions {
    private Conditions() {}

    private static final Pattern COMPARE = Pattern.compile("([a-z_]+)(>=|<=|==|!=|>|<)(-?\\d+)");

    public static boolean all(List<String> tests, ServerPlayer p, Entity npc) {
        if (tests == null) return true;
        for (String t : tests) if (!test(t, p, npc)) return false;
        return true;
    }

    public static boolean test(String token, ServerPlayer p, Entity npc) {
        token = token.trim();
        if (token.startsWith("!")) return !test(token.substring(1), p, npc);
        Level w = p.level();
        long time = w.getDayTime() % 24000;
        switch (token) {
            case "dawn": return time >= 23000 || time < 1000;
            case "day": return time >= 1000 && time < 12000;
            case "dusk": return time >= 12000 && time < 13000;
            case "night": return time >= 13000 && time < 23000;
            case "clear": return !w.isRaining();
            case "raining": return w.isRaining() && !w.isThundering();
            case "thundering": return w.isThundering();
            case "wanted": return FactionData.isWanted(p);
            case "crew": return p.getServer() != null && CrewState.get(p.getServer()).crewOf(p.getUUID()) != null;
            case "gate": return HorizonArc.gateOpened(p.getServer());
            case "archipelago": return ModWorld.isArchipelago();
            case "festival": return com.cursedtides.living.Festivals.current() != null;
            case "open": return npc == null || com.cursedtides.living.Hours.isOpen(npc);
            case "house": return com.cursedtides.home.Homes.ownsAny(p);
            default: break;
        }
        int colon = token.indexOf(':');
        if (colon > 0) {
            String kind = token.substring(0, colon), arg = token.substring(colon + 1);
            switch (kind) {
                case "chance": return p.getRandom().nextFloat() < Float.parseFloat(arg);
                case "holding": {
                    var held = p.getMainHandItem();
                    if (arg.equals("deed")) return held.getItem() instanceof com.cursedtides.item.ShipDeedItem;
                    if (arg.equals("fish")) return held.getItem() instanceof com.cursedtides.profession.CatchItem;
                    ResourceLocation id = ResourceLocation.tryParse(arg);
                    return id != null && held.is(BuiltInRegistries.ITEM.get(id));
                }
                case "has": {
                    ResourceLocation id = ResourceLocation.tryParse(arg);
                    return id != null && QuestData.has(p, BuiltInRegistries.ITEM.get(id));
                }
                case "flag": return p.getTags().contains(Dialogue.flagTag(arg));
                case "relic": return HorizonArc.hasRelic(p, arg);
                case "arcflag": return HorizonArc.hasFlag(p, arg);
                case "ending": return HorizonArc.endingName(p).equals(arg);
                case "festival": {
                    var f = com.cursedtides.living.Festivals.current();
                    return f != null && f.name().equalsIgnoreCase(arg);
                }
                case "achievement": return com.cursedtides.activity.Achievements.has(p, arg);
                case "found": return com.cursedtides.explore.Collectibles.has(p, arg);
                case "background": return CharacterData.isCreated(p) && CharacterData.background(p).name().equalsIgnoreCase(arg);
                default: return false;
            }
        }
        Matcher m = COMPARE.matcher(token);
        if (!m.matches()) return false;
        int value = value(m.group(1), p);
        int n = Integer.parseInt(m.group(3));
        return switch (m.group(2)) {
            case ">=" -> value >= n;
            case "<=" -> value <= n;
            case "==" -> value == n;
            case "!=" -> value != n;
            case ">" -> value > n;
            default -> value < n;
        };
    }

    private static int value(String name, ServerPlayer p) {
        switch (name) {
            case "navy": return FactionData.rep(p, Faction.NAVY);
            case "brethren": return FactionData.rep(p, Faction.BRETHREN);
            case "smugglers": return FactionData.rep(p, Faction.SMUGGLERS);
            case "story": return QuestData.stage(p);
            case "arc": return HorizonArc.stage(p);
            case "relics": return HorizonArc.relicCount(p);
            case "notoriety": return PirateData.notoriety(p);
            case "cleared": return Dungeons.clearedCount(p);
            case "discovered": return Integer.bitCount(p.getAttachedOrElse(ModWorld.DISCOVERED, 0));
            case "bounty": return FactionData.bounty(p);
            case "crown": return com.cursedtides.quest.CrownArc.stage(p);
            case "lights": return com.cursedtides.quest.LanternArc.stage(p);
            case "contracts": return p.getAttachedOrElse(com.cursedtides.quest.Contracts.DONE, 0);
            case "crownshards": return com.cursedtides.quest.CrownArc.shardCount(p);
            case "crownending": return com.cursedtides.quest.CrownArc.ending(p);
            case "prosperity": return com.cursedtides.living.Prosperity.at(p.getServer(), p.getBlockX(), p.getBlockZ());
            case "collected": return com.cursedtides.explore.Collectibles.count(p);
            case "achievements": return com.cursedtides.activity.Achievements.count(p);
            case "tavern_wins": return com.cursedtides.activity.Leaderboards.score(p, "drinking");
            case "arena_best": return com.cursedtides.activity.Leaderboards.score(p, "arena");
            case "range_best": return com.cursedtides.activity.Leaderboards.score(p, "range");
            case "ledger": return SideQuests.get(p, SideQuests.LEDGER);
            case "specimens": return SideQuests.get(p, SideQuests.SPECIMENS);
            case "shade": return SideQuests.get(p, SideQuests.SHADE);
            case "seam": return SideQuests.get(p, SideQuests.SEAM);
            default: break;
        }
        for (Weapon wpn : Weapon.values()) {
            if (wpn.name().equalsIgnoreCase(name) || alias(wpn).equals(name)) return PirateData.level(p, wpn);
        }
        for (Profession prof : Profession.values()) {
            if (prof.name().equalsIgnoreCase(name)) return ProfessionData.level(p, prof);
        }
        return 0;
    }

    private static String alias(Weapon w) {
        return switch (w) {
            case CUTLASS -> "blades";
            case PISTOL -> "firearms";
            case CANNON -> "gunnery";
            case HEX -> "hexes";
            default -> w.name().toLowerCase();
        };
    }
}
