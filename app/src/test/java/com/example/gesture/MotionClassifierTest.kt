package com.example.gesture

import com.example.gesture.motion.MotionClassifier
import com.example.gesture.motion.MotionFeatures
import com.example.gesture.motion.MotionState
import com.example.gesture.motion.MotionWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionClassifierTest {

    @Test
    fun steadySamplesAreClassifiedAsSteady() {
        val features = MotionClassifier.features(List(50) { 9.81f })

        assertEquals(MotionState.STEADY, MotionClassifier.classify(features))
        assertTrue(features.rms < 0.01f)
    }

    @Test
    fun moderateVariationIsMoving() {
        val samples = List(50) { index ->
            if (index % 2 == 0) 10.7f else 8.9f
        }

        val state = MotionClassifier.classify(
            MotionClassifier.features(samples),
        )

        assertEquals(MotionState.MOVING, state)
    }

    @Test
    fun strongPeakIsShake() {
        val samples = MutableList(50) { 9.81f }
        samples[25] = 17.0f

        val state = MotionClassifier.classify(
            MotionClassifier.features(samples),
        )

        assertEquals(MotionState.SHAKE, state)
    }

    @Test
    fun windowCollectsBeforeClassifying() {
        val window = MotionWindow(capacity = 10, minSamples = 5)

        repeat(4) {
            assertEquals(MotionState.COLLECTING, window.add(9.81f).state)
        }

        assertEquals(MotionState.STEADY, window.add(9.81f).state)
    }

    @Test
    fun explicitThresholdsRemainPredictable() {
        assertEquals(
            MotionState.STEADY,
            MotionClassifier.classify(MotionFeatures(rms = 0.2f, peak = 0.4f)),
        )
        assertEquals(
            MotionState.MOVING,
            MotionClassifier.classify(MotionFeatures(rms = 1.0f, peak = 2.0f)),
        )
        assertEquals(
            MotionState.SHAKE,
            MotionClassifier.classify(MotionFeatures(rms = 2.5f, peak = 3.0f)),
        )
    }
}
