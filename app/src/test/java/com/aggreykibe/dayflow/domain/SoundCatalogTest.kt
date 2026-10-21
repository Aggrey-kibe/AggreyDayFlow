package com.aggreykibe.dayflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Keeps the sound list, the audio files in res/raw and the app's defaults in step with each other. */
class SoundCatalogTest {
    private val ids = SoundCatalog.bundled.map { it.id }

    @Test fun idsAreUniqueAndValidAndroidResourceNames() {
        assertEquals(ids.size, ids.toSet().size)
        for (id in ids) assertTrue("bad resource name: $id", Regex("^[a-z][a-z0-9_]*$").matches(id))
        assertFalse(SoundIds.SYSTEM in ids)
    }

    @Test fun namesAreFilledInAndDifferent() {
        val names = SoundCatalog.bundled.map { it.name }
        assertTrue(names.all { it.isNotBlank() })
        assertEquals(names.size, names.toSet().size)
    }

    @Test fun libraryIsBigAndEveryMoodHasSounds() {
        assertTrue("expected a large library, found ${ids.size}", ids.size >= 30)
        for (vibe in SoundVibe.values()) {
            assertTrue("no sounds for $vibe", SoundCatalog.bundled.any { it.vibe == vibe })
        }
    }

    @Test fun defaultsAreValid() {
        assertTrue(SoundIds.DEFAULT in ids)
        assertEquals(SoundIds.SYSTEM, SoundIds.all.first())
        assertEquals(ids.size + 1, SoundIds.all.size)
        assertEquals(SoundIds.DEFAULT, SoundIds.orDefault(null))
        assertEquals(SoundIds.DEFAULT, SoundIds.orDefault("not-a-sound"))
        assertEquals(SoundIds.SYSTEM, SoundIds.orDefault(SoundIds.SYSTEM))
        assertEquals("bass_drop", SoundIds.orDefault("bass_drop"))
        assertEquals(SoundIds.DEFAULT, Reminder(1, "x", java.time.LocalDate.of(2026, 1, 1), java.time.LocalTime.NOON).soundId)
        assertEquals(SoundIds.DEFAULT, AppSettings().soundId)
    }

    @Test fun everySoundHasARealAudioFileAndNoFileIsLeftOut() {
        val dir = listOf(File("src/main/res/raw"), File("app/src/main/res/raw")).firstOrNull { it.isDirectory }
        assumeTrue("res/raw not found from this working directory", dir != null)
        val files = dir!!.listFiles { f -> f.isFile }!!.associateBy { it.name }
        for (id in ids) {
            val f = files["$id.ogg"]
            assertTrue("missing audio file for $id", f != null)
            assertTrue("$id is too small to be real audio (${f!!.length()} bytes)", f.length() > 4_000)
            assertTrue("$id is unexpectedly large (${f.length()} bytes)", f.length() < 300_000)
        }
        val orphans = files.keys - ids.map { "$it.ogg" }.toSet()
        assertTrue("audio files not in the catalog: $orphans", orphans.isEmpty())
    }
}
