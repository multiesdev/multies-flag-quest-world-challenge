package com.multies.flagquest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

/**
 * JVM Local Unit Test to verify absolute localization parity across all supported languages:
 * English (base), Arabic, German, and French.
 */
class LocalizationParityTest {

    private val baseFile = File("src/main/res/values/strings.xml")
    private val arFile = File("src/main/res/values-ar/strings.xml")
    private val deFile = File("src/main/res/values-de/strings.xml")
    private val frFile = File("src/main/res/values-fr/strings.xml")

    @Test
    fun testLocalizationParity() {
        // 1. Verify all resource files exist
        assertTrue("Base strings.xml should exist", baseFile.exists())
        assertTrue("Arabic strings.xml should exist", arFile.exists())
        assertTrue("German strings.xml should exist", deFile.exists())
        assertTrue("French strings.xml should exist", frFile.exists())

        // 2. Parse all files
        val baseStrings = parseStringsXml(baseFile)
        val arStrings = parseStringsXml(arFile)
        val deStrings = parseStringsXml(deFile)
        val frStrings = parseStringsXml(frFile)

        println("Found base strings: ${baseStrings.size}")
        println("Found Arabic strings: ${arStrings.size}")
        println("Found German strings: ${deStrings.size}")
        println("Found French strings: ${frStrings.size}")

        // 3. Verify key parity
        val baseKeys = baseStrings.keys
        val arKeys = arStrings.keys
        val deKeys = deStrings.keys
        val frKeys = frStrings.keys

        // Check for missing keys in Arabic
        val missingInAr = baseKeys - arKeys
        assertTrue("Arabic is missing keys: $missingInAr", missingInAr.isEmpty())
        val extraInAr = arKeys - baseKeys
        assertTrue("Arabic has extra keys: $extraInAr", extraInAr.isEmpty())

        // Check for missing keys in German
        val missingInDe = baseKeys - deKeys
        assertTrue("German is missing keys: $missingInDe", missingInDe.isEmpty())
        val extraInDe = deKeys - baseKeys
        assertTrue("German has extra keys: $extraInDe", extraInDe.isEmpty())

        // Check for missing keys in French
        val missingInFr = baseKeys - frKeys
        assertTrue("French is missing keys: $missingInFr", missingInFr.isEmpty())
        val extraInFr = frKeys - baseKeys
        assertTrue("French has extra keys: $extraInFr", extraInFr.isEmpty())

        // 4. Verify no empty translations and match placeholder/format-parity
        for (key in baseKeys) {
            val baseVal = baseStrings[key] ?: ""
            val arVal = arStrings[key] ?: ""
            val deVal = deStrings[key] ?: ""
            val frVal = frStrings[key] ?: ""

            // No empty translations
            assertTrue("Key '$key' is empty in Base", baseVal.trim().isNotEmpty())
            assertTrue("Key '$key' is empty in Arabic", arVal.trim().isNotEmpty())
            assertTrue("Key '$key' is empty in German", deVal.trim().isNotEmpty())
            assertTrue("Key '$key' is empty in French", frVal.trim().isNotEmpty())

            // Format placeholders validation (e.g. %d, %s, %1$d, %1$s, %2$d, etc.)
            val basePlaceholders = extractPlaceholders(baseVal)
            val arPlaceholders = extractPlaceholders(arVal)
            val dePlaceholders = extractPlaceholders(deVal)
            val frPlaceholders = extractPlaceholders(frVal)

            assertEquals("Format placeholder mismatch for key '$key' in Arabic. Expected: $basePlaceholders, Found: $arPlaceholders", basePlaceholders, arPlaceholders)
            assertEquals("Format placeholder mismatch for key '$key' in German. Expected: $basePlaceholders, Found: $dePlaceholders", basePlaceholders, dePlaceholders)
            assertEquals("Format placeholder mismatch for key '$key' in French. Expected: $basePlaceholders, Found: $frPlaceholders", basePlaceholders, frPlaceholders)
        }
    }

    @Test
    fun testSilentMapKeysParity() {
        val baseStrings = parseStringsXml(baseFile)
        val arStrings = parseStringsXml(arFile)
        val deStrings = parseStringsXml(deFile)
        val frStrings = parseStringsXml(frFile)

        val silentMapKeys = listOf(
            "silent_map",
            "silent_map_desc",
            "silent_map_challenge",
            "map_format_silhouette",
            "map_format_continent",
            "map_format_neighbors",
            "map_format_rotated",
            "map_format_island",
            "hint_flag_peek",
            "silent_map_level_completed",
            "streak_bonus",
            "accuracy"
        )

        for (key in silentMapKeys) {
            assertTrue("Key $key missing from base strings.xml", baseStrings.containsKey(key))
            assertTrue("Key $key missing from Arabic strings.xml", arStrings.containsKey(key))
            assertTrue("Key $key missing from German strings.xml", deStrings.containsKey(key))
            assertTrue("Key $key missing from French strings.xml", frStrings.containsKey(key))

            val baseVal = baseStrings[key]!!
            val arVal = arStrings[key]!!
            val deVal = deStrings[key]!!
            val frVal = frStrings[key]!!

            assertTrue("Key $key is empty in base", baseVal.trim().isNotEmpty())
            assertTrue("Key $key is empty in Arabic", arVal.trim().isNotEmpty())
            assertTrue("Key $key is empty in German", deVal.trim().isNotEmpty())
            assertTrue("Key $key is empty in French", frVal.trim().isNotEmpty())
        }
    }

    @Test
    fun testWhoAmIKeysParity() {
        val baseStrings = parseStringsXml(baseFile)
        val arStrings = parseStringsXml(arFile)
        val deStrings = parseStringsXml(deFile)
        val frStrings = parseStringsXml(frFile)

        val whoAmIKeys = listOf(
            "who_am_i",
            "who_am_i_desc",
            "who_am_i_challenge",
            "who_am_i_level_completed",
            "reveal_next_clue",
            "no_more_clues",
            "clue_number",
            "available_points",
            "avg_clues_used",
            "early_answer_streak",
            "score",
            "incorrect",
            "correct_answer"
        )

        for (key in whoAmIKeys) {
            assertTrue("Key $key missing from base strings.xml", baseStrings.containsKey(key))
            assertTrue("Key $key missing from Arabic strings.xml", arStrings.containsKey(key))
            assertTrue("Key $key missing from German strings.xml", deStrings.containsKey(key))
            assertTrue("Key $key missing from French strings.xml", frStrings.containsKey(key))

            val baseVal = baseStrings[key]!!
            val arVal = arStrings[key]!!
            val deVal = deStrings[key]!!
            val frVal = frStrings[key]!!

            assertTrue("Key $key is empty in base", baseVal.trim().isNotEmpty())
            assertTrue("Key $key is empty in Arabic", arVal.trim().isNotEmpty())
            assertTrue("Key $key is empty in German", deVal.trim().isNotEmpty())
            assertTrue("Key $key is empty in French", frVal.trim().isNotEmpty())
        }
    }

    /**
     * Parses an Android strings.xml file into a Key-Value map of string resources.
     */
    private fun parseStringsXml(file: File): Map<String, String> {
        val stringsMap = mutableMapOf<String, String>()
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)
        doc.documentElement.normalize()

        val nList = doc.getElementsByTagName("string")
        for (temp in 0 until nList.length) {
            val nNode = nList.item(temp)
            if (nNode.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                val eElement = nNode as org.w3c.dom.Node
                val name = eElement.attributes.getNamedItem("name").nodeValue
                val value = eElement.textContent
                stringsMap[name] = value
            }
        }
        return stringsMap
    }

    /**
     * Extracts format specifiers (e.g., %s, %d, %1$s, %1$d, %2$d) from a string to verify format safety.
     */
    private fun extractPlaceholders(text: String): List<String> {
        val placeholders = mutableListOf<String>()
        // Match standard java/android formatting specifiers like %d, %s, %1$s, %2$d, %1$d, etc.
        val pattern = Pattern.compile("%(\\d+\\$)?([a-zA-Z])")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            placeholders.add(matcher.group())
        }
        return placeholders
    }
}
