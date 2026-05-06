package com.plantsnap.domain.models

enum class CareTaskType {
    WATER,
    FERTILIZE,
    MIST,
    ROTATE,
    REPOT;

    companion object {
        fun fromName(name: String): CareTaskType? = entries.firstOrNull { it.name == name }

        /** Fallback cadence when the user enables a task without a Gemini-suggested cadence. */
        fun defaultCadenceDays(type: CareTaskType): Int = when (type) {
            WATER -> 7
            FERTILIZE -> 30
            MIST -> 3
            ROTATE -> 14
            REPOT -> 365
        }
    }
}
