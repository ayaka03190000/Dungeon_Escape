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

// 迷路生成
fun generateMaze(size: Int): Array<IntArray> {
    // 最初に全て壁にする
    val maze = Array(size) { IntArray(size) { 1 } }

    // スタート地点
    val startX = 1
    val startY = 1
    maze[startY][startX] = 0

    // DFSで使う方向
    val dx = intArrayOf(0, 0, -2, 2)
    val dy = intArrayOf(-2, 2, 0, 0)

    // 探索する場所を保存する
    val stack = mutableListOf<Pair<Int, Int>>()
    stack.add(Pair(startX, startY))

    while (stack.isNotEmpty()) {
        val current = stack.last()
        val x = current.first
        val y = current.second

        // 4方向をランダムな順番で調べる
        val directions = (0..3).shuffled()
        var moved = false

        for (d in directions) {
            val nx = x + dx[d]
            val ny = y + dy[d]

            // マップの範囲内か確認
            if (nx > 0 && nx < size - 1 && ny > 0 && ny < size -1) {
                // まだ壁なら通路にする
                if (maze[ny][nx] == 1) {
                    // 間の壁も壊す
                    maze[y + dy[d] / 2][x + dx[d]/ 2] = 0
                    maze[ny][nx] = 0

                    stack.add(Pair(nx, ny))
                    moved = true
                    break

                }
            }
        }
        // 進める方向がなければ戻る
        if (!moved) {
            stack.removeAt(stack.lastIndex)
        }
    }
    return maze
}

@Composable
// DungeonGameという画面を表示する関数
fun DungeonGame() {
    // 画面の状態
    var gameState by remember { mutableStateOf("title") }

    // 選択した難易度
    var difficulty by remember { mutableStateOf("NORMAL") }

    // 難易度ごとの階層数
    val maxFloor = when (difficulty) {
        "EASY" -> 1
        "NORMAL" -> 3
        "HARD" -> 5
        else -> 3
    }

    // 0 = 床
    // 1 = 壁
    // 2 = 鍵
    // 3 = ドア
    // 4 = ポーション
    // 5 = コイン

    var map by remember{
        mutableStateOf(generateMaze(9))
    }

    // プレイヤーのステータス（初期位置 x=1, y=1）
    var playerX by remember { mutableIntStateOf(1) }
    var playerY by remember { mutableIntStateOf(1) }
    var hp by remember { mutableIntStateOf(3) }
    var maxHp by remember { mutableIntStateOf(3) }
    var level by remember { mutableIntStateOf(1) }
    var exp by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var hasKey by remember { mutableStateOf(false) }
    var floorCount by remember { mutableIntStateOf(1) }

    // 敵のステータス（初期位置 x=7, y=5）
    var enemyX by remember { mutableIntStateOf(7) }
    var enemyY by remember { mutableIntStateOf(5) }
    var enemyHp by remember { mutableIntStateOf(2) }

    // 敵の移動・ターン処理
    fun moveEnemy() {
        if (enemyHp <= 0) return

        val dx = playerX - enemyX
        val dy = playerY - enemyY

        var nextX = enemyX
        var nextY = enemyY

        // プレイヤーとの距離が大きい方に１歩進む
        if (abs(dx) > abs(dy)) {
            nextX += if (dx > 0) 1 else -1
        } else {
            nextY += if (dy > 0) 1 else -1
        }

        // 壁判定
        if (map[nextY][nextX] == 1) {
            nextX = enemyX
            nextY = enemyY
            if (abs(dx) <= abs(dy) && dx != 0) {
                nextX += if (dx > 0) 1 else -1
            } else if (dy != 0) {
                nextY += if (dy > 0) 1 else -1
            }
        }

        // 敵の移動先にプレイヤーがいる場合　-> 攻撃
        if (nextX == playerX && nextY == playerY) {
            if (hp > 0) hp -= 1
        } else if (map[nextY][nextX] != 1) {
            enemyX = nextX
            enemyY = nextY
        }
    }

    //プレイヤー移動・ターン処理
    fun movePlayer(dx: Int, dy: Int) {
        if (hp <= 0) return     // ゲームオーバー時

        val newX = playerX + dx
        val newY = playerY + dy

        if (newY !in map.indices || newX !in map[newY].indices) return  // マップ外の時

        val targetTile = map[newY][newX]

        // 壁判定
        if (targetTile == 1) return

        // ドアに入る
        if (targetTile == 3) {
            if (hasKey) {
                floorCount += 1
                hasKey = false
            } else {
                return
            }
        }

        // 敵に攻撃
        if (newX == enemyX && newY == enemyY && enemyHp > 0) {
            enemyHp -= 1
            if (enemyHp <= 0) {
                score += 200
                exp += 50
                if (exp >= 100) {   // expが100溜まったらレベル+1
                    level += 1
                    exp -= 100
                    maxHp += 1
                    hp = maxHp  // レベルアップで最大HP+1・全回復
                }
            }
            moveEnemy() // 敵のターン
            return
        }

        // 移動・アイテム処理
        when (targetTile) {
            2 -> {              // 移動先のマスが鍵のとき
                hasKey = true           // 鍵を取得する
                map[newY][newX] = 0     // 鍵をマップ上から消す
                map = map.copyOf()
            }

            4 -> {
                // ポーション取得
                if (hp < maxHp) {   // 現在のhpがmaxHpより小さければhp１回復
                    hp += 1
                }
                map[newY][newX] = 0     // ポーションをマップ上から消す
                map = map.copyOf()
            }

            5 -> {
                // コインを取得
                score += 10         // scoreを10獲得
                map[newY][newX] = 0     // コインをマップ上から消す
                map = map.copyOf()
            }
        }

        // プレイヤーを移動
        playerX = newX
        playerY = newY

        // 敵のターン
        moveEnemy()
    }

    // タイトル画面
    if (gameState == "title") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // タイトル
            Text(
                text = "DUNGEON ESCAPE",
                color = Color.White,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "SELECT DIFFICULTY",
                color = Color.LightGray,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 難易度選択
            Button(
                onClick = { difficulty = "EASY" }
            ) {
                Text("EASY")
            }

            Button(
                onClick = { difficulty = "NORMAL" }
            ) {
                Text("NORMAL")
            }

            Button(
                onClick = { difficulty = "HARD" }
            ) {
                Text("HARD")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 選択中の難易度
            Text(
                text = "Selected: $difficulty",
                color = Color.Yellow
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ゲームスタートボタン
            Button(
                onClick = {
                    map = generateMaze(9)
                    playerX = 1
                    playerY = 1
                    gameState = "playing"
                }
            ) {
                Text("START GAME")
            }
        }
    } else {
        // 縦に並べる
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

            // ゲーム画面
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

            Spacer(modifier = Modifier.height(70.dp))   // マップとボタンの間隔
            // 操作ボタン
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 上ボタン
                Button(
                    onClick = { movePlayer(0, -1) },
                    modifier = Modifier.width(72.dp)
                ) {
                    Text("↑", fontSize = 24.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左ボタン
                    Button(
                        onClick = { movePlayer(-1, 0) },
                        modifier = Modifier.width(72.dp)
                    ) {
                        Text("←", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 左右ボタンの間にボタン一つ分の空白を作る
                    Spacer(modifier = Modifier.width(72.dp))
                    Spacer(modifier = Modifier.width(8.dp))

                    // 右ボタン
                    Button(
                        onClick = { movePlayer(1, 0) },
                        modifier = Modifier.width(72.dp)
                    ) {
                        Text("→", fontSize = 24.sp)
                    }
                }

                // 下ボタン
                Button(
                    onClick = { movePlayer(0, 1) },
                    modifier = Modifier.width(72.dp)
                ) {
                    Text("↓", fontSize = 24.sp)
                }
            }
        }
    }
}