package com.cleanroommc.retrosophisticatedbackpacks.backpack

enum class DisplaySide(val serializedName: String) {
    FRONT("front"),
    LEFT("left"),
    RIGHT("right");

    fun next(): DisplaySide =
        entries[(ordinal + 1) % entries.size]

    fun previous(): DisplaySide =
        entries[(ordinal + entries.size - 1) % entries.size]

    companion object {
        fun fromName(name: String): DisplaySide =
            entries.firstOrNull { it.serializedName == name } ?: FRONT
    }
}
