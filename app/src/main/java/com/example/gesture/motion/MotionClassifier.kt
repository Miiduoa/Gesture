package com.example.gesture.motion

import kotlin.math.abs
import kotlin.math.sqrt

enum class MotionState {
    COLLECTING,
    STEADY,
    MOVING,
    SHAKE,
    UNAVAILABLE,
}

data class MotionFeatures(
    val rms: Float = 0f,
    val peak: Float = 0f,
)

data class MotionReading(
    val state: MotionState,
    val features: MotionFeatures,
    val sampleCount: Int,
)

object MotionClassifier {
    private const val GRAVITY = 9.81f
    private const val STEADY_RMS = 0.35f
    private const val SHAKE_RMS = 2.20f
    private const val SHAKE_PEAK = 5.50f

    fun features(magnitudes: List<Float>): MotionFeatures {
        if (magnitudes.isEmpty()) return MotionFeatures()

        val dynamic = magnitudes.map { abs(it - GRAVITY) }
        val rms = sqrt(dynamic.sumOf { value ->
            (value * value).toDouble()
        } / dynamic.size).toFloat()
        val peak = dynamic.maxOrNull() ?: 0f

        return MotionFeatures(rms = rms, peak = peak)
    }

    fun classify(features: MotionFeatures): MotionState {
        return when {
            features.rms < STEADY_RMS -> MotionState.STEADY
            features.rms >= SHAKE_RMS || features.peak >= SHAKE_PEAK -> MotionState.SHAKE
            else -> MotionState.MOVING
        }
    }
}

class MotionWindow(
    private val capacity: Int = 50,
    private val minSamples: Int = 20,
) {
    private val samples = ArrayDeque<Float>()

    init {
        require(capacity > 0)
        require(minSamples in 1..capacity)
    }

    fun add(magnitude: Float): MotionReading {
        samples.addLast(magnitude)

        while (samples.size > capacity) {
            samples.removeFirst()
        }

        val features = MotionClassifier.features(samples.toList())
        val state = if (samples.size < minSamples) {
            MotionState.COLLECTING
        } else {
            MotionClassifier.classify(features)
        }

        return MotionReading(
            state = state,
            features = features,
            sampleCount = samples.size,
        )
    }
}
