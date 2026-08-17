package com.chubbyboi.psychedelicraftresparked.client.rendering;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Random;

public class RastaHeadChatBot {

    private static final String NAME = "Reggie";

    private static final int IDLE_TICKS_BEFORE_ROLL = 300; // 15s
    private static final int IDLE_ROLL_CHANCE_DENOMINATOR = 300; // ~15s average on top
    private static final int REPLY_DELAY_MIN_TICKS = 20; // 1s
    private static final int REPLY_DELAY_MAX_TICKS = 60; // 3s

    private static final String[][] RANDOM_STATEMENTS = {
        {"Did you ever notice, like...", "Sorry, I forgot what I was trying to say"},
        {"Want another joint? I got so many, like, 2 or 3 more..."},
        {"Duuuude...", "DuuuuuuuuuuuuuuuuuuDe!", "DuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuDe!", "Yo dudeliodude!"},
        {"Woah, like, woah", "That's, like, so, rad, dude"},
        {"You know what would go excellently with this?", "DIRT"},
        {"Like, hear me out", "Just this once", "Come on, come oon"},
        {"I am SO high right now", "Look at me flyyyyy..."},
        {"OoOoOOowOoOoOo", "SpooOoOoOoOky floating Head"},
        {"Creeper!"},
        {"Behind you"},
        {"Get him"},
        {"Ahahahahaaaa"},
        {"My minds is, like, so opened right now", "I'm like a can of beans", "If you... yknow..Used a can opener on me", "I'm like. My mind is like an opened can of beans", "Like a can of beans you opened"},
        {"You not gonna eat my beans, are you?"},
        {"Don't look now, but I think this guy's a little bit sus"},
        {"It's aaaaalll natural, babey"}
    };

    private static final String[][] RESPONSES_TO_PLAYER = {
        {"Haha, you're so right."},
        {"Yes."},
        {"Great idea"},
        {"Wooooooooah", "That's, like, dude", "Are you, like, a genie or something?"},
        {"Wooooooah"},
        {"My minds is, like, so opened right now"}
    };

    private static final String[][] RESPONSES_OTHER = {
        {"Haha, they're so right."},
        {"This guy, I like this guy"},
        {"Don't look now, but I think this guy's a little sus"}
    };

    private final EntityPlayerSP player;
    private final Random random;

    private int idleTicks;
    private int sleepTicks;
    private final Deque<String> messageQueue = new ArrayDeque<>();

    public RastaHeadChatBot(EntityPlayerSP player) {
        this.player = player;
        this.random = player.getRNG();
    }

    public void tick() {
        if (sleepTicks-- > 0) {
            return;
        }

        if (messageQueue.isEmpty() && idleTicks++ > IDLE_TICKS_BEFORE_ROLL && random.nextInt(IDLE_ROLL_CHANCE_DENOMINATOR) == 0) {
            queueLines(RANDOM_STATEMENTS[random.nextInt(RANDOM_STATEMENTS.length)]);
            idleTicks = 0;
        }

        String line = messageQueue.poll();
        if (line != null) {
            player.sendMessage(new TextComponentString("<" + NAME + "> " + line));
            sleepTicks = random.nextInt(19) + 2;
        }
    }

    public void onMessageReceived(String sender) {
        if (NAME.equals(sender) || random.nextFloat() >= 0.4f) {
            return;
        }

        messageQueue.clear();
        idleTicks = 0;
        queueLines(sender.equals(player.getName()) ? RESPONSES_TO_PLAYER[random.nextInt(RESPONSES_TO_PLAYER.length)] : RESPONSES_OTHER[random.nextInt(RESPONSES_OTHER.length)]);
        sleepTicks = random.nextInt(REPLY_DELAY_MAX_TICKS - REPLY_DELAY_MIN_TICKS + 1) + REPLY_DELAY_MIN_TICKS;
    }

    private void queueLines(String[] lines) {
        Collections.addAll(messageQueue, lines);
    }
}