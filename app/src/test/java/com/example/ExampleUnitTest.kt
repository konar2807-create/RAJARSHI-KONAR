package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.components.RichSpan
import com.example.ui.components.RichSpanType
import com.example.ui.components.RichTextState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun richTextState_buildAnnotatedString_appliesStyles() {
        val text = "Hello Bold and Italic World"
        val spans = listOf(
            RichSpan(6, 10, RichSpanType.BOLD),
            RichSpan(15, 21, RichSpanType.ITALIC),
            RichSpan(22, 27, RichSpanType.UNDERLINE)
        )

        val annotatedString = RichTextState.buildAnnotatedString(text, spans)
        assertEquals(text, annotatedString.text)
        assertEquals(3, annotatedString.spanStyles.size)

        // Bold span check
        val boldSpan = annotatedString.spanStyles.find { it.item.fontWeight == FontWeight.Bold }
        assertTrue(boldSpan != null)
        assertEquals(6, boldSpan?.start)
        assertEquals(10, boldSpan?.end)

        // Italic span check
        val italicSpan = annotatedString.spanStyles.find { it.item.fontStyle == FontStyle.Italic }
        assertTrue(italicSpan != null)
        assertEquals(15, italicSpan?.start)
        assertEquals(21, italicSpan?.end)

        // Underline span check
        val underlineSpan = annotatedString.spanStyles.find { it.item.textDecoration == TextDecoration.Underline }
        assertTrue(underlineSpan != null)
        assertEquals(22, underlineSpan?.start)
        assertEquals(27, underlineSpan?.end)
    }

    @Test
    fun richTextState_toggleBold_addsAndRemovesSpan() {
        val state = RichTextState(initialText = "Welcome to WordCraft rich text editor")

        // Select "WordCraft" (index 11 to 20)
        state.onValueChange(state.textFieldValue.copy(selection = TextRange(11, 20)))
        assertFalse(state.isBoldActive)

        // Toggle bold
        state.toggleBold()
        assertTrue(state.isBoldActive)
        assertEquals(1, state.spans.size)
        assertEquals(RichSpanType.BOLD, state.spans[0].type)
        assertEquals(11, state.spans[0].start)
        assertEquals(20, state.spans[0].end)

        // Toggle bold again -> should remove
        state.toggleBold()
        assertFalse(state.isBoldActive)
        assertEquals(0, state.spans.size)
    }

    @Test
    fun richTextState_clearFormatting_resetsSpans() {
        val spans = listOf(
            RichSpan(0, 5, RichSpanType.BOLD),
            RichSpan(6, 10, RichSpanType.ITALIC)
        )
        val state = RichTextState(initialText = "Hello World", initialSpans = spans)
        assertEquals(2, state.spans.size)

        state.clearFormatting()
        assertEquals(0, state.spans.size)
    }

    @Test
    fun richTextState_textAlignment_updatesCorrectly() {
        val state = RichTextState(initialText = "Sample paragraph for alignment testing")
        assertEquals(com.example.data.model.TextAlignment.LEFT, state.alignment)

        state.updateAlignment(com.example.data.model.TextAlignment.CENTER)
        assertEquals(com.example.data.model.TextAlignment.CENTER, state.alignment)

        state.updateAlignment(com.example.data.model.TextAlignment.RIGHT)
        assertEquals(com.example.data.model.TextAlignment.RIGHT, state.alignment)

        state.updateAlignment(com.example.data.model.TextAlignment.JUSTIFY)
        assertEquals(com.example.data.model.TextAlignment.JUSTIFY, state.alignment)
    }
}
