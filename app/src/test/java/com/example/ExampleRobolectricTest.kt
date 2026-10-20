package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DocumentJsonHelper
import com.example.data.model.BlockFormatting
import com.example.data.model.BlockType
import com.example.data.model.DocumentBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WordCraft", appName)
  }

  @Test
  fun `test document block serialization roundtrip`() {
    val blocks = listOf(
      DocumentBlock(
        id = "b1",
        type = BlockType.TITLE,
        text = "Annual Strategic Report",
        formatting = BlockFormatting(bold = true, fontSizeSp = 28)
      ),
      DocumentBlock(
        id = "b2",
        type = BlockType.TABLE,
        tableRows = listOf(
          listOf("Q1", "Q2", "Q3"),
          listOf("100", "200", "300")
        )
      )
    )

    val json = DocumentJsonHelper.blocksToJson(blocks)
    val deserialized = DocumentJsonHelper.jsonToBlocks(json)

    assertEquals(2, deserialized.size)
    assertEquals("Annual Strategic Report", deserialized[0].text)
    assertEquals(BlockType.TITLE, deserialized[0].type)
    assertTrue(deserialized[0].formatting.bold)
    assertEquals(2, deserialized[1].tableRows.size)
    assertEquals("Q1", deserialized[1].tableRows[0][0])
  }
}
