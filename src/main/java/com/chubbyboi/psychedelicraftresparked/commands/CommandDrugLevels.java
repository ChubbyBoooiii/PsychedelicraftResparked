package com.chubbyboi.psychedelicraftresparked.commands;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSyncDrugProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDrugLevels extends CommandBase {

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

    @Override
    public String getName() {
        return "druglevels";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/druglevels <check|set|clear|list> [args...]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
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
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /druglevels check <player>"));
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
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /druglevels set <player> <drug> <strength>"));
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
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /druglevels clear <player>"));
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

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
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
}