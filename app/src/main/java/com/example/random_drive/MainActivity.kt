package com.example.random_drive

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.random_drive.ui.Destination
import com.example.random_drive.ui.DriveViewModel
import com.google.android.gms.location.LocationServices

val BgColor = Color(0xFFF8F9FA)
val CardWhite = Color(0xFFFFFFFF)
val PrimaryPurple = Color(0xFF6B4EFF)
val PrimaryPurpleLight = Color(0xFFEEECFF)
val TextMain = Color(0xFF1E1E1E)
val TextSub = Color(0xFF8E8E93)
val DividerColor = Color(0xFFE5E5EA)
val NaverGreen = Color(0xFF03C75A)
val TmapBlue = Color(0xFF003F63)

class MainActivity : ComponentActivity() {

    private lateinit var soundPool: SoundPool
    private var carStartSoundId: Int = 0
    private var btnClickSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        carStartSoundId = soundPool.load(this, R.raw.car_start, 1)
        btnClickSoundId = soundPool.load(this, R.raw.btn_click, 1)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = BgColor,
                    surface = CardWhite,
                    primary = PrimaryPurple,
                    onPrimary = Color.White
                )
            ) {
                DriveAppScreen(
                    onPlayClick = { playSound(btnClickSoundId) },
                    onPlayCarStart = { playSound(carStartSoundId) }
                )
            }
        }
    }

    private fun playSound(soundId: Int) {
        if (soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}

@Composable
fun DriveAppScreen(
    viewModel: DriveViewModel = viewModel(),
    onPlayClick: () -> Unit,
    onPlayCarStart: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val destination by viewModel.selectedDestination.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc -> currentLocation = loc }
            } catch (_: SecurityException) { }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc -> currentLocation = loc }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f)) togetherWith
                        (fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f))
            },
            label = "step_nav"
        ) { targetStep ->
            when (targetStep) {
                1 -> SingleInputScreen(
                    onRecommendClick = { maxDist, type ->
                        val lat = currentLocation?.latitude ?: 37.5665
                        val lon = currentLocation?.longitude ?: 126.9780
                        viewModel.findRandomDestination(lat, lon, 0.0, maxDist.toDouble(), type)
                        step = 2
                    },
                    onPlayClick = onPlayClick
                )
                2 -> ResultScreen(
                    destination = destination,
                    onRetry = { step = 1 },
                    onNavigationClick = onPlayCarStart,
                    onPlayClick = onPlayClick
                )
            }
        }
    }
}

@Composable
fun SingleInputScreen(
    onRecommendClick: (Float, String) -> Unit,
    onPlayClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var maxDistance by remember { mutableFloatStateOf(30f) }
    var selectedType by remember { mutableStateOf("도심") }
    var isRandomMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0E7FF), BgColor)
                    )
                )
                .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Column {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextMain, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(24.dp))

                // 💡 글자 색상을 디자인과 똑같이 맞추고, 바로 옆에 자동차 애니메이션을 배치했습니다!
                Text(
                    text = "어디로 드라이브",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "떠나볼까요?",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryPurple // 원본 디자인처럼 보라색 강조
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedInlineCar() // 글씨 바로 옆에서 달리는 자동차
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "원하는 조건을 선택하고 추천받아보세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSub
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // 1. 거리 제한 카드
            SettingCard(
                icon = Icons.Default.LocationOn,
                title = "거리 제한",
                subtitle = "이동 가능한 최대 거리를 설정하세요",
                trailingText = "${maxDistance.toInt()} km"
            ) {
                Slider(
                    value = maxDistance,
                    onValueChange = {
                        maxDistance = it
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryPurple,
                        activeTrackColor = PrimaryPurple,
                        inactiveTrackColor = DividerColor
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val distances = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
                    distances.forEach { dist ->
                        Text(
                            text = if (dist == 10 || dist == 100) "${dist}km" else "$dist",
                            color = TextSub,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 분위기 선택 카드
            SettingCard(
                icon = Icons.Default.Face,
                title = "분위기 선택",
                subtitle = "원하는 드라이브 분위기를 선택하세요",
                trailingText = ""
            ) {
                val themes = listOf(
                    Triple("도심", "🏙️", "도심"),
                    Triple("바다", "🌊", "바다"),
                    Triple("산", "⛰️", "산"),
                    Triple("강", "🏞️", "강")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    themes.forEach { (uiLabel, emoji, dbValue) ->
                        val isSelected = selectedType == dbValue
                        ThemeTile(
                            label = uiLabel,
                            emoji = emoji,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f)
                        ) {
                            onPlayClick()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedType = dbValue
                        }
                        if (uiLabel != "강") Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 랜덤 추천 스위치
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(PrimaryPurpleLight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎲", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("랜덤 추천", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                        Text("조건 없이 랜덤으로 추천받고 싶다면?", color = TextSub, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isRandomMode,
                        onCheckedChange = {
                            onPlayClick()
                            isRandomMode = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight)
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring())

        Button(
            onClick = {
                onPlayClick()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                val finalType = if (isRandomMode) listOf("도심", "바다", "산", "강").random() else selectedType
                val finalDist = if (isRandomMode) 100f else maxDistance

                onRecommendClick(finalDist, finalType)
            },
            interactionSource = interactionSource,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .height(64.dp)
                .scale(pressScale)
                .shadow(if (isPressed) 4.dp else 16.dp, RoundedCornerShape(32.dp), spotColor = PrimaryPurple),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple, contentColor = Color.White)
        ) {
            Text("✨", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("추천받기", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

// 💡 텍스트 바로 옆에서 통통 튀며 앞으로 주행하는 자동차 애니메이션 컴포저블
@Composable
fun AnimatedInlineCar() {
    val infiniteTransition = rememberInfiniteTransition(label = "inline_car")

    // 1. 앞으로 슝 달려나가는 X 좌표 애니메이션
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 120f, // 텍스트 옆에서 시작해서 우측으로 120dp 이동
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xOffset"
    )

    // 2. 멀어지면서 스르륵 투명해지는 애니메이션
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    // 3. 엔진이 걸린 것처럼 위아래로 통통 튀는 애니메이션
    val yBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yBounce"
    )

    Text(
        text = "🚗💨",
        fontSize = 32.sp,
        modifier = Modifier
            .offset(x = xOffset.dp, y = yBounce.dp)
            .alpha(alpha)
    )
}

@Composable
fun SettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailingText: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000)),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(BgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryPurple)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
                    Text(subtitle, color = TextSub, fontSize = 12.sp)
                }
                if (trailingText.isNotEmpty()) {
                    Text(trailingText, fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun ThemeTile(
    label: String,
    emoji: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(if (isSelected) PrimaryPurpleLight else CardWhite, label = "bg")
    val borderColor by animateColorAsState(if (isSelected) PrimaryPurple else DividerColor, label = "border")
    val contentColor by animateColorAsState(if (isSelected) PrimaryPurple else TextSub, label = "content")

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = contentColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun ResultScreen(destination: Destination?, onRetry: () -> Unit, onNavigationClick: () -> Unit, onPlayClick: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val destName = destination?.name ?: "탐색 중..."
    val pureName = destName.split("\n")[0]

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("이번 드라이브는 여기 어때요?", style = MaterialTheme.typography.titleLarge, color = TextSub)
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().shadow(24.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp)).background(CardWhite).padding(40.dp), contentAlignment = Alignment.Center) {
            Text(text = destName, style = MaterialTheme.typography.titleLarge, color = TextMain, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 32.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (destination != null && destination.lat != 0.0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        onNavigationClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val url = "tmap://route?goalname=${pureName}&goalx=${destination.lon}&goaly=${destination.lat}"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=com.skt.tmap.ku".toUri()))
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TmapBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("T map 안내", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onNavigationClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val url = "nmap://navigation?dlat=${destination.lat}&dlng=${destination.lon}&dname=${pureName}&appname=com.example.random_drive"
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=com.nhn.android.nmap".toUri()))
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NaverGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("네이버 안내", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        TextButton(onClick = {
            onPlayClick()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onRetry()
        }) {
            Text("마음에 안 드시나요? 다시 뽑기", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}