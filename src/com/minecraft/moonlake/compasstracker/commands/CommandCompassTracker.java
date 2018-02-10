/*
 * Copyright (C) 2016 The MoonLake Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.minecraft.moonlake.compasstracker.commands;

import com.minecraft.moonlake.compasstracker.CompassTrackerPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandCompassTracker implements CommandExecutor {

    private final CompassTrackerPlugin main;

    public CommandCompassTracker(CompassTrackerPlugin main) {
        this.main = main;
    }

    public CompassTrackerPlugin getMain() {
        return main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            printHelp(sender);
        } else if(args.length == 1) {
            if(!sender.hasPermission("moonlake.compasstracker.reload")) {
                sender.sendMessage(getMain().getMessage("NoPermission"));
                return true;
            }
            getMain().reloadConfig();
            getMain().reloadPrefix();
            sender.sendMessage(getMain().getMessage("CompassTrackerChatReload"));
        } else {
            sender.sendMessage(getMain().getMessage("ErrorCommandArgs", "/compasstracker help 查看命令帮助."));
        }
        return true;
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage(new String[] {
                getMain().toColor("&b&l&m          &d CompassTracker &7By &6Month_Light &b&l&m          "),
                getMain().toColor("&6/compasstracker help &7- 查看插件命令帮助."),
                getMain().toColor("&6/compasstracker reload &7- 重新载入插件配置文件."),
        });
    }
}
