package com.plantsnap.ui.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.R
import com.plantsnap.ui.components.OnBoardingHeroSection

enum class PetType(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val drawableRes: Int? = null,
    val imageVector: ImageVector? = null,
) {
    DOG(R.string.onboarding_pets_option_dog, drawableRes = R.drawable.dog_icon),
    CAT(R.string.onboarding_pets_option_cat, drawableRes = R.drawable.cat_icon),
    BOTH(R.string.onboarding_pets_option_both, imageVector = Icons.Filled.Pets),
    NONE(R.string.onboarding_pets_option_none, imageVector = Icons.Filled.Check),
}

@Composable
fun PetSafetyPage(
    selectedPets: PetType?,
    onSelectPets: (PetType) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        OnBoardingHeroSection(
            imageRes = R.drawable.funny_looking_kitten,
            titleRes = R.string.onboarding_pets_title,
        )

        // Subtitle + chips
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_pets_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                lineHeight = 24.sp,
            )

            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PetType.entries.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { option ->
                            PetOptionButton(
                                labelRes = option.labelRes,
                                drawableRes = option.drawableRes,
                                imageVector = option.imageVector,
                                isSelected = selectedPets == option,
                                onClick = { onSelectPets(option) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PetOptionButton(
    @StringRes labelRes: Int,
    @DrawableRes drawableRes: Int? = null,
    imageVector: ImageVector? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val containerColor = if (isSelected) scheme.primary else scheme.surfaceContainerLow
    val contentColor = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
    val painter = imageVector?.let { rememberVectorPainter(it) }
        ?: painterResource(drawableRes!!)

    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
            .semantics {
                selected = isSelected
                role = Role.RadioButton
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
        }
    }
}
