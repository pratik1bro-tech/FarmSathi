package com.example.data.models

import java.time.LocalDate

enum class VehicleType(
    val title: String,
    val capacityQuintals: Double,
    val baseRatePerKmInr: Double,
    val iconEmoji: String
) {
    MINI_TRUCK("Tata Ace / Chhota Hathi", 15.0, 35.0, "🛺"),
    LCV_TRUCK("Eicher 10.90 / 14ft LCV", 50.0, 55.0, "🚚"),
    HEAVY_TRUCK("10-Tyre Heavy Hauler", 150.0, 95.0, "🚛"),
    TRACTOR_TROLLEY("Tractor Trolley (Double Axle)", 40.0, 45.0, "🚜")
}

data class PooledFarmerInfo(
    val anonymousName: String,
    val crop: String,
    val quantityQuintals: Double,
    val pickupRegionLabel: String,
    val approximateDistanceKm: Double
)

data class SharedTransportPool(
    val poolId: String,
    val destinationName: String,
    val cropGroup: String,
    val dispatchDate: String,
    val vehicle: VehicleType,
    val totalCapacityQuintals: Double,
    val currentBookedQuintals: Double,
    val userQuantityQuintals: Double,
    val pooledFarmers: List<PooledFarmerInfo>,
    val normalSoloCostInr: Double,
    val sharedPoolCostInr: Double,
    val estimatedSavingsInr: Double,
    val savingsPercentage: Int,
    val pickupRegionRadius: String,
    val totalDistanceKm: Double,
    val driverName: String,
    val driverPhone: String,
    val vehicleNumber: String,
    val departureWindow: String
)

data class LogisticsRouteRequest(
    val farmPickupAddress: String = "Malwa Green Organic Farm, Kanadiya, Sanwer",
    val pickupRegion: String = "Sanwer Sub-block (3 km radius)",
    val destinationMandi: String = "Indore APMC Main Grain Yard",
    val cropName: String = "Soybean (JS 20-34)",
    val quantityQuintals: Double = 35.0,
    val dispatchDate: String = "22 Aug 2026",
    val preferredVehicle: VehicleType = VehicleType.LCV_TRUCK
)
