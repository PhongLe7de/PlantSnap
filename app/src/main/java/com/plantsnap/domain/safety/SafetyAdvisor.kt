package com.plantsnap.domain.safety

import com.plantsnap.domain.models.Edibility
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.models.ToxicityLevel

sealed interface SafetyAlert {
    data class PetToxicity(
        val pet: Pet,
        val level: ToxicityLevel,
        val symptoms: String?,
    ) : SafetyAlert

    data class ForagingCaution(
        val reason: Reason,
        val guidance: String?,
    ) : SafetyAlert

    enum class Pet { DOG, CAT }
    enum class Reason { INEDIBLE, TOXIC_TO_HUMANS, GENERAL_CAUTION }
}

object SafetyAdvisor {

    private const val PET_DOG = "DOG"
    private const val PET_CAT = "CAT"
    private const val PET_BOTH = "BOTH"
    private const val INTEREST_EDIBLE = "EDIBLE"

    fun evaluate(info: PlantAiInfo?, profile: SupabaseProfile?): List<SafetyAlert> {
        val safety = info?.safety ?: return emptyList()
        if (profile == null) return emptyList()

        val alerts = mutableListOf<SafetyAlert>()

        val hasDog = profile.petType == PET_DOG || profile.petType == PET_BOTH
        val hasCat = profile.petType == PET_CAT || profile.petType == PET_BOTH

        if (hasDog) {
            val dogLevel = safety.dog?.level
            if (dogLevel != null && dogLevel.isActionable()) {
                alerts += SafetyAlert.PetToxicity(
                    pet = SafetyAlert.Pet.DOG,
                    level = dogLevel,
                    symptoms = safety.dog.symptoms,
                )
            }
        }

        if (hasCat) {
            val catLevel = safety.cat?.level
            if (catLevel != null && catLevel.isActionable()) {
                alerts += SafetyAlert.PetToxicity(
                    pet = SafetyAlert.Pet.CAT,
                    level = catLevel,
                    symptoms = safety.cat.symptoms,
                )
            }
        }

        val foragesWild = profile.plantInterests?.contains(INTEREST_EDIBLE) == true
        if (foragesWild) {
            val foragingAlert = computeForagingAlert(safety)
            if (foragingAlert != null) alerts += foragingAlert
        }

        return alerts
    }

    private fun computeForagingAlert(safety: com.plantsnap.domain.models.SafetyInfo): SafetyAlert.ForagingCaution? {
        val edibility = safety.edibility
        val humanLevel = safety.human?.level
        val notes = safety.foragingNotes

        val reason = when {
            edibility == Edibility.TOXIC -> SafetyAlert.Reason.TOXIC_TO_HUMANS
            humanLevel == ToxicityLevel.MODERATE || humanLevel == ToxicityLevel.SEVERE ->
                SafetyAlert.Reason.TOXIC_TO_HUMANS
            edibility == Edibility.INEDIBLE -> SafetyAlert.Reason.INEDIBLE
            notes != null -> SafetyAlert.Reason.GENERAL_CAUTION
            else -> return null
        }

        val guidance = notes ?: safety.human?.symptoms
        return SafetyAlert.ForagingCaution(reason = reason, guidance = guidance)
    }

    private fun ToxicityLevel.isActionable(): Boolean =
        this == ToxicityLevel.MILD || this == ToxicityLevel.MODERATE || this == ToxicityLevel.SEVERE
}
