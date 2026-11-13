package com.cursedtides.client;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * The bosses are vanilla creatures under a name (a drowned called the Tide Sovereign, a ravager called the Slag
 * Beast...). This gives each its own Mob Workshop model: any mob whose name starts with a boss's name is drawn
 * with that boss's renderer instead of its vanilla one. Renderers are rebuilt whenever resources reload.
 */
public final class BossLooks {
    private BossLooks() {}

    /** Boss name prefix to workshop model id, longest names first so nothing is shadowed. */
    private static final String[][] NAMES = {
            {"Red Maddox", "red_maddox"},
            {"Brannoch", "brannoch"},
            {"Claim-Jumper Grubb", "grubb"},
            {"Mother of Gulls", "mother_of_gulls"},
            {"Mother Brine", "mother_brine"},
            {"Old Gnasher", "old_gnasher"},
            {"The Tide Sovereign", "tide_sovereign"},
            {"The Lampwright's Hand", "lampwrights_hand"},
            {"The Cinder King", "cinder_king"},
            {"The Cinder Matron", "cinder_matron"},
            {"The Veil Witch", "veil_witch"},
            {"The Ashen Herald", "ashen_herald"},
            {"The Pretender", "pretender"},
            {"The Abyssal", "abyssal"},
            {"The Grey Lamplighter", "grey_lamplighter"},
            {"The Bone Abbot", "bone_abbot"},
            {"The Slag Beast", "slag_beast"},
            {"The Lantern Warden", "lantern_warden"},
            {"Dread Admiral Corvin Hale", "corvin_hale"},
            {"Captain Saltbones", "captain_saltbones"},
            {"Bosun Grimmet Saltbones", "captain_saltbones"},
            {"The Barnacle Bride", "barnacle_bride"},
            {"The Hangman of Bloodwake", "hangman"},
            {"The Witchlight Hag", "witchlight_hag"},
            {"The Signal Keeper", "signal_keeper"},
            {"Reach Serpent", "reach_serpent"},
            {"Siren of the Shoals", "siren"},
    };

    public static final net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey<EntityRenderer<?, ?>> RENDERER =
            net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey.create(() -> "cursedtides:boss_renderer");

    private static final Map<String, EntityRenderer<?, ?>> RENDERERS = new LinkedHashMap<>();

    /** Called from a renderer factory, so it runs on every resource reload with a live context. */
    public static <R> R rebuild(EntityRendererProvider.Context ctx, R passThrough) {
        RENDERERS.clear();
        for (String[] n : NAMES) {
            String id = n[1];
            String[] parts = WorkshopModels.parts(id);
            EntityRenderer<?, ?> r;
            if (parts == null) {
                r = WorkshopBipedRenderer.<Mob>single(ctx, id);
            } else {
                float scale = id.equals("abyssal") ? 1.6f : id.equals("reach_serpent") ? 0.55f : 1.0f;
                r = new CreatureRenderer<Mob>(ctx, WorkshopModels.layer(id), parts, 1.0f, scale, e -> id, "workshop");
            }
            RENDERERS.put(n[0], r);
        }
        return passThrough;
    }

    /** The boss renderer for this entity, or null to draw it as usual. */
    public static EntityRenderer<?, ?> forEntity(Entity entity) {
        if (!(entity instanceof Mob) || !entity.hasCustomName() || RENDERERS.isEmpty()) return null;
        Component name = entity.getCustomName();
        if (name == null) return null;
        String s = name.getString();
        for (String[] n : NAMES) {
            if (s.startsWith(n[0])) return RENDERERS.get(n[0]);
        }
        return null;
    }
}
