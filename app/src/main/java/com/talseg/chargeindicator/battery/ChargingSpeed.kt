package com.talseg.chargeindicator.battery

enum class ChargingSpeed {
    NONE, VERY_SLOW, SLOW, FAST, SUPER_FAST;

    companion object {
        // Thresholds chosen from measurements on a Xiaomi 11 Lite 5G NE:
        // PC USB 2.5 W, slow charger 8.0 W, Quick Charge 13.8 W, Mi Turbo Charge 23.4 W.
        private const val SLOW_FROM_WATTS = 5.0
        private const val FAST_FROM_WATTS = 11.0
        private const val SUPER_FAST_FROM_WATTS = 18.0

        fun classify(chargerType: ChargerType, maxChargingWatts: Double?): ChargingSpeed = when {
            chargerType == ChargerType.NONE -> NONE
            // Device doesn't report charger power: fall back to the connection type.
            maxChargingWatts == null -> if (chargerType == ChargerType.USB) VERY_SLOW else SLOW
            maxChargingWatts < SLOW_FROM_WATTS -> VERY_SLOW
            maxChargingWatts < FAST_FROM_WATTS -> SLOW
            maxChargingWatts < SUPER_FAST_FROM_WATTS -> FAST
            else -> SUPER_FAST
        }
    }
}
