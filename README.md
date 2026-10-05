# Motion Lab｜Android accelerometer experiment

一個從早期 Compose 手勢練習重做的 Android 感測器作品。

現在的主題不是 Tap / Drag，而是：**手機的原始加速度資料，怎麼轉成簡單、可解釋、可測試的 motion state？**

App 讀取 accelerometer，將三軸數值轉成 magnitude，在固定視窗內計算相對重力的 RMS 與 peak，再依透明門檻分成：

- **Steady**：手機大致穩定
- **Moving**：有持續移動，但沒有明顯劇烈搖晃
- **Shake**：RMS 或單次 peak 超過門檻

## 為什麼不用「走路 / 跑步」標籤

只靠短視窗 raw accelerometer、沒有個人校正與訓練資料，就把狀態叫做 walking / running 會太武斷。

所以這個版本刻意只描述**手機本身的運動強度**。分類器是 heuristic，不宣稱人體活動辨識準確率。

## 核心流程

```text
Accelerometer x/y/z
        ↓
sqrt(x² + y² + z²)
        ↓
|magnitude - 9.81|
        ↓
50-sample rolling window
        ↓
RMS + Peak
        ↓
Steady / Moving / Shake
```

## 技術

- Kotlin
- Jetpack Compose
- Android SensorManager
- 純 Kotlin motion classifier
- JUnit
- GitHub Actions

## 測試

分類邏輯與 Android UI 分開，因此核心規則不需要模擬器就能測：

```bash
./gradlew testDebugUnitTest
```

測試包含：

- 穩定樣本 → Steady
- 中度波動 → Moving
- 明顯 peak → Shake
- 視窗不足時維持 Collecting
- 門檻行為固定

## 執行環境

- Android minSdk 24
- compileSdk / targetSdk 34
- 實機需要 accelerometer

若裝置沒有 accelerometer，介面會顯示 Sensor unavailable。

## 限制

- heuristic 門檻沒有做跨裝置校正
- 沒有去除裝置方向造成的所有影響
- 沒有使用 gyroscope
- 沒有建立真人 activity recognition dataset
- 沒有聲稱分類結果可用於醫療、安全或運動判定

這個 repo 的重點是感測器資料管線、特徵與可測試規則，不是包裝一個不存在的「AI 辨識準確率」。
