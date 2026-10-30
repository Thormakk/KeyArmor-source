package com.thormakk.keyarmor.presentation.screens.vehicle

import com.thormakk.keyarmor.R

object VehicleBrandProvider {

    fun drawableResForBrand(make: String): Int? {
        if (make.isBlank()) return null
        val normalized = make.trim().uppercase().replace(Regex("[^A-Z0-9]"), "")
        return brandDrawables[normalized]
    }

    private val brandDrawables: Map<String, Int> = buildMap {
        fun put(res: Int, vararg keys: String) = keys.forEach { this[it] = res }

        put(R.drawable.ic_car_acura,       "ACURA")
        put(R.drawable.ic_car_alfaromeo,   "ALFAROMEO", "ALFA")
        put(R.drawable.ic_car_astonmartin, "ASTONMARTIN", "ASTON")
        put(R.drawable.ic_car_audi,        "AUDI")
        put(R.drawable.ic_car_bmw,         "BMW")
        put(R.drawable.ic_car_byd,         "BYD")
        put(R.drawable.ic_car_bentley,     "BENTLEY")
        put(R.drawable.ic_car_bugatti,     "BUGATTI")
        put(R.drawable.ic_car_buick,       "BUICK")
        put(R.drawable.ic_car_cadillac,    "CADILLAC")
        put(R.drawable.ic_car_chevrolet,   "CHEVROLET", "CHEVY")
        put(R.drawable.ic_car_chrysler,    "CHRYSLER")
        put(R.drawable.ic_car_dodge,       "DODGE")
        put(R.drawable.ic_car_ferrari,     "FERRARI")
        put(R.drawable.ic_car_fiat,        "FIAT")
        put(R.drawable.ic_car_ford,        "FORD")
        put(R.drawable.ic_car_gmc,         "GMC")
        put(R.drawable.ic_car_genesis,     "GENESIS")
        put(R.drawable.ic_car_honda,       "HONDA")
        put(R.drawable.ic_car_hummer,      "HUMMER")
        put(R.drawable.ic_car_hyundai,     "HYUNDAI")
        put(R.drawable.ic_car_infiniti,    "INFINITI", "INFINITY")
        put(R.drawable.ic_car_jaguar,      "JAGUAR")
        put(R.drawable.ic_car_jeep,        "JEEP")
        put(R.drawable.ic_car_kia,         "KIA")
        put(R.drawable.ic_car_koenigsegg,  "KOENIGSEGG")
        put(R.drawable.ic_car_lamborghini, "LAMBORGHINI", "LAMBO")
        put(R.drawable.ic_car_landrover,   "LANDROVER", "RANGEROVER")
        put(R.drawable.ic_car_lexus,       "LEXUS")
        put(R.drawable.ic_car_lincoln,     "LINCOLN")
        put(R.drawable.ic_car_lotus,       "LOTUS")
        put(R.drawable.ic_car_lucid,       "LUCID")
        put(R.drawable.ic_car_mercedesbenz,"MERCEDESBENZ", "MERCEDES", "BENZ")
        put(R.drawable.ic_car_maserati,    "MASERATI")
        put(R.drawable.ic_car_mazda,       "MAZDA")
        put(R.drawable.ic_car_mclaren,     "MCLAREN")
        put(R.drawable.ic_car_mini,        "MINI")
        put(R.drawable.ic_car_mitsubishi,  "MITSUBISHI")
        put(R.drawable.ic_car_nissan,      "NISSAN")
        put(R.drawable.ic_car_pagani,      "PAGANI")
        put(R.drawable.ic_car_polestar,    "POLESTAR")
        put(R.drawable.ic_car_porsche,     "PORSCHE")
        put(R.drawable.ic_car_ram,         "RAM", "DODGERAM")
        put(R.drawable.ic_car_rivian,      "RIVIAN")
        put(R.drawable.ic_car_rollsroyce,  "ROLLSROYCE")
        put(R.drawable.ic_car_subaru,      "SUBARU")
        put(R.drawable.ic_car_tesla,       "TESLA")
        put(R.drawable.ic_car_toyota,      "TOYOTA")
        put(R.drawable.ic_car_vinfast,     "VINFAST")
        put(R.drawable.ic_car_volkswagen,  "VOLKSWAGEN", "VW")
        put(R.drawable.ic_car_volvo,       "VOLVO")
    }
}
