package com.plantsnap

import com.plantsnap.domain.models.Edibility
import com.plantsnap.domain.models.PetToxicity
import com.plantsnap.domain.models.PlantAiInfo
import com.plantsnap.domain.models.SafetyInfo
import com.plantsnap.domain.models.SupabaseProfile
import com.plantsnap.domain.models.ToxicityLevel
import com.plantsnap.domain.safety.SafetyAdvisor
import com.plantsnap.domain.safety.SafetyAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyAdvisorTest {

    private fun profile(
        petType: String? = null,
        interests: List<String>? = null,
    ) = SupabaseProfile(
        userId = "user-1",
        createdAt = "2026-01-01T00:00:00Z",
        onboardingCompleted = true,
        petType = petType,
        plantInterests = interests,
        experienceLevel = "BEGINNER",
    )

    private fun aiInfo(safety: SafetyInfo?) = PlantAiInfo(safety = safety)

    @Test
    fun `DOG profile with severe dog toxicity emits one PetToxicity alert`() {
        val info = aiInfo(
            SafetyInfo(
                dog = PetToxicity(level = ToxicityLevel.SEVERE, symptoms = "Vomiting, lethargy"),
            )
        )
        val result = SafetyAdvisor.evaluate(info, profile(petType = "DOG"))

        assertEquals(1, result.size)
        val alert = result.first() as SafetyAlert.PetToxicity
        assertEquals(SafetyAlert.Pet.DOG, alert.pet)
        assertEquals(ToxicityLevel.SEVERE, alert.level)
        assertEquals("Vomiting, lethargy", alert.symptoms)
    }

    @Test
    fun `CAT profile with no cat toxicity emits no alert`() {
        val info = aiInfo(SafetyInfo(cat = PetToxicity(level = ToxicityLevel.NONE)))

        val result = SafetyAdvisor.evaluate(info, profile(petType = "CAT"))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `BOTH profile with mixed severities emits two pet alerts`() {
        val info = aiInfo(
            SafetyInfo(
                dog = PetToxicity(level = ToxicityLevel.MODERATE, symptoms = "Drooling"),
                cat = PetToxicity(level = ToxicityLevel.MILD, symptoms = "Mild irritation"),
            )
        )

        val result = SafetyAdvisor.evaluate(info, profile(petType = "BOTH"))

        assertEquals(2, result.size)
        val dogAlert = result.filterIsInstance<SafetyAlert.PetToxicity>().first { it.pet == SafetyAlert.Pet.DOG }
        val catAlert = result.filterIsInstance<SafetyAlert.PetToxicity>().first { it.pet == SafetyAlert.Pet.CAT }
        assertEquals(ToxicityLevel.MODERATE, dogAlert.level)
        assertEquals(ToxicityLevel.MILD, catAlert.level)
    }

    @Test
    fun `NONE profile with no edible interest emits nothing`() {
        val info = aiInfo(
            SafetyInfo(
                dog = PetToxicity(level = ToxicityLevel.SEVERE),
                cat = PetToxicity(level = ToxicityLevel.SEVERE),
                edibility = Edibility.TOXIC,
            )
        )

        val result = SafetyAdvisor.evaluate(info, profile(petType = "NONE"))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `EDIBLE interest with inedible plant emits foraging inedible alert`() {
        val info = aiInfo(SafetyInfo(edibility = Edibility.INEDIBLE))

        val result = SafetyAdvisor.evaluate(info, profile(interests = listOf("EDIBLE")))

        assertEquals(1, result.size)
        val alert = result.first() as SafetyAlert.ForagingCaution
        assertEquals(SafetyAlert.Reason.INEDIBLE, alert.reason)
    }

    @Test
    fun `EDIBLE interest with edible plant emits nothing`() {
        val info = aiInfo(SafetyInfo(edibility = Edibility.EDIBLE))

        val result = SafetyAdvisor.evaluate(info, profile(interests = listOf("EDIBLE")))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `null profile guest emits nothing`() {
        val info = aiInfo(
            SafetyInfo(
                dog = PetToxicity(level = ToxicityLevel.SEVERE),
                edibility = Edibility.TOXIC,
            )
        )

        val result = SafetyAdvisor.evaluate(info, null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `null safety emits nothing`() {
        val info = aiInfo(null)

        val result = SafetyAdvisor.evaluate(info, profile(petType = "BOTH", interests = listOf("EDIBLE")))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `DOG with EDIBLE interest and toxic plant emits both alerts`() {
        val info = aiInfo(
            SafetyInfo(
                dog = PetToxicity(level = ToxicityLevel.SEVERE, symptoms = "Seizures"),
                edibility = Edibility.TOXIC,
                foragingNotes = "Resembles wild carrot but is deadly.",
            )
        )

        val result = SafetyAdvisor.evaluate(
            info,
            profile(petType = "DOG", interests = listOf("EDIBLE", "OUTDOOR")),
        )

        assertEquals(2, result.size)
        val pet = result.filterIsInstance<SafetyAlert.PetToxicity>().single()
        val foraging = result.filterIsInstance<SafetyAlert.ForagingCaution>().single()
        assertEquals(SafetyAlert.Pet.DOG, pet.pet)
        assertEquals(ToxicityLevel.SEVERE, pet.level)
        assertEquals(SafetyAlert.Reason.TOXIC_TO_HUMANS, foraging.reason)
        assertEquals("Resembles wild carrot but is deadly.", foraging.guidance)
    }

    @Test
    fun `EDIBLE interest with only foraging notes emits general caution`() {
        val info = aiInfo(
            SafetyInfo(
                edibility = Edibility.UNKNOWN,
                human = PetToxicity(level = ToxicityLevel.MILD),
                foragingNotes = "Only consume after thorough cooking.",
            )
        )

        val result = SafetyAdvisor.evaluate(info, profile(interests = listOf("EDIBLE")))

        assertEquals(1, result.size)
        val foraging = result.first() as SafetyAlert.ForagingCaution
        assertEquals(SafetyAlert.Reason.GENERAL_CAUTION, foraging.reason)
        assertEquals("Only consume after thorough cooking.", foraging.guidance)
    }
}
