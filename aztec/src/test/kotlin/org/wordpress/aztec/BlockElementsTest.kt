package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import org.wordpress.aztec.TestUtils.backspaceAt
import org.wordpress.aztec.TestUtils.safeAppend
import org.wordpress.aztec.TestUtils.safeEmpty
import org.wordpress.aztec.TestUtils.safeLength

/**
 * Testing interactions of multiple block elements
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class BlockElementsTest {
    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithoutExtraSpacing() {
        safeAppend(editText, "some text")
        safeAppend(editText, "\n")
        safeAppend(editText, "quote")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(safeLength(editText))
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "list")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST)
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "some text")

        Assert.assertEquals("some text<blockquote>quote</blockquote><ul><li>list</li></ul>some text", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithExtraSpacing() {
        safeAppend(editText, "some text")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "quote")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<br><br><blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(safeLength(editText))
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "list")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST)
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "some text")

        Assert.assertEquals("some text<br><br><blockquote>quote</blockquote><br><ul><li>list</li></ul><br>some text", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun checkForDanglingListWithoutItems() {
        editText.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)
        Assert.assertEquals("<ol><li></li></ol>", editText.toHtml())
        Assert.assertTrue(safeEmpty(editText))

        backspaceAt(editText, 0)
        Assert.assertTrue(safeEmpty(editText))
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCollapsingEmptyQuoteAboveNewline() {
        safeAppend(editText, "\n")
        editText.setSelection(0)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        editText.text.insert(0, "\n")

        Assert.assertEquals("<br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCollapsingEmptyListAboveNewline() {
        safeAppend(editText, "\n")
        editText.setSelection(0)
        editText.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)
        editText.text.insert(0, "\n")

        Assert.assertEquals("<br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCompletelySurroundedStylingOfMultipleNestedBlocksByQuote() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                " <li>a</li>\n" +
                " <li>b</li>\n" +
                "</ol>\n" +
                "<b>c</b>\n" +
                "<h1>d</h1>")
        val expectedHtml = "<blockquote>a" +
                "<ol>" +
                "<li>a</li>" +
                "<li>b</li>" +
                "</ol>" +
                "<b>c</b>" +
                "<h1>d</h1></blockquote>"

        editText.setSelection(0, editText.length())
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testPartiallySurroundedStylingOfMultipleNestedBlocksByQuote() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                " <li>a</li>\n" +
                " <li>b</li>\n" +
                "</ol>\n" +
                "<b>c</b>\n" +
                "<h1>d</h1>")
        val expectedHtml = "a" +
                "<ol>" +
                "<li>a</li>" +
                "<li><blockquote>b</blockquote></li>" +
                "</ol>" +
                "<blockquote><b>c</b>" +
                "<h1>d</h1></blockquote>"

        editText.setSelection(4, editText.length())
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testPartiallySurroundedStylingOfListByQuote() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                "<li>a</li>\n" +
                "<li>b</li>\n" +
                "</ol>\n" +
                "<b>c</b>\n" +
                "<h1>d</h1>")
        val expectedHtml = "a" +
                "<blockquote><ol>" +
                "<li>a</li>" +
                "<li>b</li>" +
                "</ol></blockquote>" +
                "<blockquote><b>c</b>" +
                "<h1>d</h1></blockquote>"

        editText.setSelection(4, editText.length())
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testEntirelySelectedListItemByQuote() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                "<li>1</li>\n" +
                "<li>b</li>\n" +
                "</ol>")
        val expectedHtml = "a" +
                "<ol>" +
                "<li><blockquote>1</blockquote></li>" +
                "<li>b</li>" +
                "</ol>"

        val index = editText.text.indexOf("1")
        editText.setSelection(index, index + 1)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun tesListItemSurroundedByQuoteWithCursorAtTheEnd() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                "<li>1</li>\n" +
                "<li>b</li>\n" +
                "</ol>")
        val expectedHtml = "a" +
                "<ol>" +
                "<li><blockquote>1</blockquote></li>" +
                "<li>b</li>" +
                "</ol>"

        val index = editText.text.indexOf("1")
        editText.setSelection(index + 1)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun tesListItemSurroundedByQuoteWithCursorAtTheBeginning() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                "<li>1</li>\n" +
                "<li>b</li>\n" +
                "</ol>")
        val expectedHtml = "a" +
                "<ol>" +
                "<li><blockquote>1</blockquote></li>" +
                "<li>b</li>" +
                "</ol>"

        val index = editText.text.indexOf("1")
        editText.setSelection(index)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun tesListItemWithHeading() {
        editText.fromHtml("a\n" +
                "<ol>\n" +
                "<li>1</li>\n" +
                "<li>b</li>\n" +
                "</ol>")
        val expectedHtml = "a" +
                "<ol>" +
                "<li><h1>1</h1></li>" +
                "<li>b</li>" +
                "</ol>"

        val index = editText.text.indexOf("1")
        editText.setSelection(index)
        editText.toggleFormatting(AztecTextFormat.FORMAT_HEADING_1)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleLineStylingInsideListWithQuote() {
        editText.fromHtml("<ul>\n" +
                "<li>a</li>\n" +
                "<li>b</li>\n" +
                "<li>c</li>\n" +
                "</ul>")
        val expectedHtml = "<ul>" +
                "<li>a</li>" +
                "<li><blockquote>b</blockquote></li>" +
                "<li><blockquote>c</blockquote></li>" +
                "</ul>"

        val index = editText.text.indexOf("b")
        editText.setSelection(index + 1, index + 2)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleLineStylingInsideListWithHeading() {
        editText.fromHtml("<ul>\n" +
                "<li>a</li>\n" +
                "<li>b</li>\n" +
                "<li>c</li>\n" +
                "</ul>")
        val expectedHtml = "<ul>" +
                "<li>a</li>" +
                "<li><h2>b</h2></li>" +
                "<li><h2>c</h2></li>" +
                "</ul>"

        val index = editText.text.indexOf("b")
        editText.setSelection(index + 1, index + 3)
        editText.toggleFormatting(AztecTextFormat.FORMAT_HEADING_2)

        Assert.assertEquals(expectedHtml, editText.toHtml())
    }
}
