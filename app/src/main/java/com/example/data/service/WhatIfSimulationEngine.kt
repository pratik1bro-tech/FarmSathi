package com.example.data.service

import com.example.data.models.FarmFieldParcel
import com.example.data.models.FieldHealthStatus
import com.example.data.models.WhatIfScenario
import com.example.data.models.WhatIfSimulationResult
import com.example.ui.theme.*

class WhatIfSimulationEngine {

    fun simulateScenario(
        field: FarmFieldParcel,
        scenario: WhatIfScenario
    ): WhatIfSimulationResult {
        return when (scenario) {
            WhatIfScenario.DELAY_IRRIGATION_3_DAYS -> {
                val predictedMoisture = (field.soilMoisturePct - 18.0).coerceAtLeast(18.0)
                val yieldImpact = -7.2
                WhatIfSimulationResult(
                    scenario = scenario,
                    fieldName = field.fieldName,
                    currentMoisturePct = field.soilMoisturePct,
                    predictedSoilMoisturePct = predictedMoisture,
                    moistureChangeLabel = "-18.0% moisture drop across 45cm root zone",
                    cropStressLevel = "High Stomatal Stress (0.72 Index)",
                    cropStressColor = FarmWarningAmber,
                    estimatedYieldImpactPct = yieldImpact,
                    yieldImpactLabel = "-7.2% Potential Yield Deficit",
                    yieldImpactColor = FarmAlertRed,
                    assumptionsList = listOf(
                        "Daily evapotranspiration rate ETc = 5.8 mm/day based on canopy temperature.",
                        "Soil water depletion exceeds 50% available water capacity (AWC) in Vertisol clay.",
                        "No unseasonal atmospheric rainfall occurs during the 72-hour window."
                    ),
                    uncertaintyMargin = "±3.5% variance depending on nighttime dew condensation and humidity.",
                    AIAdvice = "Delaying irrigation will cause mild wilting and lower pod filling density. Recommended to schedule evening drip cycle before Day 2."
                )
            }

            WhatIfScenario.IRRIGATE_TODAY -> {
                val predictedMoisture = (field.soilMoisturePct + 16.0).coerceAtMost(78.0)
                val yieldImpact = +4.5
                WhatIfSimulationResult(
                    scenario = scenario,
                    fieldName = field.fieldName,
                    currentMoisturePct = field.soilMoisturePct,
                    predictedSoilMoisturePct = predictedMoisture,
                    moistureChangeLabel = "+16.0% root-zone saturation (Optimal 68%)",
                    cropStressLevel = "Optimal Hydration (0.08 Stress Index)",
                    cropStressColor = FarmSuccessGreen,
                    estimatedYieldImpactPct = yieldImpact,
                    yieldImpactLabel = "+4.5% Yield Gain (Optimal Pod Mass)",
                    yieldImpactColor = FarmPrimaryGreen,
                    assumptionsList = listOf(
                        "25mm drip application delivered uniformly over 3.5 hours at 1.8 L/hr per emitter.",
                        "Deep percolation losses minimized under drip emitter lateral spacing.",
                        "Root nutrient uptake efficiency increases by 18% under optimal moisture matrix."
                    ),
                    uncertaintyMargin = "±1.8% variance based on drip line pressure uniformity.",
                    AIAdvice = "Irrigating today restores moisture to peak field capacity (68%), supporting maximum photo-assimilate translocation into pods."
                )
            }

            WhatIfScenario.HEAVY_RAINFALL -> {
                val predictedMoisture = (field.soilMoisturePct + 28.0).coerceAtMost(92.0)
                val yieldImpact = -5.8
                WhatIfSimulationResult(
                    scenario = scenario,
                    fieldName = field.fieldName,
                    currentMoisturePct = field.soilMoisturePct,
                    predictedSoilMoisturePct = predictedMoisture,
                    moistureChangeLabel = "+28.0% waterlogging risk (>85% saturation)",
                    cropStressLevel = "Anaerobic Root Stress (0.65 Index)",
                    cropStressColor = FarmAlertRed,
                    estimatedYieldImpactPct = yieldImpact,
                    yieldImpactLabel = "-5.8% Drainage Yield Risk",
                    yieldImpactColor = FarmAlertRed,
                    assumptionsList = listOf(
                        "45mm torrential rainfall event exceeds soil infiltration capacity (12 mm/hr).",
                        "Surface run-off and temporary waterlogging in low-lying furrow pockets.",
                        "Increased root oxygen deficit and elevated fungal spore proliferation (Phytophthora)."
                    ),
                    uncertaintyMargin = "±4.8% variance based on field slope drainage efficiency.",
                    AIAdvice = "Heavy rainfall saturates root zone. Ensure perimeter drainage channels are cleared to prevent root rot and nitrogen leaching."
                )
            }

            WhatIfScenario.EXTREME_HEATWAVE -> {
                val predictedMoisture = (field.soilMoisturePct - 24.0).coerceAtLeast(15.0)
                val yieldImpact = -11.4
                WhatIfSimulationResult(
                    scenario = scenario,
                    fieldName = field.fieldName,
                    currentMoisturePct = field.soilMoisturePct,
                    predictedSoilMoisturePct = predictedMoisture,
                    moistureChangeLabel = "-24.0% rapid canopy evapotranspiration loss",
                    cropStressLevel = "Severe Thermal Stress (0.88 Index)",
                    cropStressColor = FarmAlertRed,
                    estimatedYieldImpactPct = yieldImpact,
                    yieldImpactLabel = "-11.4% Severe Heat Loss (Flower Abortion)",
                    yieldImpactColor = FarmAlertRed,
                    assumptionsList = listOf(
                        "Continuous 40°C canopy air temperatures increase vapor pressure deficit (VPD > 3.2 kPa).",
                        "Accelerated stomatal closure reduces carbon fixation during peak sunlight hours.",
                        "High thermal degradation of pollen viability during flowering stage."
                    ),
                    uncertaintyMargin = "±4.2% variance based on microclimate wind velocity.",
                    AIAdvice = "Extreme heat causes flower drop. Execute pulse micro-irrigation at midday to cool the crop canopy temperature by 3–4°C."
                )
            }
        }
    }
}
