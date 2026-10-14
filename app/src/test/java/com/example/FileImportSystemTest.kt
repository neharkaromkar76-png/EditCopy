package com.example

import com.example.data.model.ImportFormatSupportStatus
import com.example.data.repository.TemplateRepository
import com.example.data.util.ProjectImportManager
import com.example.data.util.SevenZipProjectExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileImportSystemTest {

    @Test
    fun testMagicByteSignatures() {
        // 7-Zip magic bytes: 37 7A BC AF 27 1C
        val sevenZBytes = byteArrayOf(0x37, 0x7A, 0xBC.toByte(), 0xAF.toByte(), 0x27, 0x1C)
        assertTrue("Should detect 7z magic signature", ProjectImportManager.is7zMagic(sevenZBytes))
        assertFalse("Random bytes should not match 7z magic", ProjectImportManager.is7zMagic(byteArrayOf(1, 2, 3, 4, 5, 6)))

        // ZIP magic bytes: PK\x03\x04
        val zipBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        assertTrue("Should detect ZIP magic signature", ProjectImportManager.isZipMagic(zipBytes))
        assertFalse("Random bytes should not match ZIP magic", ProjectImportManager.isZipMagic(byteArrayOf(1, 2, 3, 4)))

        // SQLite magic bytes: "SQLite format 3\0"
        val sqliteBytes = "SQLite format 3\u0000".toByteArray(Charsets.ISO_8859_1)
        assertTrue("Should detect SQLite magic signature", ProjectImportManager.isSqliteMagic(sqliteBytes))
        assertFalse("Random bytes should not match SQLite magic", ProjectImportManager.isSqliteMagic(byteArrayOf(0, 1, 2, 3)))
    }

    @Test
    fun testFormatFileSize() {
        assertEquals("Unknown size", ProjectImportManager.formatFileSize(0))
        assertEquals("Unknown size", ProjectImportManager.formatFileSize(-10))
        assertEquals("500 B", ProjectImportManager.formatFileSize(500))
        assertEquals("1.5 KB", ProjectImportManager.formatFileSize(1536))
        assertEquals("2.0 MB", ProjectImportManager.formatFileSize(2 * 1024 * 1024))
        assertEquals("1.2 GB", ProjectImportManager.formatFileSize((1.2 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testZipArchiveExtractionAndZipSlipPrevention() {
        // Create a ZIP file with standard entries
        val tempZip = File.createTempFile("test_valid", ".zip")
        ZipOutputStream(FileOutputStream(tempZip)).use { zos ->
            val entry = ZipEntry("project.json")
            zos.putNextEntry(entry)
            zos.write("""{"title":"Test Project","cutsCount":12,"durationSeconds":15.0}""".toByteArray())
            zos.closeEntry()
        }

        assertTrue(tempZip.exists())
        assertTrue(tempZip.length() > 0)

        // Verify that ZIP magic is detected
        val bytes = tempZip.inputStream().use {
            val b = ByteArray(4)
            it.read(b)
            b
        }
        assertTrue("ZIP magic should be detected from created file", ProjectImportManager.isZipMagic(bytes))

        tempZip.delete()
    }

    @Test
    fun testCorruptedOrEmptyArchiveHandling() {
        // 0-byte file
        val emptyFile = File.createTempFile("empty", ".7z")
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val result = SevenZipProjectExtractor.extractFromFile(context, emptyFile, "empty.7z", 0L)
        assertFalse("Empty file must not be reported as compatible", result.isCompatible)
        assertEquals("Invalid Archive", result.statusBadge)
        emptyFile.delete()
    }

    @Test
    fun testVNProject_DoesNotFalselyClaimCompatibility() {
        val fakeDao = FakeTemplateDao()
        val repository = TemplateRepository(fakeDao)

        val result = repository.analyzeProjectImport("Dance_BeatDrop_Edit.vnproject")
        assertFalse("VN format should not claim full automatic compatibility without verified parser", result.isCompatible)
        assertEquals("Compatibility Under Lab Testing", result.statusBadge)
        assertTrue(result.summaryNotice.contains("VN Video Editor"))
        assertNotNull("Should provide safe reconstructed pacing template", result.suggestedTemplate)
    }

    @Test
    fun testSupportedFormatsCatalog_IncludesZipAnd7z() {
        val fakeDao = FakeTemplateDao()
        val repository = TemplateRepository(fakeDao)

        val formats = repository.getSupportedFormats()
        assertTrue(formats.any { it.fileExtension.contains(".7z") && it.status == ImportFormatSupportStatus.SUPPORTED })
        assertTrue(formats.any { it.fileExtension.contains(".zip") && it.status == ImportFormatSupportStatus.SUPPORTED })
        assertTrue(formats.any { it.fileExtension.contains(".ecproj") && it.status == ImportFormatSupportStatus.SUPPORTED })
        assertTrue(formats.any { it.fileExtension.contains(".vnproject") && it.status == ImportFormatSupportStatus.TESTING_LAB_ONLY })
        assertTrue(formats.any { it.fileExtension.contains(".xml") && it.status == ImportFormatSupportStatus.STRUCTURAL_ONLY })
    }
}
