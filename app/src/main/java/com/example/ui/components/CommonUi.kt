package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.Dignity
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.AuspiciousGreenBg
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.CautionAmberBg

@Composable
fun LanguageAndSupportActions(
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onHelpClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("language_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = "Change Language",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                AppLanguage.entries.forEach { lang ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = lang.nativeName,
                                fontWeight = if (lang == language) FontWeight.Bold else FontWeight.Normal,
                                color = if (lang == language) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onLanguageSelected(lang)
                        },
                        modifier = Modifier.testTag("lang_option_${lang.code}")
                    )
                }
            }
        }

        IconButton(
            onClick = onHelpClick,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("help_button")
        ) {
            Icon(
                imageVector = Icons.Outlined.HeadsetMic,
                contentDescription = "Help & Support",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CelestialHeroHeader(
    language: AppLanguage,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val bannerHeight = if (compact) 82.dp else 144.dp
    val emblemSize = if (compact) 52.dp else 76.dp
    val contentPadding = if (compact) 10.dp else 16.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 16.dp else 22.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_lotus_mandala_banner),
                contentDescription = AstroStrings.appTitle(language),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = contentPadding, vertical = if (compact) 8.dp else contentPadding)
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_astro_emblem),
                    contentDescription = AstroStrings.appTitle(language),
                    modifier = Modifier
                        .size(emblemSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(if (compact) 10.dp else 14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AstroStrings.appTitle(language),
                        style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        color = com.example.ui.theme.DarkBrandBlue,
                        fontWeight = FontWeight.Bold,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
                    Text(
                        text = AstroStrings.appSubtitle(language),
                        style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun GoldBorderedCard(
    modifier: Modifier = Modifier,
    containerColor: Color = com.example.ui.theme.LightBlueCard,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
fun DignityBadge(dignity: Dignity, language: AppLanguage) {
    val (bg, fg) = when (dignity) {
        Dignity.EXALTED, Dignity.MOOLTRIKONA, Dignity.OWN_SIGN -> AuspiciousGreenBg to AuspiciousGreen
        Dignity.DEBILITATED, Dignity.ENEMY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        Dignity.FRIEND -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        Dignity.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = AstroStrings.dignityLabel(dignity, language),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (isPositive) AuspiciousGreenBg else CautionAmberBg,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPositive) AuspiciousGreen else CautionAmber,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
