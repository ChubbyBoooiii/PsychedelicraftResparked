package com.chubbyboi.psychedelicraftresparked.drug;

import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;

import java.util.Random;

public class MessageDistorter {

    private static final String[] FILLER_WORDS = {", like, ", "... like, ", ", uhm, ", ", uhhhh, "};
    private static final String[] START_FILLER_WORDS = {"Dude, ", "Dood, ", "Dewd, ", "Dude, like, ", "Dood, like, ", "Dewd, like, ", "Yeah... ", "And, "};

    private MessageDistorter() { }

    public static String distort(String message, Random random, float alcohol, float zero, float cannabis) {
        StringBuilder builder = new StringBuilder();

        float randomCaseChance = PsychMathHelper.zeroToOne(alcohol, 0.3f, 1.0f) * 0.06f + PsychMathHelper.zeroToOne(zero, 0.0f, 0.3f);
        float randomLetterChance = PsychMathHelper.zeroToOne(alcohol, 0.5f, 1.0f) * 0.015f;
        float sToShChance = PsychMathHelper.zeroToOne(alcohol, 0.2f, 0.6f);
        float longShChance = alcohol * 0.8f;
        float hicChance = PsychMathHelper.zeroToOne(alcohol, 0.5f, 1.0f) * 0.04f;
        float rewindChance = PsychMathHelper.zeroToOne(alcohol, 0.4f, 0.9f) * 0.03f;
        float longCharChance = PsychMathHelper.zeroToOne(alcohol, 0.3f, 1.0f) * 0.025f;

        float oneZeroChance = PsychMathHelper.zeroToOne(zero, 0.6f, 0.95f);
        float randomCharChance = PsychMathHelper.zeroToOne(zero, 0.2f, 0.95f);

        float fillerWordChance = PsychMathHelper.zeroToOne(cannabis, 0.2f, 0.95f) * 0.1f;
        float startFillerWordChance = PsychMathHelper.zeroToOne(cannabis, 0.2f, 0.95f) * 0.7f;

        boolean wasPoint = true;
        for (int i = 0; i < message.length(); i++) {
            char curChar = message.charAt(i);

            if (random.nextFloat() < oneZeroChance) {
                curChar = random.nextBoolean() ? '0' : '1';
            } else if (random.nextFloat() < randomCharChance) {
                curChar = (char) (' ' + random.nextInt('~' - ' ' + 1));
            } else if (random.nextFloat() < randomLetterChance) {
                curChar = (char) ((random.nextBoolean() ? 'a' : 'A') + random.nextInt(26));
            } else if (random.nextFloat() < randomCaseChance) {
                if (random.nextBoolean()) {
                    curChar = Character.isUpperCase(curChar) ? Character.toLowerCase(curChar) : Character.toUpperCase(curChar);
                }
            }

            if ((curChar == 's' || curChar == 'S') && random.nextFloat() < sToShChance) {
                builder.append(curChar).append(random.nextFloat() < longShChance ? "hh" : "h");
            } else if (curChar == ' ' && random.nextFloat() < fillerWordChance) {
                builder.append(FILLER_WORDS[random.nextInt(FILLER_WORDS.length)]);
            } else if (wasPoint && random.nextFloat() < startFillerWordChance) {
                builder.append(START_FILLER_WORDS[random.nextInt(START_FILLER_WORDS.length)]).append(curChar);
            } else {
                builder.append(curChar);
            }

            wasPoint = false;

            if (random.nextFloat() < longCharChance) {
                float moreChance = 0.6f * 2.0f;
                do {
                    moreChance *= 0.5f;
                    builder.append(curChar);
                } while (random.nextFloat() < moreChance);
            }

            if (random.nextFloat() < hicChance) {
                builder.append("*hic*");
            }

            if (random.nextFloat() < rewindChance) {
                builder.append("... ");
                int wordsRewind = random.nextInt(5) + 1;
                for (int j = 0; j < wordsRewind; j++) {
                    i = message.lastIndexOf(" ", i - 1);
                }
                if (i < 0) {
                    i = 0;
                }
            }
        }

        return builder.toString();
    }
}