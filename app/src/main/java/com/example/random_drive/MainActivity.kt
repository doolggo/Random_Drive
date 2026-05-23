package com.example.random_drive

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.random_drive.ui.Destination
import com.example.random_drive.ui.DriveViewModel
import com.google.android.gms.location.LocationServices

val LightBg = Color(0xFFF0F9FF)
val CardWhite = Color(0xFFFFFFFF)
val PrimaryOrange = Color(0xFFFF7043)
val AccentMint = Color(0xFF66BB6A)
val SoftPurple = Color(0xFFBC84EE)
val DeepCharcoal = Color(0xFF1E293B)
val NaverGreen = Color(0xFF03C75A) // 네이버 고유 색상
val TmapBlue = Color(0xFF003F63)  // 티맵 고유 색상

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = LightBg,
                    surface = CardWhite,
                    primary = PrimaryOrange,
                    secondary = AccentMint
                )
            ) {
                DriveAppScreen()
            }
        }
    }
}

@Composable
fun DriveAppScreen(viewModel: DriveViewModel = viewModel()) {
    var step by remember { mutableIntStateOf(1) }
    val destination by viewModel.selectedDestination.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc -> currentLocation = loc }
            } catch (e: SecurityException) {
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc -> currentLocation = loc }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFFE0F2FE), LightBg)))) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            when (step) {
                1 -> QuestionnaireScreen(
                    onSelectionComplete = { distRange, type ->
                        val lat = currentLocation?.latitude ?: 37.5665
                        val lon = currentLocation?.longitude ?: 126.9780
                        viewModel.findRandomDestination(lat, lon, distRange.first, distRange.second, type)
                        step = 2
                    }
                )
                2 -> ResultScreen(
                    destination = destination,
                    onRetry = { step = 1 }
                )
            }
        }
    }
}

// QuestionnaireScreen, DistanceQuestionView, TypeQuestionView, TravelTile 등은 기존과 완벽히 동일하므로 생략 없이 유지
@Composable
fun QuestionnaireScreen(onSelectionComplete: (Pair<Double, Double>, String) -> Unit) {
    var currentQuestion by remember { mutableIntStateOf(1) }
    var distanceRange by remember { mutableStateOf(Pair(0.0, 0.0)) }
    var type by remember { mutableStateOf("") }

    AnimatedContent(
        targetState = currentQuestion,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInHorizontally(animationSpec = tween(500)) { it } + fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith
                        (slideOutHorizontally(animationSpec = tween(500)) { -it } + fadeOut())
            } else {
                (slideInHorizontally(animationSpec = tween(500)) { -it } + fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith
                        (slideOutHorizontally(animationSpec = tween(500)) { it } + fadeOut())
            }
        },
        label = "travel_nav"
    ) { targetQuestion ->
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (targetQuestion == 2) {
                    IconButton(onClick = { currentQuestion = 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DeepCharcoal)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.height(8.dp).weight(1f).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2E8F0))) {
                    Box(modifier = Modifier.fillMaxWidth(if (targetQuestion == 1) 0.5f else 1f).fillMaxHeight().background(if (targetQuestion == 1) SoftPurple else AccentMint))
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            when (targetQuestion) {
                1 -> DistanceQuestionView(selectedRange = distanceRange, onSelect = { distanceRange = it; currentQuestion = 2 })
                2 -> TypeQuestionView(selectedType = type, onSelect = { type = it }, onSubmit = { onSelectionComplete(distanceRange, type) })
            }
        }
    }
}

@Composable
fun DistanceQuestionView(selectedRange: Pair<Double, Double>, onSelect: (Pair<Double, Double>) -> Unit) {
    Column {
        Text("어디까지\n가볼까요?", style = MaterialTheme.typography.headlineLarge, color = DeepCharcoal, fontWeight = FontWeight.ExtraBold, lineHeight = 42.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("여행의 반경을 정해주세요.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(32.dp))
        val options = listOf("5km - 10km" to Pair(5.0, 10.0), "10km - 20km" to Pair(10.0, 20.0), "20km - 30km" to Pair(20.0, 30.0), "30km - 40km" to Pair(30.0, 40.0), "40km - 50km" to Pair(40.0, 50.0), "50km 초과" to Pair(50.0, 9999.0))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(options) { (label, range) ->
                TravelTile(label = label, isSelected = selectedRange == range, activeColor = SoftPurple, onClick = { onSelect(range) })
            }
        }
    }
}

@Composable
fun TypeQuestionView(selectedType: String, onSelect: (String) -> Unit, onSubmit: () -> Unit) {
    Column {
        Text("어떤 풍경을\n좋아하세요?", style = MaterialTheme.typography.headlineLarge, color = DeepCharcoal, fontWeight = FontWeight.ExtraBold, lineHeight = 42.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("가슴이 뻥 뚫리는 테마를 골라보세요.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(32.dp))
        val options = listOf("바다", "산", "강", "도심")
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(options) { option ->
                TravelTile(label = option, isSelected = selectedType == option, activeColor = PrimaryOrange, onClick = { onSelect(option) })
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(64.dp).shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            enabled = selectedType.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange, contentColor = Color.White)
        ) {
            Text("목적지 랜덤 추천 받기 🚀", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun TravelTile(label: String, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    val elevation by animateDpAsState(if (isSelected) 8.dp else 2.dp, label = "elev")
    val bgColor by animateColorAsState(if (isSelected) activeColor else CardWhite, label = "bg")
    val textColor by animateColorAsState(if (isSelected) Color.White else DeepCharcoal, label = "text")
    Box(
        modifier = Modifier.fillMaxWidth().height(110.dp).shadow(elevation, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(bgColor).clickable { onClick() }.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
            if (isSelected) {
                Spacer(modifier = Modifier.height(6.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// 💡 여기서부터 내비게이션 연결 로직이 크게 추가되었습니다!
@Composable
fun ResultScreen(destination: Destination?, onRetry: () -> Unit) {
    val context = LocalContext.current
    val destName = destination?.name ?: "탐색 중..."

    // (이름 안에 줄바꿈으로 정보가 섞여 있으므로 첫 번째 줄인 실제 이름만 자르기)
    val pureName = destName.split("\n")[0]

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("이번 드라이브는 여기 어때요?", style = MaterialTheme.typography.titleLarge, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(32.dp)).clip(RoundedCornerShape(32.dp)).background(CardWhite).padding(40.dp), contentAlignment = Alignment.Center) {
            Text(text = destName, style = MaterialTheme.typography.titleLarge, color = DeepCharcoal, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 32.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 🚗 내비게이션 버튼들 (T map, 네이버 지도)
        if (destination != null && destination.lat != 0.0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // 티맵 버튼
                Button(
                    onClick = {
                        val url = "tmap://route?goalname=${pureName}&goalx=${destination.lon}&goaly=${destination.lat}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            // 티맵이 없으면 스토어로 이동
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.skt.tmap.ku")))
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

                // 네이버 지도 버튼
                Button(
                    onClick = {
                        val url = "nmap://navigation?dlat=${destination.lat}&dlng=${destination.lon}&dname=${pureName}&appname=com.example.random_drive"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            // 네이버지도가 없으면 스토어로 이동
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")))
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
        TextButton(onClick = onRetry) {
            Text("마음에 안 드시나요? 다시 뽑기", color = SoftPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}