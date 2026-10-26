package com.pfe.mamacareplus.ui.pregnancy

object BabyDevelopmentImageCatalog {

    @Suppress("UNUSED_PARAMETER")
    fun imageUrlFor(week: Int, mode: ComparisonMode, comparisonName: String): String {
        val folder = when (mode) {
            ComparisonMode.FRUIT -> "fruit"
            ComparisonMode.ANIMAL -> "animal"
            ComparisonMode.OBJECT -> "object"
        }
        val fileName = "week_${week.toString().padStart(2, '0')}.png"
        return "asset://baby-development-images/$folder/$fileName"
    }
}
