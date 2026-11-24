package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.astro.LalKitabAndStoreEngine
import com.example.core.AppLanguage
import com.example.core.ContactConfig
import com.example.core.tr
import com.example.model.KundaliResult
import com.example.model.StoreCategory
import com.example.ui.components.GoldBorderedCard
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusPill
import com.example.ui.theme.AuspiciousGreen
import com.example.ui.theme.DarkBrandBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StoreScreen(
    language: AppLanguage,
    activeChart: KundaliResult?,
    selectedCategory: StoreCategory?,
    cartIds: Set<String>,
    onSelectCategory: (StoreCategory?) -> Unit,
    onToggleCart: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val recommendedIds = remember(activeChart) {
        if (activeChart != null) LalKitabAndStoreEngine.recommendedIdsForChart(activeChart.summary) else emptySet()
    }
    val itemsList = remember(selectedCategory) {
        if (selectedCategory == null) LalKitabAndStoreEngine.catalog
        else LalKitabAndStoreEngine.catalog.filter { it.category == selectedCategory }
    }
    val totalInr = LalKitabAndStoreEngine.catalog.filter { it.id in cartIds }.sumOf { it.priceInr }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize().testTag("store_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = tr(language, "रत्न, रुद्राक्ष एवं यंत्र भंडार", "Gemstone, Rudraksha & Yantra Store", "宝石・金刚菩提・法印商店"),
                        color = DarkBrandBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text(tr(language, "सभी", "All", "全部")) }
                        )
                        FilterChip(
                            selected = selectedCategory == StoreCategory.GEMSTONE,
                            onClick = { onSelectCategory(StoreCategory.GEMSTONE) },
                            label = { Text(tr(language, "रत्न (Gems)", "Gemstones", "宝石")) }
                        )
                        FilterChip(
                            selected = selectedCategory == StoreCategory.RUDRAKSHA,
                            onClick = { onSelectCategory(StoreCategory.RUDRAKSHA) },
                            label = { Text(tr(language, "रुद्राक्ष", "Rudraksha", "金刚菩提")) }
                        )
                        FilterChip(
                            selected = selectedCategory == StoreCategory.YANTRA,
                            onClick = { onSelectCategory(StoreCategory.YANTRA) },
                            label = { Text(tr(language, "यंत्र", "Yantras", "法印")) }
                        )
                    }

                    if (cartIds.isNotEmpty()) {
                        GoldBorderedCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${tr(language, "चयनित सामग्री", "Selected Inquiry Items", "已选法物")}: ${cartIds.size} • ₹$totalInr",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBrandBlue
                                )
                                Button(
                                    onClick = {
                                        val helpline = ContactConfig.helplines.first()
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${helpline.phoneDial}")))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBrandBlue, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tr(language, "अभिमंत्रित रत्न ऑर्डर हेतु कॉल करें (+91 7275547797)", "Call Guruji to Order Energized Items (+91 7275547797)", "致电预订开光圣物 (+91 7275547797)"))
                                }
                            }
                        }
                    }

                    SectionTitle(
                        title = tr(
                            language,
                            "कुंडली-सुरक्षित एवं अभिमंत्रित संग्रह",
                            "Chart-Safe Energized Collection",
                            "依命盘严选开光馆藏"
                        )
                    )
                }
            }

            items(itemsList, key = { it.id }) { item ->
                val isRec = item.id in recommendedIds
                val inCart = item.id in cartIds
                GoldBorderedCard(modifier = Modifier.widthIn(max = 680.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.EN) item.nameEn else item.nameHi,
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkBrandBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "₹${item.priceInr}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isRec) {
                            StatusPill(
                                text = tr(language, "कुंडली के अनुकूल (सुझावित)", "Safe & Recommended for Active Chart", "符合当前星盘推荐"),
                                isPositive = true
                            )
                        }
                        Text(
                            text = if (language == AppLanguage.EN) item.subtitleEn else item.subtitleHi,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (language == AppLanguage.EN) item.benefitEn else item.benefitHi,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${tr(language, "मंत्र", "Mantra", "真言")}: ${item.mantra}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AuspiciousGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (language == AppLanguage.EN) item.wearingMethodEn else item.wearingMethodHi,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { onToggleCart(item.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("store_toggle_${item.id}")
                        ) {
                            Icon(if (inCart) Icons.Outlined.CheckCircle else Icons.Outlined.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (inCart) tr(language, "सूची से हटाएँ", "Added to Inquiry", "已加入清单")
                                else tr(language, "अभिमंत्रित ऑर्डर सूची में जोड़ें", "Add to Order Inquiry", "加入预订清单")
                            )
                        }
                    }
                }
            }
        }
    }
}
