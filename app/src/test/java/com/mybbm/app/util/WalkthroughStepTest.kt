package com.mybbm.app.util

import androidx.compose.ui.geometry.Rect
import com.mybbm.app.ui.components.AppWalkthroughSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkthroughStepTest {

    @Test
    fun walkthroughSteps_hasFourNonBlankSteps() {
        assertEquals(4, AppWalkthroughSteps.steps.size)
        AppWalkthroughSteps.steps.forEach { step ->
            assertTrue(step.title.isNotBlank())
            assertTrue(step.description.isNotBlank())
            assertTrue(step.badgeText.isNotBlank())
        }
    }

    @Test
    fun stepNavigation_clampsWithinValidBounds() {
        assertEquals(0, AppWalkthroughSteps.previousStepIndex(0))
        assertEquals(1, AppWalkthroughSteps.nextStepIndex(0))
        assertEquals(3, AppWalkthroughSteps.nextStepIndex(3))
        assertEquals(2, AppWalkthroughSteps.previousStepIndex(3))
        assertFalse(AppWalkthroughSteps.isLastStep(0))
        assertFalse(AppWalkthroughSteps.isLastStep(2))
        assertTrue(AppWalkthroughSteps.isLastStep(3))
    }

    @Test
    fun calculateBubblePlacement_topRightTarget_placesBubbleBelowAndAlignsArrowToRight() {
        val target = Rect(left = 900f, top = 120f, right = 1040f, bottom = 520f)
        val placement = AppWalkthroughSteps.calculateBubblePlacement(
            targetRect = target,
            containerWidthPx = 1080f,
            containerHeightPx = 2400f,
            bubbleWidthPx = 740f,
            bubbleHeightPx = 360f,
            arrowHeightPx = 27f,
            gapPx = 24f,
            horizontalMarginPx = 48f,
            verticalMarginPx = 72f,
            arrowEdgePaddingPx = 66f
        )

        assertFalse("Top-right FABs should place bubble below", placement.placeAbove)
        assertEquals(1080f - 740f - 48f, placement.bubbleLeftPx, 0.5f)
        assertTrue("Arrow should point toward target center X on right side of bubble", placement.arrowOffsetInBubblePx > 600f)
    }

    @Test
    fun calculateBubblePlacement_bottomTarget_placesBubbleAboveTarget() {
        val target = Rect(left = 60f, top = 1850f, right = 1020f, bottom = 1990f)
        val placement = AppWalkthroughSteps.calculateBubblePlacement(
            targetRect = target,
            containerWidthPx = 1080f,
            containerHeightPx = 2400f,
            bubbleWidthPx = 740f,
            bubbleHeightPx = 360f,
            arrowHeightPx = 27f,
            gapPx = 24f,
            horizontalMarginPx = 48f,
            verticalMarginPx = 72f,
            arrowEdgePaddingPx = 66f
        )

        assertTrue("Bottom button should place bubble above", placement.placeAbove)
        assertTrue("Bubble top should be above target top", placement.bubbleTopPx < target.top)
        assertEquals(370f, placement.arrowOffsetInBubblePx, 1f)
    }
}
