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


package com.minecraft.moonlake.compasstracker.manager;

import com.minecraft.moonlake.compasstracker.CompassTrackerPlugin;
import com.minecraft.moonlake.compasstracker.task.CompassTrackerTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CompassTrackerManager {

    private final CompassTrackerPlugin main;
    private final ConcurrentHashMap<String, CompassTrackerTask> taskMap;

    public CompassTrackerManager(CompassTrackerPlugin main) {
        this.main = main;
        this.taskMap = new ConcurrentHashMap<>();
    }

    public CompassTrackerPlugin getMain() {
        return main;
    }

    public void close() {
        // 关闭缓存 map
        taskMap.values().stream().filter(Objects::nonNull).forEach(CompassTrackerTask::cancel);
        // 清除缓存
        taskMap.clear();
    }

    public void close(@Nonnull Player player) {
        // 关闭玩家的缓存
        CompassTrackerTask task = taskMap.containsKey(player.getName())? taskMap.remove(player.getName()) : null;

        if(task != null)
            task.cancel();
    }

    public void close(@Nonnull Player player, @Nullable CompassTrackerTask task) {
        // 关闭玩家的指南针跟踪器 task
        if(task != null)
            task.cancel();
        // 清除缓存 map 的玩家对象
        if(taskMap.containsKey(player.getName()))
            taskMap.remove(player.getName());
    }

    protected CompassTrackerTask getTask(Player player) {
        // 获取玩家的指南针跟踪器 task 对象
        return player != null && taskMap.containsKey(player.getName()) ? taskMap.get(player.getName()) : null;
    }

    protected void initTask(Player player) {
        // 初始化玩家的指南针跟踪器 task
        CompassTrackerTask task = getTask(player);

        if(task == null) {
            // 只有为 null 则进行初始化并 put 到缓存 map
            task = new CompassTrackerTask(this, player);
            task.runTaskTimerAsynchronously(getMain(), 0L, getTaskPeriod());
            taskMap.put(player.getName(), task);
        }
    }

    public long getTaskPeriod() {
        // 获取指南针跟踪器的更新周期
        return getMain().getConfig().getLong("UpdatePeriod", 20L);
    }

    public String getMessage(String target, double distance) {
        // 获取指南针跟踪器交互 bar 消息
        String message = getMain().getConfig().getString("ActionBarMessage", "玩家: %target% 距离: %distance%");
        message = message.replaceAll("%target%", target == null ? "" : target).replaceAll("%distance%", String.valueOf((int) distance));
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void handlerItemHeld(@Nonnull Player player, @Nonnull ItemStack itemStack) {
        // 处理玩家手持物品
        if(itemStack.getType() != Material.COMPASS)
            // 不为指南针则 close 指南针跟踪器 task
            close(player);
        else
            handlerCompassHeld(player);
    }

    protected void handlerCompassHeld(@Nonnull Player player) {
        // 处理玩家手持指南针

        // 初始化指南针跟踪器 task
        initTask(player);
    }
}
