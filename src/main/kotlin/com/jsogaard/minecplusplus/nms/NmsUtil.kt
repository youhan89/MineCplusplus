package com.jsogaard.minecplusplus.nms

import org.bukkit.Bukkit

object NmsUtil {
    val NMS_VERSION: String by lazy {
        try {
            Bukkit.getServer().javaClass.getPackage().name.substring(23)
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}