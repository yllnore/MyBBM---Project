package com.mybbm.app.ui.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeBottomSheetPhysicsTest {

    @Test
    fun expandedSheet_smoothDownwardDragPastThreshold_collapsesEvenWithZeroVelocity() {
        val thresholdPx = 120f
        // Simulasi drag halus 120Hz: 15 frame masing-masing 10px (total 150px > 120px)
        var cumulativeDrag = 0f
        repeat(15) {
            cumulativeDrag += 10f
        }

        val remainExpanded = resolveBottomSheetExpansionAfterDrag(
            currentlyExpanded = true,
            cumulativeDragPx = cumulativeDrag,
            velocityPxPerSec = 0f,
            thresholdPx = thresholdPx
        )

        assertFalse("Smooth downward drag > threshold must collapse sheet", remainExpanded)
    }

    @Test
    fun expandedSheet_downwardFlick_collapsesEvenWithShortDistance() {
        val remainExpanded = resolveBottomSheetExpansionAfterDrag(
            currentlyExpanded = true,
            cumulativeDragPx = 30f,
            velocityPxPerSec = 600f,
            thresholdPx = 120f
        )

        assertFalse("Downward flick velocity > 350 px/s must collapse sheet", remainExpanded)
    }

    @Test
    fun expandedSheet_tinyTouchWobble_staysExpanded() {
        val remainExpanded = resolveBottomSheetExpansionAfterDrag(
            currentlyExpanded = true,
            cumulativeDragPx = 15f,
            velocityPxPerSec = 50f,
            thresholdPx = 120f
        )

        assertTrue("Tiny accidental drag must keep sheet expanded", remainExpanded)
    }

    @Test
    fun collapsedSheet_upwardDragOrFlick_expands() {
        val expandedByDrag = resolveBottomSheetExpansionAfterDrag(
            currentlyExpanded = false,
            cumulativeDragPx = -150f,
            velocityPxPerSec = 0f,
            thresholdPx = 120f
        )
        val expandedByFlick = resolveBottomSheetExpansionAfterDrag(
            currentlyExpanded = false,
            cumulativeDragPx = -20f,
            velocityPxPerSec = -500f,
            thresholdPx = 120f
        )
        val stayedCollapsed = resolveBottomSheetExpansionAfterDrag(
            currentlyExpanded = false,
            cumulativeDragPx = -10f,
            velocityPxPerSec = -40f,
            thresholdPx = 120f
        )

        assertTrue(expandedByDrag)
        assertTrue(expandedByFlick)
        assertFalse(stayedCollapsed)
    }
}
