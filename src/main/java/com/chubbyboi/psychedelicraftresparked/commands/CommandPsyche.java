package com.chubbyboi.psychedelicraftresparked.commands;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.HallucinationManager.HallucinationType;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import com.chubbyboi.psychedelicraftresparked.entities.EntityRealityRift;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketHallucinationDebug;
import com.chubbyboi.psychedelicraftresparked.network.PacketSyncDrugProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandPsyche extends CommandBase {

    @Override
    public String getName() {
        return "psyche";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/psyche <druglevels|spawnrift|hallucinate> [args...]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  druglevels <check|set|clear|list> - Check or change players' drug levels"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  spawnrift [x y z] - Spawn a Reality Rift"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  hallucinate <set|clear|status|mindcolor> <player> - Force a player's hallucinations"));
            return;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (args[0].toLowerCase()) {
            case "druglevels":
                executeDrugLevels(server, sender, subArgs);
                break;
            case "spawnrift":
                executeSpawnRift(sender, subArgs);
                break;
            case "hallucinate":
                executeHallucinate(server, sender, subArgs);
                break;
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown subcommand: " + args[0]));
                break;
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "druglevels", "spawnrift", "hallucinate");
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (args[0].toLowerCase()) {
            case "druglevels":
                return drugLevelsTabCompletions(server, sender, subArgs);
            case "spawnrift":
                return subArgs.length <= 3 ? getTabCompletionCoordinate(subArgs, 0, targetPos) : Collections.emptyList();
            case "hallucinate":
                return hallucinateTabCompletions(server, subArgs);
            default:
                return Collections.emptyList();
        }
    }

    // ==================== druglevels ====================

    private void executeDrugLevels(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /psyche druglevels <check|set|clear|list> [args...]"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  check <player> - Check drug levels"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  set <player> <drug> <strength> - Set player's drug level"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  clear <player> - Clear all drug effects"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  list - List known drug types"));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "check":
                handleCheck(server, sender, args);
                break;
            case "set":
                handleSet(server, sender, args);
                break;
            case "clear":
                handleClear(server, sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown subcommand: " + subCommand));
                break;
        }
    }

    private void handleCheck(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /psyche druglevels check <player>"));
            return;
        }

        EntityPlayer target = getPlayer(server, sender, args[1]);

        IDrugProperties props = target.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to get drug properties!"));
            return;
        }

        DrugProperties drugProps = (DrugProperties) props;
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Drug levels for " + target.getName() + ":"));

        boolean hasAnyEffects = false;
        for (IDrug drug : drugProps.getAllDrugs()) {
            float strength = drug.getActiveValue();
            if (strength > 0.001f) {
                double decaySpeedPlus = drug.getDecaySpeedPlus();

                double ticksToZero = strength / decaySpeedPlus;
                double secondsToZero = ticksToZero / 20.0;
                double minutesToZero = secondsToZero / 60.0;

                String timeString;
                if (minutesToZero >= 1.0) {
                    timeString = String.format("~%.1fmin", minutesToZero);
                } else {
                    timeString = String.format("~%.0fs", secondsToZero);
                }

                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  " + drug.getName() + ": " + TextFormatting.WHITE + String.format("%.3f", strength) + TextFormatting.GRAY + " (decays to 0 in " + timeString + ")"));
                hasAnyEffects = true;
            }
        }

        if (!hasAnyEffects) {
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "  No active drug effects"));
        }
    }

    private void handleSet(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!PSConfig.drugEffectsEnabled) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Drug effects are disabled on this server (drugEffectsEnabled = false)."));
            return;
        }

        if (args.length != 4) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /psyche druglevels set <player> <drug> <strength>"));
            return;
        }

        EntityPlayer target = getPlayer(server, sender, args[1]);
        String drugType = args[2].toLowerCase();
        float strength = (float) parseDouble(args[3], 0.0, 1.0);

        IDrugProperties props = target.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (props == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to get drug properties!"));
            return;
        }

        if (props instanceof DrugProperties) {
            DrugProperties drugProps = (DrugProperties) props;
            IDrug drug = drugProps.getDrug(drugType);
            if (drug != null) {
                drug.setDesiredValue(strength);
                drug.setActiveValue(strength);
            }
        } else {
            props.setDrugStrength(drugType, strength);
        }

        syncToClient(target, props);

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set " + target.getName() + "'s " + drugType + " level to " + String.format("%.3f", strength)
        ));
    }

    private void handleClear(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /psyche druglevels clear <player>"));
            return;
        }

        EntityPlayer target = getPlayer(server, sender, args[1]);

        IDrugProperties props = target.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Failed to get drug properties!"));
            return;
        }

        DrugProperties drugProps = (DrugProperties) props;
        drugProps.clearAll();
        syncToClient(target, props);

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared all drug effects for " + target.getName()));
    }

    private void handleList(ICommandSender sender) throws CommandException {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        List<String> drugs = getRegisteredDrugs(player);

        if (drugs.isEmpty()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No drugs registered!"));
            return;
        }

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Registered drug types:"));
        for (String drug : drugs) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  - " + drug));
        }
    }

    private void syncToClient(EntityPlayer player, IDrugProperties props) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        PacketSyncDrugProperties packet = new PacketSyncDrugProperties(props);
        NetworkHandler.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
    }

    private static List<String> getRegisteredDrugs(EntityPlayer player) {
        List<String> drugList = new ArrayList<>();

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (props instanceof DrugProperties) {
            DrugProperties drugProps = (DrugProperties) props;
            for (IDrug drug : drugProps.getAllDrugs()) {
                drugList.add(drug.getName());
            }
        }

        return drugList;
    }

    private List<String> drugLevelsTabCompletions(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "check", "set", "clear", "list");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear"))) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            try {
                EntityPlayer player = getCommandSenderAsPlayer(sender);
                List<String> drugs = getRegisteredDrugs(player);
                return getListOfStringsMatchingLastWord(args, drugs);
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            return getListOfStringsMatchingLastWord(args, "0", "0.5", "1.0");
        }

        return Collections.emptyList();
    }

    // ==================== spawnrift ====================

    private void executeSpawnRift(ICommandSender sender, String[] args) throws CommandException {
        double x, y, z;

        if (args.length == 0) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            x = player.posX;
            y = player.posY;
            z = player.posZ;
        } else if (args.length == 3) {
            BlockPos base = sender.getPosition();
            x = parseDouble(base.getX(), args[0], true);
            y = parseDouble(base.getY(), args[1], true);
            z = parseDouble(base.getZ(), args[2], true);
        } else {
            throw new WrongUsageException("/psyche spawnrift [x y z]");
        }

        EntityRealityRift rift = new EntityRealityRift(sender.getEntityWorld());
        rift.setPosition(x, y, z);
        sender.getEntityWorld().spawnEntity(rift);

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN
                + String.format("Spawned a Reality Rift at %.1f, %.1f, %.1f", x, y, z)));
    }

    // ==================== hallucinate ====================

    private void executeHallucinate(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /psyche hallucinate <set|clear|status|mindcolor> <player> [args...]"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  set <player> <type> <value> - force a slot active at an exact value"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  clear <player> [type] - remove one or all overrides"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  status <player> - print pool totals and all slots (in that player's chat)"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  mindcolor <player> <r> <g> <b>|clear - force/release the shared mind colour"));
            return;
        }

        String sub = args[0].toLowerCase();
        EntityPlayerMP target = getPlayer(server, sender, args[1]);

        switch (sub) {
            case "set": {
                if (args.length != 4) throw new WrongUsageException("/psyche hallucinate set <player> <type> <value>");

                HallucinationType type = parseType(args[2]);
                float value = (float) parseDouble(args[3], 0.0, 5.0);

                sendHallucination(target, PacketHallucinationDebug.Action.SET, type.name(), value, 0.0f, 0.0f);
                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Forced " + type + " to " + String.format("%.3f", value) + " for " + target.getName()));
                break;
            }
            case "clear": {
                if (args.length >= 3) {
                    HallucinationType type = parseType(args[2]);
                    sendHallucination(target, PacketHallucinationDebug.Action.CLEAR, type.name(), 0.0f, 0.0f, 0.0f);
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared override for " + type + " on " + target.getName()));
                } else {
                    sendHallucination(target, PacketHallucinationDebug.Action.CLEAR_ALL, "", 0.0f, 0.0f, 0.0f);
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared all hallucination overrides for " + target.getName()));
                }
                break;
            }
            case "status": {
                sendHallucination(target, PacketHallucinationDebug.Action.STATUS, "", 0.0f, 0.0f, 0.0f);
                if (target != sender.getCommandSenderEntity()) {
                    sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Hallucination status printed in " + target.getName() + "'s chat"));
                }
                break;
            }
            case "mindcolor": {
                if (args.length == 3 && args[2].equalsIgnoreCase("clear")) {
                    sendHallucination(target, PacketHallucinationDebug.Action.MIND_COLOR_CLEAR, "", 0.0f, 0.0f, 0.0f);
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Mind colour resuming natural wobble for " + target.getName()));
                    return;
                }

                if (args.length != 5) throw new WrongUsageException("/psyche hallucinate mindcolor <player> <r> <g> <b>|clear");

                float r = (float) parseDouble(args[2], 0.0, 1.0);
                float g = (float) parseDouble(args[3], 0.0, 1.0);
                float b = (float) parseDouble(args[4], 0.0, 1.0);

                sendHallucination(target, PacketHallucinationDebug.Action.MIND_COLOR, "", r, g, b);
                sender.sendMessage(new TextComponentString(TextFormatting.GREEN
                        + String.format("Forced %s's mind colour to (%.3f, %.3f, %.3f)", target.getName(), r, g, b)));
                break;
            }
            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown subcommand: " + sub));
        }
    }

    private void sendHallucination(EntityPlayerMP target, PacketHallucinationDebug.Action action, String type, float x, float y, float z) {
        NetworkHandler.INSTANCE.sendTo(new PacketHallucinationDebug(action, type, x, y, z), target);
    }

    private HallucinationType parseType(String name) throws CommandException {
        try {
            return HallucinationType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommandException("Unknown hallucination type: " + name);
        }
    }

    private List<String> hallucinateTabCompletions(MinecraftServer server, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "set", "clear", "status", "mindcolor");
        }

        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        String sub = args[0];

        if (args.length == 3 && (sub.equalsIgnoreCase("set") || sub.equalsIgnoreCase("clear"))) {
            List<String> names = new ArrayList<>();
            for (HallucinationType type : HallucinationType.values()) {
                names.add(type.name().toLowerCase());
            }
            return getListOfStringsMatchingLastWord(args, names);
        }

        if (args.length == 4 && sub.equalsIgnoreCase("set")) {
            return getListOfStringsMatchingLastWord(args, "0.0", "0.5", "1.0", "2.0");
        }

        if (args.length == 3 && sub.equalsIgnoreCase("mindcolor")) {
            return getListOfStringsMatchingLastWord(args, "clear", "1.0");
        }

        if (args.length >= 4 && args.length <= 5 && sub.equalsIgnoreCase("mindcolor")) {
            return getListOfStringsMatchingLastWord(args, "0.0", "1.0");
        }

        return Collections.emptyList();
    }
}