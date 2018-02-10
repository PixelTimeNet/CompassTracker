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


package com.minecraft.moonlake.compasstracker.task;

import com.minecraft.moonlake.compasstracker.manager.CompassTrackerManager;
import com.minecraft.moonlake.compasstracker.util.PacketPlayOutChat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompassTrackerTask extends BukkitRunnable {

    private final CompassTrackerManager manager;
    private final Player player;

    public CompassTrackerTask(CompassTrackerManager manager, Player player) {
        this.manager = manager;
        this.player = player;
    }

    public CompassTrackerManager getManager() {
        return manager;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isHeldCompass() {
        // 获取此玩家是否手持指南针物品
        ItemStack itemStack = player.getItemInHand();
        return itemStack != null && itemStack.getType() == Material.COMPASS;
    }

    private Player getClosest() {
        // 获取离此玩家最近的一个目标玩家
        List<Player> targetList = new ArrayList<>();

        // 遍历玩家当前世界的玩家列表, 不为此玩家则添加到临时列表
        getPlayer().getWorld().getPlayers().stream().filter((target) -> !target.equals(getPlayer())).forEach(targetList::add);

        if(targetList.isEmpty())
            // 如果列表为空则返回 null
            return null;

        return Collections.min(targetList, (target1, target2) -> {
            // 获取距离此玩家最近的目标玩家
            double target1Distance = getDistance(target1);
            double target2Distance = getDistance(target2);
            return target2Distance > target1Distance ? 1 : target2Distance > target1Distance ? -1 : 0;
        });
    }

    private double getDistance(Player target) {
        // 获取目标与此玩家的距离值
        return target.getLocation().distance(getPlayer().getLocation());
    }

    private void sendMessage() {
        // 发送指南针跟踪器的消息
        Player target = getClosest();

        if(target == null)
            return;
        // 只有目标不为 null 则发送
        double distance = getDistance(target);
        // 发送消息到玩家
        new PacketPlayOutChat(
                getManager().getMessage(target.getName(), distance),
                PacketPlayOutChat.Mode.HOTBAR)
        .send(getPlayer());
        // 更新玩家指南针的目标
        getPlayer().setCompassTarget(target.getLocation());
    }

    @Override
    public void run() {
        // 更新玩家的指南针跟踪器
        if(!isHeldCompass()) {
            // 如果没有手持指南针则关闭
            getManager().close(getPlayer(), this);
            return;
        }
        // 否则手持指南针则更新
        sendMessage();
    }
}
