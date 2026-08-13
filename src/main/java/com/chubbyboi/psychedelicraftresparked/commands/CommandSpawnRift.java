package com.chubbyboi.psychedelicraftresparked.commands;

import com.chubbyboi.psychedelicraftresparked.entities.EntityRealityRift;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandSpawnRift extends CommandBase {

    @Override
    public String getName() {
        return "spawnrift";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/spawnrift [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
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
            throw new WrongUsageException(getUsage(sender));
        }

        EntityRealityRift rift = new EntityRealityRift(sender.getEntityWorld());
        rift.setPosition(x, y, z);
        sender.getEntityWorld().spawnEntity(rift);

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN
                + String.format("Spawned a Reality Rift at %.1f, %.1f, %.1f", x, y, z)));
    }
}