package com.chubbyboi.psychedelicraftresparked.commands;

import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CommandHallucinationDebug extends CommandBase {

    @Override
    public String getName() {
        return "hallucinate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/hallucinate <set|clear|status> [type] [value]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  set <type> <value> - force a slot active at an exact value"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  clear [type] - remove one or all overrides"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  status - print pool totals and all 14 slots"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  mindcolor <r> <g> <b>|clear - force/release the shared mind colour"));
            return;
        }

        HallucinationManager manager = HallucinationManager.getInstance();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "set":
                handleSet(manager, sender, args);
                break;
            case "clear":
                handleClear(manager, sender, args);
                break;
            case "status":
                handleStatus(manager, sender);
                break;
            case "mindcolor":
                handleMindColor(manager, sender, args);
                break;
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown subcommand: " + sub));
        }
    }

    private void handleMindColor(HallucinationManager manager, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {
            manager.clearDebugMindColor();
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Mind colour resuming natural wobble"));
            return;
        }

        if (args.length != 4) throw new WrongUsageException("/hallucinate mindcolor <r> <g> <b>|clear");

        float r = (float) parseDouble(args[1], 0.0, 1.0);
        float g = (float) parseDouble(args[2], 0.0, 1.0);
        float b = (float) parseDouble(args[3], 0.0, 1.0);

        manager.setDebugMindColor(r, g, b);
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN
                + String.format("Forced mind colour to (%.3f, %.3f, %.3f)", r, g, b)));
    }

    private void handleSet(HallucinationManager manager, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) throw new WrongUsageException("/hallucinate set <type> <value>");

        HallucinationManager.HallucinationType type = parseType(args[1]);
        float value = (float) parseDouble(args[2], 0.0, 5.0);

        manager.setDebugOverride(type, value);
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Forced " + type + " to " + String.format("%.3f", value)));
    }

    private void handleClear(HallucinationManager manager, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 2) {
            HallucinationManager.HallucinationType type = parseType(args[1]);
            manager.clearDebugOverride(type);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared override for " + type));
        } else {
            manager.clearAllDebugOverrides();
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared all hallucination overrides"));
        }
    }

    private void handleStatus(HallucinationManager manager, ICommandSender sender) {
        for (HallucinationManager.Pool pool : HallucinationManager.Pool.values()) {
            sender.sendMessage(new TextComponentString(TextFormatting.AQUA + pool.name() + " pool: " + TextFormatting.WHITE + String.format("%.3f", manager.getPoolValue(pool))));
        }

        float[] mindColor = manager.getCurrentMindColor();
        sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "mind colour: " + TextFormatting.WHITE + String.format("(%.3f, %.3f, %.3f)", mindColor[0], mindColor[1], mindColor[2]) + (manager.isMindColorForced() ? TextFormatting.LIGHT_PURPLE + " [forced]" : "")));

        for (HallucinationManager.HallucinationType type : HallucinationManager.HallucinationType.values()) {
            boolean active = manager.isActive(type);
            boolean forced = manager.isDebugForced(type);
            TextFormatting color = forced ? TextFormatting.LIGHT_PURPLE : (active ? TextFormatting.YELLOW : TextFormatting.GRAY);

            sender.sendMessage(new TextComponentString(color + "  " + type.name() + ": " + String.format("%.3f", manager.getEffectValue(type)) + (active ? " [active]" : "") + (forced ? " [forced]" : "")));
        }
    }

    private HallucinationManager.HallucinationType parseType(String name) throws CommandException {
        try {
            return HallucinationManager.HallucinationType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandException("Unknown hallucination type: " + name);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "set", "clear", "status", "mindcolor");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear"))) {
            List<String> names = new ArrayList<>();
            for (HallucinationManager.HallucinationType type : HallucinationManager.HallucinationType.values()) {
                names.add(type.name().toLowerCase());
            }
            return getListOfStringsMatchingLastWord(args, names);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return getListOfStringsMatchingLastWord(args, "0.0", "0.5", "1.0", "2.0");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("mindcolor")) {
            return getListOfStringsMatchingLastWord(args, "clear", "1.0");
        }

        if (args.length >= 3 && args.length <= 4 && args[0].equalsIgnoreCase("mindcolor")) {
            return getListOfStringsMatchingLastWord(args, "0.0", "1.0");
        }

        return Collections.emptyList();
    }
}
