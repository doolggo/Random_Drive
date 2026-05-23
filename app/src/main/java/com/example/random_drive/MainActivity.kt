package com.example.random_drive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.random_drive.ui.DriveViewModel

// 여행 테마를 위한 상큼한 컬러 팔레트
val LightBg = Color(0xFFF0F9FF) // 연한 하늘색
val CardWhite = Color(0xFFFFFFFF)
val PrimaryOrange = Color(0xFFFF7043) // 선셋 오렌지
val AccentMint = Color(0xFF66BB6A) // 미트 그린
val SoftPurple = Color(0xFFBC84EE) // 연보라
val DeepCharcoal = Color(0xFF1E293B) // 짙은 회색 (가독성용)

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

    // 가상의 위치 (3단계에서 실제 GPS로 교체 예정)
    val currentLat = 37.5665
    val currentLon = 126.9780

    // 전체 배경에 미세한 그라데이션 적용
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F2FE), LightBg)
                )
            )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
            when (step) {
                1 -> QuestionnaireScreen(
                    onSelectionComplete = { distRange, type ->
                        viewModel.findRandomDestination(currentLat, currentLon, distRange.first, distRange.second, type)
                        step = 2
                    }
                )
                2 -> ResultScreen(
                    destinationName = destination?.name ?: "탐색 중...",
                    onRetry = { step = 1 }
                )
            }
        }
    }
}

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
            // 상단 디자인 (진행 바 형태)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (targetQuestion == 2) {
                    IconButton(onClick = { currentQuestion = 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DeepCharcoal)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))

                // 심플한 스텝 인디케이터
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (targetQuestion == 1) 0.5f else 1f)
                            .fillMaxHeight()
                            .background(if (targetQuestion == 1) SoftPurple else AccentMint)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            when (targetQuestion) {
                1 -> DistanceQuestionView(
                    selectedRange = distanceRange,
                    onSelect = { distanceRange = it; currentQuestion = 2 }
                )
                2 -> TypeQuestionView(
                    selectedType = type,
                    onSelect = { type = it },
                    onSubmit = { onSelectionComplete(distanceRange, type) }
                )
            }
        }
    }
}

@Composable
fun DistanceQuestionView(
    selectedRange: Pair<Double, Double>,
    onSelect: (Pair<Double, Double>) -> Unit
) {
    Column {
        Text("어디까지\n가볼까요?", style = MaterialTheme.typography.headlineLarge, color = DeepCharcoal, fontWeight = FontWeight.ExtraBold, lineHeight = 42.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("여행의 반경을 정해주세요.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(32.dp))

        val options = listOf(
            "5km - 10km" to Pair(5.0, 10.0),
            "10km - 20km" to Pair(10.0, 20.0),
            "20km - 30km" to Pair(20.0, 30.0),
            "30km - 40km" to Pair(30.0, 40.0),
            "40km - 50km" to Pair(40.0, 50.0),
            "50km 초과" to Pair(50.0, 9999.0)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(options) { (label, range) ->
                TravelTile(
                    label = label,
                    isSelected = selectedRange == range,
                    activeColor = SoftPurple,
                    onClick = { onSelect(range) }
                )
            }
        }
    }
}

@Composable
fun TypeQuestionView(
    selectedType: String,
    onSelect: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column {
        Text("어떤 풍경을\n좋아하세요?", style = MaterialTheme.typography.headlineLarge, color = DeepCharcoal, fontWeight = FontWeight.ExtraBold, lineHeight = 42.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("가슴이 뻥 뚫리는 테마를 골라보세요.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(32.dp))

        val options = listOf("바다", "산", "강", "도심")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(options) { option ->
                TravelTile(
                    label = option,
                    isSelected = selectedType == option,
                    activeColor = PrimaryOrange,
                    onClick = { onSelect(option) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
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
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(elevation, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp),
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

@Composable
fun ResultScreen(destinationName: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("이번 드라이브는 여기 어때요?", style = MaterialTheme.typography.titleLarge, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(24.dp))

        // 결과 카드 디자인
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(CardWhite)
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = destinationName,
                style = MaterialTheme.typography.displaySmall,
                color = DeepCharcoal,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        TextButton(onClick = onRetry) {
            Text("마음에 안 드시나요? 다시 뽑기", color = SoftPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}