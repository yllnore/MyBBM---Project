package com.mybbm.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBottomSheetPhysicsTest {

    @Test
    fun expandedSheet_smoothDownwardDragPastStepThreshold_collapsesToCompact() {
        val thresholdPx = 72f
        var cumulativeDrag = 0f
        repeat(15) {
            cumulativeDrag += 10f
        }

        val target = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.EXPANDED,
            cumulativeDragPx = cumulativeDrag,
            velocityPxPerSec = 0f,
            stepThresholdPx = thresholdPx
        )

        assertEquals(SheetDetent.COMPACT, target)
    }

    @Test
    fun expandedSheet_longDownwardDragPastJumpThreshold_minimizesToPeek() {
        val thresholdPx = 72f
        val target = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.EXPANDED,
            cumulativeDragPx = 1000f,
            velocityPxPerSec = 0f,
            stepThresholdPx = thresholdPx,
            jumpThresholdPx = 900f
        )

        assertEquals(SheetDetent.PEEK, target)
    }

    @Test
    fun expandedSheet_fastDownwardWhipFlick_minimizesToPeek() {
        val target = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.EXPANDED,
            cumulativeDragPx = 120f,
            velocityPxPerSec = 2500f,
            stepThresholdPx = 72f
        )

        assertEquals(SheetDetent.PEEK, target)
    }

    @Test
    fun expandedSheet_gentleDownwardFlick_collapsesToCompact() {
        val target = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.EXPANDED,
            cumulativeDragPx = 30f,
            velocityPxPerSec = 350f,
            stepThresholdPx = 72f
        )

        assertEquals(SheetDetent.COMPACT, target)
    }

    @Test
    fun expandedSheet_tinyWobbleOrUpwardReversal_staysExpanded() {
        val wobble = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.EXPANDED,
            cumulativeDragPx = 15f,
            velocityPxPerSec = 50f,
            stepThresholdPx = 72f
        )
        val reversedUp = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.EXPANDED,
            cumulativeDragPx = 90f,
            velocityPxPerSec = -400f,
            stepThresholdPx = 72f
        )

        assertEquals(SheetDetent.EXPANDED, wobble)
        assertEquals(SheetDetent.EXPANDED, reversedUp)
    }

    @Test
    fun compactSheet_downwardDragOrFlick_minimizesToPeek() {
        val byDrag = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.COMPACT,
            cumulativeDragPx = 90f,
            velocityPxPerSec = 0f,
            stepThresholdPx = 72f
        )
        val byFlick = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.COMPACT,
            cumulativeDragPx = 20f,
            velocityPxPerSec = 350f,
            stepThresholdPx = 72f
        )

        assertEquals(SheetDetent.PEEK, byDrag)
        assertEquals(SheetDetent.PEEK, byFlick)
    }

    @Test
    fun compactSheet_upwardDragOrFlick_expands() {
        val byDrag = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.COMPACT,
            cumulativeDragPx = -90f,
            velocityPxPerSec = 0f,
            stepThresholdPx = 72f
        )
        val byFlick = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.COMPACT,
            cumulativeDragPx = -20f,
            velocityPxPerSec = -350f,
            stepThresholdPx = 72f
        )
        val wobble = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.COMPACT,
            cumulativeDragPx = -10f,
            velocityPxPerSec = -40f,
            stepThresholdPx = 72f
        )

        assertEquals(SheetDetent.EXPANDED, byDrag)
        assertEquals(SheetDetent.EXPANDED, byFlick)
        assertEquals(SheetDetent.COMPACT, wobble)
    }

    @Test
    fun peekSheet_upwardDragRestoresCompact_andLongOrFastPullExpands() {
        val toCompact = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.PEEK,
            cumulativeDragPx = -90f,
            velocityPxPerSec = 0f,
            stepThresholdPx = 72f
        )
        val toExpandedByDrag = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.PEEK,
            cumulativeDragPx = -1000f,
            velocityPxPerSec = 0f,
            stepThresholdPx = 72f,
            jumpThresholdPx = 900f
        )
        val toExpandedByFlick = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.PEEK,
            cumulativeDragPx = -100f,
            velocityPxPerSec = -2500f,
            stepThresholdPx = 72f
        )
        val staysPeekOnDownward = resolveSheetDetentAfterDrag(
            currentDetent = SheetDetent.PEEK,
            cumulativeDragPx = 80f,
            velocityPxPerSec = 300f,
            stepThresholdPx = 72f
        )

        assertEquals(SheetDetent.COMPACT, toCompact)
        assertEquals(SheetDetent.EXPANDED, toExpandedByDrag)
        assertEquals(SheetDetent.EXPANDED, toExpandedByFlick)
        assertEquals(SheetDetent.PEEK, staysPeekOnDownward)
    }
}
