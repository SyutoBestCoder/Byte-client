package dev.blend.util

import net.minecraft.client.Minecraft

interface IAccessor {
    val mc get() = Minecraft.getInstance()!!
}