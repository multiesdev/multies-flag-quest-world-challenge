package com.multies.flagquest.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.multies.flagquest.data.local.entity.AchievementEntity
import com.multies.flagquest.data.local.entity.AtlasEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.ui.components.FlagQuestBottomNavigation
import com.multies.flagquest.ui.localization.Locales
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtlasScreen(
    lang: String,
    countries: List<Country>,
    discoveries: List<AtlasEntity>,
    onBack: () -> Unit,
    navController: NavController? = null,
    onToggleFavorite: (String) -> Unit = {},
    onRelatedQuizSelected: (String) -> Unit = {},
    allAchievements: List<AchievementEntity> = emptyList(),
    initialCountryId: String? = null
) {
    var selectedCountry by remember { mutableStateOf<Country?>(null) }

    LaunchedEffect(initialCountryId, countries) {
        if (!initialCountryId.isNullOrEmpty()) {
            val matched = countries.find { it.id.equals(initialCountryId, ignoreCase = true) }
            if (matched != null) {
                selectedCountry = matched
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedContinentFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyDiscovered by remember { mutableStateOf(false) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    
    // Editable inclusion policies for disputed territories and self-governing regions
    var includePartiallyRecognized by remember { mutableStateOf(true) }
    var includeSelfGoverning by remember { mutableStateOf(true) }
    var showPolicyDialog by remember { mutableStateOf(false) }
    
    // Map vs Grid fallback selection toggle
    var useMapRepresentation by remember { mutableStateOf(true) }

    // Disputed / partially recognized states
    val partiallyRecognizedIds = remember { setOf("XK", "TW", "PS", "EH") }
    // Dependent / self-governing territories
    val selfGoverningIds = remember { setOf("PR", "GL", "HK", "MO", "GF", "GP", "MQ", "RE", "YT") }

    // Filtered countries based on inclusion rules, search, and filters
    val filteredCountries = remember(
        countries, searchQuery, selectedContinentFilter, showOnlyDiscovered, 
        showOnlyFavorites, includePartiallyRecognized, includeSelfGoverning, discoveries
    ) {
        countries.filter { country ->
            // Apply editable inclusion policies
            if (!includePartiallyRecognized && partiallyRecognizedIds.contains(country.id)) return@filter false
            if (!includeSelfGoverning && selfGoverningIds.contains(country.id)) return@filter false

            // Continent Filter
            if (selectedContinentFilter != null && !country.continentEn.equals(selectedContinentFilter, ignoreCase = true)) {
                return@filter false
            }

            // Discovery and Favorite state
            val status = discoveries.find { it.countryId == country.id }
            val isDiscovered = status?.isDiscovered == true
            val isFavorite = status?.isFavorite == true

            if (showOnlyDiscovered && !isDiscovered) return@filter false
            if (showOnlyFavorites && !isFavorite) return@filter false

            // Localized Search across English, Arabic, German, French, capitals, and ISO ids
            if (searchQuery.isNotEmpty()) {
                val queryNorm = searchQuery.trim().lowercase()
                
                // Normalizing Arabic letters for better Arabic search accuracy
                val nameArNormalized = country.nameAr.lowercase()
                    .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
                    .replace("ة", "ه").replace("ى", "ي")
                val queryArNormalized = queryNorm
                    .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
                    .replace("ة", "ه").replace("ى", "ي")
                    // Strip common "ال" prefix
                    .let { if (it.startsWith("ال")) it.substring(2) else it }

                val matchesSearch = country.nameEn.lowercase().contains(queryNorm) ||
                        country.nameDe.lowercase().contains(queryNorm) ||
                        country.nameFr.lowercase().contains(queryNorm) ||
                        nameArNormalized.contains(queryArNormalized) ||
                        country.id.lowercase().contains(queryNorm) ||
                        country.capitalEn.lowercase().contains(queryNorm) ||
                        country.capitalAr.contains(searchQuery)

                if (!matchesSearch) return@filter false
            }

            true
        }
    }

    // Progress math
    val totalCount = countries.size
    val discoveredCount = countries.count { c -> discoveries.any { it.countryId == c.id && it.isDiscovered } }
    val progressPercent = if (totalCount > 0) (discoveredCount.toFloat() / totalCount.toFloat()) else 0f

    // Continent progress mapper
    val continentStats = remember(countries, discoveries) {
        val continentsList = listOf("North America", "South America", "Europe", "Africa", "Asia", "Oceania")
        continentsList.map { continent ->
            val totalInCont = countries.count { it.continentEn.equals(continent, ignoreCase = true) }
            val discInCont = countries.count { c -> 
                c.continentEn.equals(continent, ignoreCase = true) && 
                discoveries.any { it.countryId == c.id && it.isDiscovered } 
            }
            ContinentProgress(continent, discInCont, totalInCont)
        }
    }

    // Recently discovered countries (sorted by timestamp)
    val recentlyDiscovered = remember(countries, discoveries) {
        discoveries
            .filter { it.isDiscovered && it.discoveredAt > 0L }
            .sortedByDescending { it.discoveredAt }
            .mapNotNull { disc -> countries.find { it.id == disc.countryId } }
            .take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedCountry?.getLocalizedName(lang) ?: Locales.get("atlas", lang),
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedCountry != null) {
                                selectedCountry = null
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("atlas_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (selectedCountry == null) {
                        IconButton(onClick = { showPolicyDialog = true }) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Territory Policy")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (navController != null && selectedCountry == null) {
                FlagQuestBottomNavigation(
                    navController = navController,
                    lang = lang
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentSelected = selectedCountry
            if (currentSelected != null) {
                val isDiscovered = discoveries.any { it.countryId == currentSelected.id && it.isDiscovered }
                val isFavorite = discoveries.any { it.countryId == currentSelected.id && it.isFavorite }
                
                CountryDetailView(
                    lang = lang,
                    country = currentSelected,
                    isDiscovered = isDiscovered,
                    isFavorite = isFavorite,
                    onToggleFavorite = { 
                        onToggleFavorite(currentSelected.id)
                    },
                    onRelatedQuizClick = {
                        val category = when (currentSelected.continentEn) {
                            "Europe" -> "FLAGS"
                            "Asia" -> "FLAGS"
                            "Africa" -> "FLAGS"
                            else -> "FLAGS"
                        }
                        onRelatedQuizSelected(category)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Overall Progress Header Card
                    item {
                        ProgressHeaderCard(
                            lang = lang,
                            discoveredCount = discoveredCount,
                            totalCount = totalCount,
                            progressPercent = progressPercent,
                            continentStats = continentStats,
                            allAchievements = allAchievements
                        )
                    }

                    // 2. Interactive Map / Grid Switcher & Canvas representation
                    item {
                        MapAndGridSection(
                            lang = lang,
                            useMap = useMapRepresentation,
                            onToggleRepresentation = { useMapRepresentation = it },
                            selectedContinent = selectedContinentFilter,
                            onSelectContinent = { selectedContinentFilter = it },
                            continentStats = continentStats
                        )
                    }

                    // 3. Recently Discovered Section
                    if (recentlyDiscovered.isNotEmpty()) {
                        item {
                            RecentlyDiscoveredRow(
                                lang = lang,
                                list = recentlyDiscovered,
                                onCountryClick = { selectedCountry = it }
                            )
                        }
                    }

                    // 4. Search and Multi-Filters Card
                    item {
                        SearchFiltersCard(
                            lang = lang,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedContinent = selectedContinentFilter,
                            onClearContinent = { selectedContinentFilter = null },
                            showOnlyDiscovered = showOnlyDiscovered,
                            onDiscoveredToggle = { showOnlyDiscovered = it },
                            showOnlyFavorites = showOnlyFavorites,
                            onFavoritesToggle = { showOnlyFavorites = it }
                        )
                    }

                    // 5. Country Cards list
                    if (filteredCountries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Locales.get("empty_atlas", lang),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredCountries) { country ->
                            val status = discoveries.find { it.countryId == country.id }
                            val isDiscovered = status?.isDiscovered == true
                            val isFavorite = status?.isFavorite == true

                            AtlasCountryItem(
                                lang = lang,
                                country = country,
                                isDiscovered = isDiscovered,
                                isFavorite = isFavorite,
                                onFavoriteToggle = { onToggleFavorite(country.id) },
                                onClick = {
                                    // Clicking discovered OR locked country opens detail view (with locked state applied inside)
                                    selectedCountry = country
                                }
                            )
                        }
                    }
                }
            }

            // Territory Policy Customization Dialog
            if (showPolicyDialog) {
                TerritoryPolicyDialog(
                    lang = lang,
                    includePartiallyRecognized = includePartiallyRecognized,
                    onTogglePartiallyRecognized = { includePartiallyRecognized = it },
                    includeSelfGoverning = includeSelfGoverning,
                    onToggleSelfGoverning = { includeSelfGoverning = it },
                    onDismiss = { showPolicyDialog = false }
                )
            }
        }
    }
}

// Data holder for continent statistics
data class ContinentProgress(
    val nameEn: String,
    val discovered: Int,
    val total: Int
)

@Composable
fun ProgressHeaderCard(
    lang: String,
    discoveredCount: Int,
    totalCount: Int,
    progressPercent: Float,
    continentStats: List<ContinentProgress>,
    allAchievements: List<AchievementEntity>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Atlas icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = Locales.get("progress_title", lang),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${Locales.get("discovered", lang)}: $discoveredCount / $totalCount",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progressPercent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Exploration accomplishments directly in-view
            val relevantAchievements = allAchievements.filter { 
                it.id.startsWith("explorer_") || it.id.startsWith("atlas_")
            }
            if (relevantAchievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Locales.get("achievements", lang).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                relevantAchievements.take(2).forEach { ach ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Locales.get(ach.titleKey, lang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${ach.progress}/${ach.maxProgress} " + if (ach.isUnlocked) "🏆" else "🔒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MapAndGridSection(
    lang: String,
    useMap: Boolean,
    onToggleRepresentation: (Boolean) -> Unit,
    selectedContinent: String?,
    onSelectContinent: (String?) -> Unit,
    continentStats: List<ContinentProgress>
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            Text(
                text = Locales.get("world_map_view", lang, context),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row {
                IconButton(onClick = { onToggleRepresentation(true) }) {
                    Icon(
                        imageVector = Icons.Default.Map, 
                        contentDescription = "Map view",
                        tint = if (useMap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                IconButton(onClick = { onToggleRepresentation(false) }) {
                    Icon(
                        imageVector = Icons.Default.GridOn, 
                        contentDescription = "Grid view",
                        tint = if (!useMap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (useMap) {
            // Stylized World Map representation drawn completely on native Canvas
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // FORCE Ltr layout for the visual Map so it is geographically identical in all languages (no RTL mirroring)
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Ltr) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val density = LocalDensity.current
                        val w = with(density) { maxWidth.toPx() }
                        val h = with(density) { maxHeight.toPx() }
                        
                        // Define interactive geographic centers of the 6 habitable continents
                        val continents = remember(w, h, continentStats) {
                            listOf(
                                MapContinent("North America", "North America", 0.22f * w, 0.35f * h, 38.dp, Color(0xFF42A5F5), continentStats.find { it.nameEn == "North America" }),
                                MapContinent("South America", "South America", 0.32f * w, 0.72f * h, 32.dp, Color(0xFF66BB6A), continentStats.find { it.nameEn == "South America" }),
                                MapContinent("Europe", "Europe", 0.51f * w, 0.31f * h, 30.dp, Color(0xFFFFCA28), continentStats.find { it.nameEn == "Europe" }),
                                MapContinent("Africa", "Africa", 0.53f * w, 0.60f * h, 36.dp, Color(0xFFEF5350), continentStats.find { it.nameEn == "Africa" }),
                                MapContinent("Asia", "Asia", 0.76f * w, 0.37f * h, 42.dp, Color(0xFFAB47BC), continentStats.find { it.nameEn == "Asia" }),
                                MapContinent("Oceania", "Oceania", 0.85f * w, 0.74f * h, 28.dp, Color(0xFF26A69A), continentStats.find { it.nameEn == "Oceania" })
                            )
                        }

                        val accessibilityLabel = "Interactive offline world map. Continents: North America, South America, Europe, Africa, Asia, Oceania. Tap any region to filter."

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics { contentDescription = accessibilityLabel }
                                .pointerInput(continents, selectedContinent) {
                                    detectTapGestures { offset ->
                                        // Detect if user tapped inside any continent bubble
                                        val clicked = continents.find { mc ->
                                            val dx = offset.x - mc.x
                                            val dy = offset.y - mc.y
                                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                            dist <= with(density) { mc.radius.toPx() }
                                        }
                                        if (clicked != null) {
                                            if (selectedContinent == clicked.id) {
                                                onSelectContinent(null) // clear filter
                                            } else {
                                                onSelectContinent(clicked.id)
                                            }
                                        } else {
                                            onSelectContinent(null) // tap ocean clears filter
                                        }
                                    }
                                }
                        ) {
                            // 1. Draw subtle grid dashed lines (longitude / latitude lines)
                            val gridPaint = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            
                            // Latitudes
                            for (row in 1..3) {
                                val latY = (row * 0.25f) * h
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(0f, latY),
                                    end = Offset(w, latY),
                                    strokeWidth = 2f,
                                    pathEffect = gridPaint
                                )
                            }
                            // Longitudes
                            for (col in 1..5) {
                                val lonX = (col * 0.16f) * w
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(lonX, 0f),
                                    end = Offset(lonX, h),
                                    strokeWidth = 2f,
                                    pathEffect = gridPaint
                                )
                            }

                            // 2. Draw trade routes dotted connections between continents
                            val pathPaint = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            // NA -> SA
                            drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0.22f*w, 0.35f*h), Offset(0.32f*w, 0.72f*h), strokeWidth = 3f, pathEffect = pathPaint)
                            // NA -> Europe
                            drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0.22f*w, 0.35f*h), Offset(0.51f*w, 0.31f*h), strokeWidth = 3f, pathEffect = pathPaint)
                            // Europe -> Africa
                            drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0.51f*w, 0.31f*h), Offset(0.53f*w, 0.60f*h), strokeWidth = 3f, pathEffect = pathPaint)
                            // Europe -> Asia
                            drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0.51f*w, 0.31f*h), Offset(0.76f*w, 0.37f*h), strokeWidth = 3f, pathEffect = pathPaint)
                            // Africa -> Asia
                            drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0.53f*w, 0.60f*h), Offset(0.76f*w, 0.37f*h), strokeWidth = 3f, pathEffect = pathPaint)
                            // Asia -> Oceania
                            drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0.76f*w, 0.37f*h), Offset(0.85f*w, 0.74f*h), strokeWidth = 3f, pathEffect = pathPaint)

                            // 3. Draw continent nodes
                            continents.forEach { mc ->
                                val rPx = mc.radius.toPx()
                                val isCurrentFilter = selectedContinent == mc.id
                                
                                // Draw continent filled backing
                                drawCircle(
                                    color = mc.color.copy(alpha = if (isCurrentFilter) 0.6f else 0.25f),
                                    radius = rPx,
                                    center = Offset(mc.x, mc.y)
                                )

                                // Thick outline (high contrast compliant)
                                drawCircle(
                                    color = if (isCurrentFilter) mc.color else mc.color.copy(alpha = 0.6f),
                                    radius = rPx,
                                    center = Offset(mc.x, mc.y),
                                    style = Stroke(width = if (isCurrentFilter) 8f else 3f)
                                )

                                // Radiant select outer outline
                                if (isCurrentFilter) {
                                    drawCircle(
                                        color = mc.color.copy(alpha = 0.3f),
                                        radius = rPx + 16f,
                                        center = Offset(mc.x, mc.y),
                                        style = Stroke(width = 4f)
                                    )
                                }
                            }
                        }

                        // Overlay labels and progress texts on canvas safely using Compose Views
                        continents.forEach { mc ->
                            val isSelected = selectedContinent == mc.id
                            Box(
                                modifier = Modifier
                                    .absoluteOffset(
                                        x = with(density) { (mc.x - mc.radius.toPx()).toDp() },
                                        y = with(density) { (mc.y - mc.radius.toPx() * 0.4f).toDp() }
                                    )
                                    .width(with(density) { (mc.radius.toPx() * 2f).toDp() }),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = when (mc.id) {
                                            "North America" -> when (lang) {
                                                "ar" -> "أمريكا ش."
                                                "de" -> "N. Amerika"
                                                "fr" -> "Amér. du N."
                                                else -> "N. America"
                                            }
                                            "South America" -> when (lang) {
                                                "ar" -> "أمريكا ج."
                                                "de" -> "S. Amerika"
                                                "fr" -> "Amér. du S."
                                                else -> "S. America"
                                            }
                                            else -> Locales.get(mc.id.lowercase().replace(" ", "_"), lang, LocalContext.current)
                                        },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${mc.stats?.discovered ?: 0}/${mc.stats?.total ?: 0}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Elegant Grid Fallback
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                continentStats.forEach { stat ->
                    val isSelected = selectedContinent == stat.nameEn
                    Card(
                        modifier = Modifier
                            .width(135.dp)
                            .clickable {
                                if (isSelected) onSelectContinent(null) else onSelectContinent(stat.nameEn)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (stat.nameEn) {
                                    "Europe" -> "🇪🇺"
                                    "Asia" -> "🌏"
                                    "Africa" -> "🌍"
                                    "North America" -> "🌎"
                                    "South America" -> "🌎"
                                    else -> "🇦🇺"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "ar") {
                                    val key = stat.nameEn.lowercase().replace(" ", "_")
                                    Locales.get(key, "ar")
                                } else stat.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${stat.discovered} / ${stat.total}",
                                fontSize = 11.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MapContinent(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
    val radius: androidx.compose.ui.unit.Dp,
    val color: Color,
    val stats: ContinentProgress?
)

@Composable
fun RecentlyDiscoveredRow(
    lang: String,
    list: List<Country>,
    onCountryClick: (Country) -> Unit
) {
    Column(modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp)) {
        Text(
            text = Locales.get("recent_discovered", lang).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            list.forEach { country ->
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .clickable { onCountryClick(country) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = country.flagEmoji,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = country.getLocalizedName(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFiltersCard(
    lang: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedContinent: String?,
    onClearContinent: () -> Unit,
    showOnlyDiscovered: Boolean,
    onDiscoveredToggle: (Boolean) -> Unit,
    showOnlyFavorites: Boolean,
    onFavoritesToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("atlas_search_input"),
                placeholder = { Text(Locales.get("search_placeholder", lang)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            if (selectedContinent != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Continent: $selectedContinent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear filter",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onClearContinent() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checkbox filter rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filter: Discovered Only
                FilterChip(
                    selected = showOnlyDiscovered,
                    onClick = { onDiscoveredToggle(!showOnlyDiscovered) },
                    label = { Text(Locales.get("filter_discovered", lang)) },
                    leadingIcon = if (showOnlyDiscovered) {
                        { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )

                // Filter: Favorites Only
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { onFavoritesToggle(!showOnlyFavorites) },
                    label = { Text(Locales.get("filter_favorites", lang)) },
                    leadingIcon = if (showOnlyFavorites) {
                        { Icon(imageVector = Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else {
                        { Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AtlasCountryItem(
    lang: String,
    country: Country,
    isDiscovered: Boolean,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("country_item_${country.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovered) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Display
            if (isDiscovered) {
                Text(
                    text = country.flagEmoji,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("country_flag_${country.id}"),
                    textAlign = TextAlign.Center
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked country flag",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDiscovered) country.getLocalizedName(lang) else "???",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDiscovered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isDiscovered) country.getLocalizedContinent(lang) else Locales.get("undiscovered", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Favorite Button (always clickable if discovered)
            if (isDiscovered) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite toggle",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailView(
    lang: String,
    country: Country,
    isDiscovered: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onRelatedQuizClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isDiscovered) {
                        Text(
                            text = country.flagEmoji,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked flag",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = if (isDiscovered) country.getLocalizedName(lang) else "???",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    if (isDiscovered) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ISO ID: ${country.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isDiscovered) {
                    // Toggle Favorite Button
                    Button(
                        onClick = onToggleFavorite,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle favorite state",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Locales.get("filter_favorites", lang),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Play Related Quiz Button
                Button(
                    onClick = onRelatedQuizClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Locales.get("related_quiz", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isDiscovered) {
            // Detailed Facts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow(label = Locales.get("continent", lang), value = country.getLocalizedContinent(lang))
                        DetailRow(label = Locales.get("subregion", lang), value = country.getLocalizedSubregion(lang))
                        DetailRow(label = Locales.get("capitals", lang), value = country.getLocalizedCapital(lang))
                        DetailRow(label = Locales.get("currencies", lang), value = country.getLocalizedCurrency(lang))
                        
                        val numFormat = NumberFormat.getNumberInstance(Locale.US)
                        DetailRow(label = Locales.get("area", lang), value = "${numFormat.format(country.areaSqKm)} km²")
                        DetailRow(
                            label = Locales.get("population", lang), 
                            value = "${numFormat.format(country.population)} (${country.yearOfData})"
                        )
                        
                        DetailRow(label = Locales.get("languages", lang), value = country.getLocalizedLanguages(lang))
                        DetailRow(label = Locales.get("neighbors", lang), value = country.getLocalizedNeighbors(lang))
                        DetailRow(label = Locales.get("organizations", lang), value = country.getLocalizedOrganizations(lang))
                    }
                }
            }

            // Fun Fact / Short Facts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Locales.get("fun_fact", lang, LocalContext.current),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = country.getLocalizedFunFact(lang),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${Locales.get("verified", lang, LocalContext.current)}: ${country.dataSource}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = country.lastVerified,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // locked State Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked discovery details",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Locales.get("discovery_hint", lang, LocalContext.current),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = Locales.get("discovery_hint_desc", lang, LocalContext.current),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TerritoryPolicyDialog(
    lang: String,
    includePartiallyRecognized: Boolean,
    onTogglePartiallyRecognized: (Boolean) -> Unit,
    includeSelfGoverning: Boolean,
    onToggleSelfGoverning: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = Locales.get("territory_policy_title", lang, LocalContext.current),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = Locales.get("territory_policy_desc", lang, LocalContext.current),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Text(
                    text = "EDIT INCLUSION GUIDELINES:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // Editable Partially Recognized Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Partially Recognized States",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "e.g., Kosovo, Taiwan, Palestine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includePartiallyRecognized,
                        onCheckedChange = onTogglePartiallyRecognized
                    )
                }

                // Editable Self-Governing Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Self-Governing Territories",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "e.g., Greenland, Puerto Rico",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeSelfGoverning,
                        onCheckedChange = onToggleSelfGoverning
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Apply Guidelines")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}
