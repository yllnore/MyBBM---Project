package com.mybbm.app.util

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
}
