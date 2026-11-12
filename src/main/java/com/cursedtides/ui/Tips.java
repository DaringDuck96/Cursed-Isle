package com.cursedtides.ui;

import java.util.List;
import java.util.Random;

/** Tips for the loading screens, and the Pirate Code page in the journal. */
public final class Tips {
    private Tips() {}

    public static final List<String> TIPS = List.of(
            "Hold Alt aboard ship and press Z, X, C, V or B for your sailing skills: Full Sail, Come About and more.",
            "Press R on foot to lock on to a foe. Press it again to switch targets, and sneak + R to let go.",
            "Hold a special shot (venom, hot or silver) in your off hand and your gun will load it first.",
            "Silver shot hits the cursed and the undead nearly twice as hard.",
            "Hold use with a hex staff to charge it: half a second for Charged, a second and a half for Fully Charged.",
            "Batter an enemy hull below half, then throw a grappling hook to board her.",
            "A Light refit makes your ship faster but fragile; a War refit adds guns and timber but slows her.",
            "Firebrand shot sets a ship burning. Bane shot tears through ghost ships.",
            "Press Y for quick chat: say what you need in a word, mid-fight.",
            "A gold ! over someone's head means your story wants you to talk to them.",
            "A yellow ! means someone in town has an errand for you; a green ? means they're waiting for you to come back.",
            "Lost? The radar's arrow points to your next goal. Press J for your journal and M for your chart.",
            "Warp stones by the notice boards carry you between the ports.",
            "Living stumps burn. Bring a firebomb.",
            "Snapjaw flytraps can't move. Don't stand next to one.",
            "Sea monsters prowl far from land, especially at night. The Reach Serpent rams hulls.",
            "Your friends list (/friends) shows who's aboard and lets you go to them, if they're ashore.",
            "Wear a title you've earned with /title.",
            "Festivals bring visitors to the towns. Their tokens buy trinkets, chests and titles (/festival).",
            "Wrecks lie off the islands with their decks above water. The captain waits in the hold.",
            "Duels and fights with other pirates raise your land infamy; sinking their ships raises your sea infamy.",
            "Guilds are for life (/guild); crews are for sailing (/crew).",
            "Talk to the shipwright to trade up to a bigger ship.",
            "Patch your hull with planks. Sneak and punch her to stow her in a deed."
    );

    public static String random(Random r) {
        return TIPS.get(r.nextInt(TIPS.size()));
    }

    /** The Pirate Code, for the journal. */
    public static List<String> code() {
        return List.of(
                "§71. Every pirate keeps what they plunder, and pays their crew their share.",
                "§72. No pirate shall strike a crewmate. (The sea won't let you anyway.)",
                "§73. A duel ends when one yields. Leave them their life.",
                "§74. The Navy hangs pirates. Keep your infamy low near Crownwatch.",
                "§75. Help a pirate in trouble; you'll be one yourself one day.",
                "§76. Keep the lights burning. Something under the Reach is listening.",
                "§8Tips: hold Alt aboard for sailing skills; R on foot to lock on; Y for quick chat; /friends, /guild, /title, /festival."
        );
    }
}
