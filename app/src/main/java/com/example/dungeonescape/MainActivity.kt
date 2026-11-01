package com.example.dungeonescape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DungeonGame()
        }
    }
}

@Composable
// DungeonGameという画面を表示する関数
fun DungeonGame() {

    // 0 = 床
    // 1 = 壁
    // 2 = 鍵
    // 3 = ドア
    // 4 = ポーション
    // 5 = コイン

    var map by remember{
        mutableStateOf(
            arrayOf(
                intArrayOf(1,1,1,1,1,1,1,1,1),
                intArrayOf(1,0,0,0,1,0,0,2,1),
                intArrayOf(1,0,1,0,1,0,1,0,1),
                intArrayOf(1,0,1,0,0,0,1,0,1),
                intArrayOf(1,0,1,1,1,0,1,0,1),
                intArrayOf(1,0,5,0,0,0,1,0,1),
                intArrayOf(1,1,1,1,1,1,1,0,1),
                intArrayOf(1,4,0,0,0,0,0,3,1),
                intArrayOf(1,1,1,1,1,1,1,1,1)
            )
        )
    }

    // プレイヤーのステータス（初期位置 x=1, y=1）
    var playerX by remember { mutableIntStateOf(1) }
    var playerY by remember { mutableIntStateOf(1) }
    var hp by remember { mutableIntStateOf(3) }     // 現在のHP
    var maxHp by remember { mutableIntStateOf(3) }  // 最大HP
    var level by remember { mutableIntStateOf(1) }  // 現在のレベル
    var exp by remember { mutableIntStateOf(0) }    // 現在の経験値
    var score by remember { mutableIntStateOf(0) }  // 現在の得点
    var hasKey by remember { mutableStateOf(false) }    // 鍵の取得判定
    var floorCount by remember { mutableIntStateOf(1) } //階層カウント

    // 敵のステータス（初期位置 x=7, y=5）
    var enemyX by remember { mutableIntStateOf(7) }
    var enemyY by remember { mutableIntStateOf(5) }
    var enemyHp by remember { mutableIntStateOf(2) }

    // ゲーム画面
    Column(
        modifier = Modifier
            .fillMaxSize()      // 全画面表示
            .background(Color.Black)    // 背景を黒に設定
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 画面上部に表示するステータスバー
        // 横に並べる
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, vertical = 30.dp)   // マップとステータスバーの空間
        ) {
            Text(
                text = "❤ ❤ ❤  Lv.$level  Floor: $floorCount  Score: $score",
                color = Color.White
            )
        }

        // マップ画面
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (y in map.indices) {
                Row {
                    for (x in map[y].indices) {
                        val tile = map[y][x]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (tile == 1) Color.DarkGray
                                    else Color.LightGray
                                )
                                .border(0.5.dp, Color.Gray),    // マス目沿って薄い線を描く
                            contentAlignment = Alignment.Center
                        ) {
                            when (tile) {
                                2 -> Text("🔑", fontSize = 20.sp)
                                3 -> Text("🚪", fontSize = 20.sp)
                                4 -> Text("🧪", fontSize = 20.sp)
                                5 -> Text("🪙", fontSize = 20.sp)
                            }

                            if (x == playerX && y == playerY) {
                                Text("🔵", fontSize = 20.sp)
                            }

                            if (x == enemyX && y == enemyY && enemyHp > 0) {
                                Text("👾", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}