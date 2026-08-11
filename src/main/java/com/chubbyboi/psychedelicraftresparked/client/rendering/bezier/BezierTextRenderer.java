package com.chubbyboi.psychedelicraftresparked.client.rendering.bezier;

import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;

// Draws a string one character at a time along a BezierPath - the Rift Jar's "This is a small spiral." beam effect.
public class BezierTextRenderer {

    private String text;
    private boolean spreadToFill;
    private double shift;
    private double capBottom;
    private double capTop = 1.0;

    public void setText(String text) {
        this.text = text;
    }

    public void setSpreadToFill(boolean spreadToFill) {
        this.spreadToFill = spreadToFill;
    }

    public void setShift(double shift) {
        this.shift = shift;
    }

    public void setCapBottom(double capBottom) {
        this.capBottom = capBottom;
    }

    public void setCapTop(double capTop) {
        this.capTop = capTop;
    }

    // fontRenderer is passed in rather than cached - Minecraft.standardGalacticFontRenderer isn't populated yet when TESRs are constructed (mod init runs before that field is set), so caching it at construction is null.
    public void render(BezierPath path, FontRenderer fontRenderer) {
        if (path.isDirty()) {
            path.buildDistances();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        String plainText = "";
        ArrayList<String> modifiers = new ArrayList<>();
        modifiers.add("");

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (character == '§' && i + 1 < text.length()) {
                modifiers.set(modifiers.size() - 1, modifiers.get(modifiers.size() - 1) + text.substring(i, i + 2));
                i++;
            } else {
                plainText = plainText + character;
                modifiers.add(modifiers.get(modifiers.size() - 1));
            }
        }

        for (int i = 0; i < plainText.length(); i++) {
            int charIndex = plainText.length() - i - 1;
            char character = plainText.charAt(charIndex);

            if (character != ' ') {
                double totalProgress = (spreadToFill ? ((double) i / (double) text.length()) : (i * 0.5)) + shift;
                double finalProgress = ((totalProgress % 1.0) + 1.0) % 1.0;

                if (finalProgress >= capBottom && finalProgress <= capTop) {
                    BezierPathStep cachedStep = path.getCachedStep(finalProgress);
                    double[] position = cachedStep.getPosition();
                    double[] rotation = path.getNaturalRotation(cachedStep, 0.01);

                    double red = PsychMathHelper.mix(cachedStep.getLeftPoint().getRed(), cachedStep.getRightPoint().getRed(), cachedStep.getInnerProgress());
                    double green = PsychMathHelper.mix(cachedStep.getLeftPoint().getGreen(), cachedStep.getRightPoint().getGreen(), cachedStep.getInnerProgress());
                    double blue = PsychMathHelper.mix(cachedStep.getLeftPoint().getBlue(), cachedStep.getRightPoint().getBlue(), cachedStep.getInnerProgress());

                    double textSize = PsychMathHelper.mix(cachedStep.getLeftPoint().getFontSize(), cachedStep.getRightPoint().getFontSize(), cachedStep.getInnerProgress());

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(position[0], position[1], position[2]);
                    GlStateManager.scale(-textSize / 12.0, -textSize / 12.0, -textSize / 12.0);
                    GlStateManager.rotate((float) rotation[0] + 180.0f, 0.0f, 1.0f, 0.0f);
                    GlStateManager.rotate((float) rotation[1], 1.0f, 0.0f, 0.0f);
                    fontRenderer.drawString(modifiers.get(charIndex) + character, 0, 0, ((int) (red * 255.0) << 16) + ((int) (green * 255.0) << 8) + ((int) (blue * 255.0)));
                    GlStateManager.popMatrix();
                }
            }
        }

        GlStateManager.disableBlend();
    }
}