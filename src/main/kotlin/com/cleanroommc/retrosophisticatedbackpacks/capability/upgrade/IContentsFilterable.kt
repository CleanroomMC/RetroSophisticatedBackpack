package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

interface IContentsFilterable : IBasicFilterable {
    companion object {
        const val FILTER_BY_STORAGE_TAG = "FilterByStorage"
    }

    var contentsFilterType: ContentsFilterType

    enum class ContentsFilterType {
        ALLOW,
        BLOCK,
        STORAGE;

        fun next(): ContentsFilterType =
            entries[(ordinal + 1) % entries.size]
    }
}
