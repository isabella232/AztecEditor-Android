package org.wordpress.aztec

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Html
import android.text.SpannableString
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class ClipboardTest {

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
    fun copyAndPasteSameInlineStyle() {
        editText.fromHtml("<b>Bold</b>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<b>BoldBold</b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteDifferentInlineStyle() {
        editText.fromHtml("<b>Bold</b><i>Italic</i>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<b>Bold</b><i>Italic</i><b>Bold</b><i>Italic</i>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteNestedListItem() {
        editText.fromHtml("<ul><li>aaa</li><li>bbb<ul><li>ccc</li></ul></li></ul>")

        // select "ccc"
        editText.setSelection(8, 11)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<ul><li>aaa</li><li>bbb<ul><li>ccc<ul><li>ccc</li></ul></li></ul></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteMultipleListLevelsWithInlineStyle() {
        editText.fromHtml("<ul><li>aaa</li><li>bb<b>b</b><ul><li>ccc</li></ul></li></ul>")

        // select text starting with "b<b>..."
        editText.setSelection(5, 10)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<ul><li>aaa</li><li>bb<b>b</b><ul><li>ccc<ul><li>b<b>b</b><ul><li>cc</li></ul></li></ul></li></ul></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyUpperListItemAndReplaceEntireText() {
        editText.fromHtml("<ul><li>aaa</li><li>bb<b>b</b><ul><li>ccc</li></ul></li></ul>")

        // select text with "b<b>b</b>"
        editText.setSelection(5, 7)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(0, editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<ul><li>b<b>b</b></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyBlockquoteAndReplaceInlineStyleText() {
        editText.fromHtml("<blockquote>Hello</blockquote><u>Bye</u>End")

        // select text with "Hello"
        editText.setSelection(0, 5)
        TestUtils.copyToClipboard(editText)

        // Select "Bye"
        editText.setSelection(6, 9)
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<blockquote>Hello</blockquote><blockquote>Hello</blockquote>End", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyBlockquoteAndPartiallyReplaceInlineStyleText() {
        editText.fromHtml("<blockquote>Hello</blockquote><u>Bye</u>End")

        // select text with "Hello"
        editText.setSelection(0, 5)
        TestUtils.copyToClipboard(editText)

        // Select "Bye"
        editText.setSelection(7, 9)
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<blockquote>Hello</blockquote><u>B</u><blockquote>Hello</blockquote>End", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteHeadings() {
        editText.fromHtml("<h1>H1</h1><h2>H2</h2><u>Bye</u>End")

        // select half of first and half of second heading
        editText.setSelection(1, 4)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<h1>H1</h1><h2>H2</h2><u>Bye</u>End<h1>1</h1><h2>H</h2>", editText.toHtml())
    }
}