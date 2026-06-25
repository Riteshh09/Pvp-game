package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class MoshiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class MoshiContent(
    val parts: List<MoshiPart>
)

@JsonClass(generateAdapter = true)
data class MoshiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class MoshiGenerateRequest(
    val contents: List<MoshiContent>,
    val generationConfig: MoshiGenerationConfig? = null,
    val systemInstruction: MoshiContent? = null
)

@JsonClass(generateAdapter = true)
data class MoshiCandidate(
    val content: MoshiContent? = null
)

@JsonClass(generateAdapter = true)
data class MoshiResponse(
    val candidates: List<MoshiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class EliteChampion(
    val name: String,
    val title: String,
    val hp: Int,
    val specialAttackName: String,
    val specialAttackPower: Int,
    val battleCry: String,
    val backgroundStory: String
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: MoshiGenerateRequest
    ): MoshiResponse
}

object GeminiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    val moshiInstance: Moshi get() = moshi
}

class GeminiService {

    private val tag = "GeminiService"

    // Default key check
    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key == "MY_GEMINI_API_KEY" || key.isBlank()) "" else key
    }

    suspend fun generateEliteChampion(playerRating: Int): EliteChampion {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            Log.w(tag, "API Key is missing or default. Returning mock elite champion.")
            return getDefaultEliteChampion(playerRating)
        }

        val prompt = """
            Create a unique Cyberpunk/Space Gladiator "Elite Champion" boss for our PvP real-time battle game. 
            The current player has an ELO rating of $playerRating. Scale the boss's power, title, and name accordingly.
            Provide the output STRICTLY in the following JSON format:
            {
              "name": "A unique sci-fi/cyberpunk warrior name",
              "title": "A cool boss title (e.g., Aether Overlord, Neural Renegade)",
              "hp": A suitable health value between 110 and 150 based on rating,
              "specialAttackName": "A flashy special attack name (e.g., Neon Plasma Overdrive, Chrono Blade Vortex)",
              "specialAttackPower": A suitable integer between 20 and 35,
              "battleCry": "A confident, atmospheric challenge quote (15 words max)",
              "backgroundStory": "A short cyberpunk bio of how they conquered the arena (30 words max)"
            }
        """.trimIndent()

        val request = MoshiGenerateRequest(
            contents = listOf(MoshiContent(parts = listOf(MoshiPart(text = prompt)))),
            generationConfig = MoshiGenerationConfig(responseMimeType = "application/json", temperature = 1.0f),
            systemInstruction = MoshiContent(parts = listOf(MoshiPart(text = "You are a specialized sci-fi game designer. Return ONLY the JSON object. Do not include markdown code block tags.")))
        )

        return try {
            val response = GeminiClient.api.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                // Parse JSON using Moshi
                val cleanJson = jsonText.trim().removeSurrounding("```json", "```").trim()
                val adapter = GeminiClient.moshiInstance.adapter(EliteChampion::class.java)
                adapter.fromJson(cleanJson) ?: getDefaultEliteChampion(playerRating)
            } else {
                getDefaultEliteChampion(playerRating)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to generate Elite Champion", e)
            getDefaultEliteChampion(playerRating)
        }
    }

    suspend fun generateCombatCoaching(combatLogs: List<String>): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return "Perfect your defensive timing. Striking immediately after blocking your opponent's heaviest hit breaks their posture and guarantees a high-critical blow!"
        }

        val formattedLogs = combatLogs.joinToString("\n")
        val prompt = """
            Review the following combat logs from our real-time PvP action game and provide a single concise, tactical coaching tip (max 40 words) in a mentoring, cybernetic tone:
            
            $formattedLogs
            
            Give the player a clear strategic recommendation based on their hits, blocks, or ultimate use.
        """.trimIndent()

        val request = MoshiGenerateRequest(
            contents = listOf(MoshiContent(parts = listOf(MoshiPart(text = prompt)))),
            generationConfig = MoshiGenerationConfig(temperature = 0.7f),
            systemInstruction = MoshiContent(parts = listOf(MoshiPart(text = "You are an AI Combat Coach in Aether PvP Arena. Give short, direct tactical advice.")))
        )

        return try {
            val response = GeminiClient.api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Optimize your Ultimate deployment. Ensure the opponent's Energy bar is depleted before discharging your Overload to maximize unblocked piercing damage."
        } catch (e: Exception) {
            Log.e(tag, "Failed to generate combat coaching", e)
            "Keep an eye on enemy Energy. When they approach 100%, prepare to initiate your Dodge (Evade) or raise your Defend Shield to absorb their Overload attack."
        }
    }

    suspend fun generateTrashTalk(opponentName: String, opponentClass: String, pHP: Int, oHP: Int): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return if (pHP > oHP) "Your defense is full of holes, I can see every strike coming!" else "Just a minor glitch in my database. I'm taking you down!"
        }

        val prompt = """
            Generate a short, snappy trash talk or competitive banter line from opponent '$opponentName' (Class: $opponentClass). 
            Current combat health: Player has $pHP HP, Opponent has $oHP HP. Keep it under 12 words, sci-fi/cyberpunk flavored.
        """.trimIndent()

        val request = MoshiGenerateRequest(
            contents = listOf(MoshiContent(parts = listOf(MoshiPart(text = prompt)))),
            generationConfig = MoshiGenerationConfig(temperature = 1.0f)
        )

        return try {
            val response = GeminiClient.api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Your neural circuits are too slow for my blade!"
        } catch (e: Exception) {
            "Initiating clean-up protocol. You're history!"
        }
    }

    private fun getDefaultEliteChampion(rating: Int): EliteChampion {
        return when {
            rating >= 1500 -> EliteChampion(
                name = "Vesper-9",
                title = "Void Archon",
                hp = 140,
                specialAttackName = "Quantum Horizon Shatter",
                specialAttackPower = 32,
                battleCry = "The dark space hears no screams, champion. Prepare to dissolve!",
                backgroundStory = "A synthetic warrior crafted in deep void labs, undefeated for 99 consecutive solar cycles."
            )
            rating >= 1200 -> EliteChampion(
                name = "Kira Hex",
                title = "Cybernetic Overlord",
                hp = 125,
                specialAttackName = "Nanotech Singularity",
                specialAttackPower = 28,
                battleCry = "Your simple biological reflexes cannot match my overclocked algorithms!",
                backgroundStory = "A rogue cyber-hacker who fused her consciousness with the arena's core processor."
            )
            else -> EliteChampion(
                name = "Nexus-X",
                title = "Scrap-Iron Gladiator",
                hp = 115,
                specialAttackName = "Aether Chainsaw Strike",
                specialAttackPower = 22,
                battleCry = "More scrap metal for my collection. Let's see if you can bleed!",
                backgroundStory = "Formed from the remains of broken security drones, powered by pure arena hazard current."
            )
        }
    }
}
