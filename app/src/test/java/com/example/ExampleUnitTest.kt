package com.example

import com.example.data.local.TemplateDao
import com.example.data.local.TemplateEntity
import com.example.data.model.ImportFormatSupportStatus
import com.example.data.model.TemplateCategory
import com.example.data.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTemplateDao : TemplateDao {
    private val storage = mutableMapOf<String, TemplateEntity>()

    override fun getAllTemplates(): Flow<List<TemplateEntity>> = flowOf(storage.values.toList())
    override fun getFavoriteTemplates(): Flow<List<TemplateEntity>> = flowOf(storage.values.filter { it.isFavorite })
    override fun getUserCreatedTemplates(): Flow<List<TemplateEntity>> = flowOf(storage.values.filter { it.isUserCreated })
    override suspend fun getTemplateById(id: String): TemplateEntity? = storage[id]
    override suspend fun insertTemplate(template: TemplateEntity) { storage[template.id] = template }
    override suspend fun insertAll(templates: List<TemplateEntity>) { templates.forEach { storage[it.id] = it } }
    override suspend fun updateFavorite(id: String, isFavorite: Boolean) {
        storage[id]?.let { storage[id] = it.copy(isFavorite = isFavorite) }
    }
    override suspend fun renameTemplate(id: String, newTitle: String, timestamp: Long) {
        storage[id]?.let { storage[id] = it.copy(title = newTitle, updatedAtTimestamp = timestamp) }
    }
    override suspend fun deleteTemplateById(id: String) { storage.remove(id) }
}

class ExampleUnitTest {

    @Test
    fun testDefaultTemplatesAvailable() {
        val templates = TemplateRepository.DEFAULT_TEMPLATES
        assertTrue("Should have default templates preloaded", templates.isNotEmpty())
        val cyberVelocity = templates.first { it.id == "tpl_cyber_velocity" }
        assertEquals("Cyber Velocity Ramp", cyberVelocity.title)
        assertEquals(TemplateCategory.BEAT_SYNC, cyberVelocity.category)
        assertTrue(cyberVelocity.cutsCount > 0)
        assertTrue(cyberVelocity.clipSlots.isNotEmpty())
    }

    @Test
    fun testAnalyzeProjectImport_VNProject_ReportsLabTesting() {
        val fakeDao = FakeTemplateDao()
        val repository = TemplateRepository(fakeDao)

        val result = repository.analyzeProjectImport("dance_trend.vnproject")
        assertFalse("VN format should not claim full automatic compatibility without verified parser", result.isCompatible)
        assertEquals("Compatibility Under Lab Testing", result.statusBadge)
        assertTrue(result.summaryNotice.contains("VN Video Editor"))
        assertNotNull(result.suggestedTemplate)
    }

    @Test
    fun testAnalyzeProjectImport_EditCopyNative_IsVerified() {
        val fakeDao = FakeTemplateDao()
        val repository = TemplateRepository(fakeDao)

        val result = repository.analyzeProjectImport("speed_ramp.ecproj")
        assertTrue("EditCopy native format should be verified", result.isCompatible)
        assertEquals("Verified & Compatible", result.statusBadge)
        assertTrue(result.extractedCuts > 0)
    }

    @Test
    fun testSupportedFormatsCatalog() {
        val fakeDao = FakeTemplateDao()
        val repository = TemplateRepository(fakeDao)

        val formats = repository.getSupportedFormats()
        assertTrue(formats.any { it.fileExtension.contains(".ecproj") && it.status == ImportFormatSupportStatus.SUPPORTED })
        assertTrue(formats.any { it.fileExtension.contains(".vnproject") && it.status == ImportFormatSupportStatus.TESTING_LAB_ONLY })
    }
}
