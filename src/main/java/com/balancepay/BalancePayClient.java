package com.balancepay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalancePayClient implements ClientModInitializer {

    // ===== CONFIG =====
    /** Account that receives the money. */
    private static final String TARGET = ".lavishfoil6492";
    /** How often to check the scoreboard + pay (in seconds). */
    private static final int INTERVAL_SECONDS = 5;
    /** Don't send a /pay below this amount. */
    private static final long MIN_AMOUNT = 100;
    // ==================

    // Matches things like "$ 7.6K", "$7,600", "$1.2M"
    private static final Pattern MONEY = Pattern.compile("\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)");

    private static KeyBinding toggleKey;
    private static boolean enabled = false;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.balancepay.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.balancepay"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            tickCounter = 0;
            client.player.sendMessage(
                    Text.literal("[BalancePay] " + (enabled ? "ON" : "OFF")), true);
        }

        if (!enabled) return;

        if (++tickCounter < INTERVAL_SECONDS * 20) return;
        tickCounter = 0;

        long amount = readBalanceFromSidebar(client);
        if (amount < MIN_AMOUNT) return;

        client.player.networkHandler.sendChatCommand("pay " + TARGET + " " + amount);
    }

    /** Returns the balance (floored to a whole number) or -1 if not found. */
    private long readBalanceFromSidebar(MinecraftClient client) {
        Scoreboard sb = client.world.getScoreboard();
        ScoreboardObjective obj = sb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (obj == null) return -1;

        for (ScoreboardEntry entry : sb.getScoreboardEntries(obj)) {
            Team team = sb.getScoreHolderTeam(entry.owner());
            Text decorated = Team.decorateName(team, entry.name());
            long parsed = parseMoney(decorated.getString());
            if (parsed >= 0) return parsed;
        }
        return -1;
    }

    static long parseMoney(String line) {
        Matcher m = MONEY.matcher(line);
        if (!m.find()) return -1;
        try {
            double value = Double.parseDouble(m.group(1).replace(",", ""));
            switch (m.group(2).toLowerCase()) {
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
}
