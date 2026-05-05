package com.plantsnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plantsnap.R
import java.util.Locale

@Composable
fun SafetyDisclaimerBanner(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.errorContainer),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = scheme.error.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = scheme.onErrorContainer,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = scheme.onErrorContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onErrorContainer,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
fun ConfidenceBadge(
    score: Float,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .offset(x = (-12).dp, y = 24.dp)
            .background(scheme.primary, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(scheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = scheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = scheme.inversePrimary,
                fontSize = 9.sp,
            )
            Text(
                text = "%.1f%%".format(score * 100),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.onPrimary,
            )
        }
    }
}

@Composable
fun RetakeCTABox(
    title: String,
    body: String,
    buttonText: String,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.primary)
            .padding(32.dp),
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(scheme.primaryContainer.copy(alpha = 0.2f), CircleShape),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetake,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                ),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = buttonText.uppercase(Locale.getDefault()),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    trailingContent: @Composable () -> Unit = { Spacer(Modifier.size(40.dp)) },
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.background)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = scheme.surfaceContainerHigh,
            ),
            modifier = Modifier.clip(CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detail_back),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        trailingContent()
    }
}

@Composable
fun AiLoadingRow(
    loadingText: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = scheme.primary,
        )
        Text(
            text = loadingText,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
fun RetryButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.detail_retry))
    }
}

@Composable
fun SmallLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(top = 8.dp)
            .size(16.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.primary,
    )
}
