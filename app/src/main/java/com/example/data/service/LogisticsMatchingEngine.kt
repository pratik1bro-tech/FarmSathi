package com.example.data.service

import com.example.data.models.LogisticsRouteRequest
import com.example.data.models.PooledFarmerInfo
import com.example.data.models.SharedTransportPool
import com.example.data.models.VehicleType

class LogisticsMatchingEngine {

    fun computeLogisticsMatch(request: LogisticsRouteRequest): SharedTransportPool {
        val distanceKm = 42.0
        val vehicle = request.preferredVehicle
        
        // Base solo cost calculation
        val baseSoloCost = (distanceKm * vehicle.baseRatePerKmInr) + 800.0 // Base loading/toll fee
        val roundSoloCost = (Math.round(baseSoloCost / 50.0) * 50).toDouble()

        // Pooled farmers matching criteria
        val pooledFarmers = listOf(
            PooledFarmerInfo(
                anonymousName = "Farmer R. K.",
                crop = request.cropName,
                quantityQuintals = 20.0,
                pickupRegionLabel = "Sanwer North Sector",
                approximateDistanceKm = 1.8
            ),
            PooledFarmerInfo(
                anonymousName = "Farmer V. P.",
                crop = request.cropName,
                quantityQuintals = 15.0,
                pickupRegionLabel = "Kanadiya Village Hub",
                approximateDistanceKm = 2.4
            )
        )

        val totalPooledQty = request.quantityQuintals + pooledFarmers.sumOf { it.quantityQuintals }
        
        // Shared transport cost formula (Split fixed transport cost by weight fraction + 15% route detour incentive)
        val routeDetourCost = 350.0
        val totalSharedVehicleCost = baseSoloCost + routeDetourCost
        val userFraction = request.quantityQuintals / totalPooledQty
        val userSharedCost = totalSharedVehicleCost * userFraction
        
        val roundSharedCost = (Math.round(userSharedCost / 50.0) * 50).toDouble()
        val savings = (roundSoloCost - roundSharedCost).coerceAtLeast(0.0)
        val savingsPct = if (roundSoloCost > 0) ((savings / roundSoloCost) * 100).toInt() else 0

        return SharedTransportPool(
            poolId = "POOL_IND_SANWER_882",
            destinationName = request.destinationMandi,
            cropGroup = request.cropName,
            dispatchDate = request.dispatchDate,
            vehicle = vehicle,
            totalCapacityQuintals = vehicle.capacityQuintals,
            currentBookedQuintals = totalPooledQty,
            userQuantityQuintals = request.quantityQuintals,
            pooledFarmers = pooledFarmers,
            normalSoloCostInr = roundSoloCost,
            sharedPoolCostInr = roundSharedCost,
            estimatedSavingsInr = savings,
            savingsPercentage = savingsPct,
            pickupRegionRadius = "3.0 km Sanwer Corridor",
            totalDistanceKm = distanceKm,
            driverName = "Suresh Patel",
            driverPhone = "+91 98260 XXXXX",
            vehicleNumber = "MP 09 GH 4102",
            departureWindow = "06:30 AM – 08:00 AM"
        )
    }

    fun getAlternativePools(request: LogisticsRouteRequest): List<SharedTransportPool> {
        val primaryMatch = computeLogisticsMatch(request)
        
        val secondaryPool = primaryMatch.copy(
            poolId = "POOL_DEWAS_SANWER_319",
            destinationName = "Dewas APMC Sub-Mandi",
            vehicle = VehicleType.TRACTOR_TROLLEY,
            totalDistanceKm = 36.0,
            normalSoloCostInr = 2800.0,
            sharedPoolCostInr = 1650.0,
            estimatedSavingsInr = 1150.0,
            savingsPercentage = 41,
            driverName = "Rajesh Verma",
            vehicleNumber = "MP 41 AA 1092"
        )

        return listOf(primaryMatch, secondaryPool)
    }
}
