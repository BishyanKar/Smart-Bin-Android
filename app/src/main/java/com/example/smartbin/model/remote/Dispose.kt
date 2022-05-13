package com.example.smartbin.model.remote

data class Dispose(private val binId: String, private val location: Array<Double?>, private val wasteType: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dispose

        if (binId != other.binId) return false
        if (!location.contentEquals(other.location)) return false
        if (wasteType != other.wasteType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = binId.hashCode()
        result = 31 * result + location.contentHashCode()
        result = 31 * result + wasteType.hashCode()
        return result
    }
}