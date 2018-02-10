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


package com.minecraft.moonlake.compasstracker.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PacketPlayOutChat {

    private final static String VERSION;
    private final static Class<?> CLASS_PACKETPLAYOUTCHAT;
    private final static Class<?> CLASS_CHATSERIALIZER;
    private final static Class<?> CLASS_ICHATBASECOMPONENT;
    private final static Class<?> CLASS_PACKET;
    private final static Class<?> CLASS_CRAFTPLAYER;
    private final static Class<?> CLASS_ENTITYPLAYER;
    private final static Class<?> CLASS_PLAYERCONNECTION;
    private final static Method METHOD_CHARSERIALIZER_A;
    private final static Method METHOD_GETHANDLE;
    private final static Method METHOD_SENDPACKET;
    private final static Field FIELD_PLAYERCONNECTION;

    static {
        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            String[] packageSplit = packageName.split("\\.");
            VERSION = packageSplit[packageSplit.length - 1];

            String start = "net.minecraft.server." + VERSION + ".";

            CLASS_PACKETPLAYOUTCHAT = Class.forName(start + "PacketPlayOutChat");
            CLASS_ICHATBASECOMPONENT = Class.forName(start + "IChatBaseComponent");
            CLASS_CHATSERIALIZER = Class.forName(start + "IChatBaseComponent$ChatSerializer");
            CLASS_PACKET = Class.forName(start + "Packet");
            CLASS_CRAFTPLAYER = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            CLASS_ENTITYPLAYER = Class.forName(start + "EntityPlayer");
            CLASS_PLAYERCONNECTION = Class.forName(start + "PlayerConnection");

            METHOD_GETHANDLE = CLASS_CRAFTPLAYER.getMethod("getHandle");
            METHOD_SENDPACKET = CLASS_PLAYERCONNECTION.getMethod("sendPacket", CLASS_PACKET);
            METHOD_CHARSERIALIZER_A = CLASS_CHATSERIALIZER.getDeclaredMethod("a", String.class);

            FIELD_PLAYERCONNECTION = CLASS_ENTITYPLAYER.getField("playerConnection");
            FIELD_PLAYERCONNECTION.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException("The nms packet play out chat reflect raw initialize exception.", e);
        }
    }

    private String message;
    private Mode mode;

    public PacketPlayOutChat(String message) {
        this(message, Mode.CHAT);
    }

    public PacketPlayOutChat(String message, Mode mode) {
        this.message = message;
        this.mode = mode;
    }

    public String getMessage() {
        return message;
    }

    public Mode getMode() {
        return mode;
    }

    public void send(Player player) throws RuntimeException {
        try {
            Object nmsChat = METHOD_CHARSERIALIZER_A.invoke(null, "{\"text\":\"" + message + "\"}");
            Object packet = CLASS_PACKETPLAYOUTCHAT.getConstructor(CLASS_ICHATBASECOMPONENT, Byte.TYPE).newInstance(nmsChat, mode == null ? (byte) 1 : mode.getMode());

            METHOD_SENDPACKET.invoke(FIELD_PLAYERCONNECTION.get(METHOD_GETHANDLE.invoke(player)), packet);
        }
        catch (Exception e) {
            throw new RuntimeException("The nms packet play out chat send exception.", e);
        }
    }

    public enum Mode {

        /**
         * 聊天: 聊天栏位置
         */
        CHAT((byte)0),
        /**
         * 系统消息: 聊天栏位置
         */
        SYSTEM((byte)1),
        /**
         * 快捷栏: 快捷栏上面位置
         */
        HOTBAR((byte)2),
        ;

        private byte mode;

        Mode(byte mode) {
            this.mode = mode;
        }

        public byte getMode() {
            return mode;
        }
    }
}
