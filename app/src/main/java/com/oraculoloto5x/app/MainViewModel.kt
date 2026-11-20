package com.oraculoloto5x.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oraculoloto5x.app.ai.GeminiOracleAdvisor
import com.oraculoloto5x.app.ai.GeminiResult
import com.oraculoloto5x.app.ai.HermeticKnowledgeRetriever
import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.local.LotoDatabaseHelper
import com.oraculoloto5x.app.data.models.*
import com.oraculoloto5x.app.data.sync.SyncResult
import com.oraculoloto5x.app.data.sync.TinkaOnlineSyncService
import com.oraculoloto5x.app.engine.*
import com.oraculoloto5x.app.notification.PlanetaryAlarmScheduler
import com.oraculoloto5x.app.pdf.BookCitation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val dbHelper = LotoDatabaseHelper(application)

    private val _userProfile = MutableStateFlow(dbHelper.getUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _selectedLottery = MutableStateFlow(LotteryType.LA_TINKA)
    val selectedLottery: StateFlow<LotteryType> = _selectedLottery.asStateFlow()

    private val _intentionWord = MutableStateFlow("")
    val intentionWord: StateFlow<String> = _intentionWord.asStateFlow()

    private val _oracleResult = MutableStateFlow<OracleGenerationResult?>(null)
    val oracleResult: StateFlow<OracleGenerationResult?> = _oracleResult.asStateFlow()

    private val _currentPlanetaryHour = MutableStateFlow(
        PlanetaryClockEngine(dbHelper.getUserProfile()).getCurrentPlanetaryHour()
    )
    val currentPlanetaryHour: StateFlow<PlanetaryHour> = _currentPlanetaryHour.asStateFlow()

    private val _nextFavorableWindow = MutableStateFlow(
        PlanetaryClockEngine(dbHelper.getUserProfile()).getTimeToNextFavorableWindow()
    )
    val nextFavorableWindow: StateFlow<Pair<PlanetaryHour, Duration>> = _nextFavorableWindow.asStateFlow()

    private val _sunTimes = MutableStateFlow(
        PlanetaryClockEngine(dbHelper.getUserProfile()).calculateSunTimes(LocalDate.now())
    )
    val sunTimes: StateFlow<Pair<LocalTime, LocalTime>> = _sunTimes.asStateFlow()

    private val _cutoffStatus = MutableStateFlow(
        PlanetaryClockEngine(dbHelper.getUserProfile()).checkLaTinkaCutoffStatus()
    )
    val cutoffStatus: StateFlow<CutoffAlertStatus> = _cutoffStatus.asStateFlow()

    private val _savedPlays = MutableStateFlow<List<SavedPlay>>(emptyList())
    val savedPlays: StateFlow<List<SavedPlay>> = _savedPlays.asStateFlow()

    private val _drawHistory = MutableStateFlow<List<DrawResult>>(emptyList())
    val drawHistory: StateFlow<List<DrawResult>> = _drawHistory.asStateFlow()

    private val _selectedBallForAnalysis = MutableStateFlow<NumberAnalysis?>(null)
    val selectedBallForAnalysis: StateFlow<NumberAnalysis?> = _selectedBallForAnalysis.asStateFlow()

    private val _isProfileDialogOpen = MutableStateFlow(false)
    val isProfileDialogOpen: StateFlow<Boolean> = _isProfileDialogOpen.asStateFlow()

    private val _isAddDrawDialogOpen = MutableStateFlow(false)
    val isAddDrawDialogOpen: StateFlow<Boolean> = _isAddDrawDialogOpen.asStateFlow()

    private val _isBallSheetOpen = MutableStateFlow(false)
    val isBallSheetOpen: StateFlow<Boolean> = _isBallSheetOpen.asStateFlow()

    private val _hasPlaySavedToday = MutableStateFlow(false)
    val hasPlaySavedToday: StateFlow<Boolean> = _hasPlaySavedToday.asStateFlow()

    // ── Chat & Gemini AI State ────────────────────────────────────────────────
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Compatibilidad para ProfileSettingsDialog y TodayOracleScreen
    private val _geminiResult = MutableStateFlow<GeminiResult?>(null)
    val geminiResult: StateFlow<GeminiResult?> = _geminiResult.asStateFlow()

    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    // ── Online Sync State ─────────────────────────────────────────────────────
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // ── PDF Citation Direct Viewer ────────────────────────────────────────────
    private val _activePdfCitation = MutableStateFlow<BookCitation?>(null)
    val activePdfCitation: StateFlow<BookCitation?> = _activePdfCitation.asStateFlow()

    private val _pdfBooksLinked = MutableStateFlow(0)
    val pdfBooksLinked: StateFlow<Int> = _pdfBooksLinked.asStateFlow()

    init {
        loadData()
        loadChatHistory()
        generateOraclePlay()

        // Sincronización silenciosa en segundo plano al iniciar si hay internet
        viewModelScope.launch {
            syncTinkaDrawsOnline(showFeedback = false)
        }
    }

    fun loadData() {
        val lottery = _selectedLottery.value
        _savedPlays.value = dbHelper.getSavedPlays(lottery)
        _drawHistory.value = dbHelper.getDrawHistory(lottery)
        refreshPlanetaryClock()
    }

    fun loadChatHistory() {
        _chatMessages.value = dbHelper.getChatHistory(50)
    }

    fun refreshPlanetaryClock(date: LocalDate = LocalDate.now(), time: LocalTime = LocalTime.now()) {
        val engine = PlanetaryClockEngine(_userProfile.value)
        _currentPlanetaryHour.value = engine.getCurrentPlanetaryHour(date, time)
        _nextFavorableWindow.value = engine.getTimeToNextFavorableWindow(date, time)
        _sunTimes.value = engine.calculateSunTimes(date)
        _cutoffStatus.value = engine.checkLaTinkaCutoffStatus(date, time)
    }

    fun selectLottery(lottery: LotteryType) {
        _selectedLottery.value = lottery
        loadData()
        generateOraclePlay()
    }

    fun setIntentionWord(word: String) {
        _intentionWord.value = word
    }

    fun generateOraclePlay(targetDate: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val lottery = _selectedLottery.value
            val frequencies = dbHelper.getHistoricalFrequencies(lottery)
            val lastDraw: List<Int> = _drawHistory.value.firstOrNull()?.winningBalls ?: emptyList()
            val fusionEngine = LotteryFusionEngine(
                userProfile = _userProfile.value,
                historicalFrequencies = frequencies,
                lastOfficialDrawBalls = lastDraw
            )

            val result = fusionEngine.generateOraclePlay(
                lotteryType = lottery,
                targetDate = targetDate,
                intentionWord = _intentionWord.value
            )
            _oracleResult.value = result

            if (result.masterPlay.isNotEmpty()) {
                val firstBall = result.masterPlay.first()
                _selectedBallForAnalysis.value = result.allBallsAnalysis.find { it.ballNumber == firstBall }
            }
        }
    }

    fun selectBallForAnalysis(ball: Int) {
        val result = _oracleResult.value ?: return
        _selectedBallForAnalysis.value = result.allBallsAnalysis.find { it.ballNumber == ball }
    }

    fun openBallSheet(ball: Int) {
        selectBallForAnalysis(ball)
        _isBallSheetOpen.value = true
    }

    fun closeBallSheet() {
        _isBallSheetOpen.value = false
    }

    fun openPdfCitation(citation: BookCitation) {
        _activePdfCitation.value = citation
    }

    fun closePdfCitation() {
        _activePdfCitation.value = null
    }

    // ── Chat Conversacional Inteligente ───────────────────────────────────────

    fun sendChatMessage(question: String) {
        if (question.isBlank() || _isChatLoading.value) return
        val currentPlay = _oracleResult.value

        val userMsg = ChatMessage(
            timestamp = System.currentTimeMillis(),
            sender = "user",
            text = question.trim(),
            relatedBalls = currentPlay?.masterPlay?.joinToString("-") ?: ""
        )

        // Guardar de inmediato mensaje de usuario en memoria y DB
        dbHelper.saveChatMessage(userMsg)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatLoading.value = true

            val contextBundle = HermeticKnowledgeRetriever.buildContextBundle(
                masterPlayBalls = currentPlay?.masterPlay ?: emptyList(),
                riskAssessment = currentPlay?.riskAssessment,
                currentHour = _currentPlanetaryHour.value,
                moonInfo = currentPlay?.moonInfo,
                lastOfficialDraw = _drawHistory.value.firstOrNull()?.winningBalls
            )

            val result = GeminiOracleAdvisor.chatTurn(
                context = getApplication(),
                systemInstruction = contextBundle.systemInstruction,
                history = _chatMessages.value.dropLast(1), // Historial previo excluyendo la pregunta actual
                newQuestion = question.trim()
            )

            val oracleReplyText = when (result) {
                is GeminiResult.Success -> result.text
                is GeminiResult.Offline -> "Modo offline activo. Toca cualquier bolilla de tu jugada maestra para ver sus páginas en los 5 libros locales sin internet."
                is GeminiResult.NoApiKey -> "La API Key de Google AI Studio no está configurada. Puedes configurarla en el menú Ajustes (arriba a la derecha)."
                is GeminiResult.Error -> "⚠ ${result.message}"
            }

            val oracleMsg = ChatMessage(
                timestamp = System.currentTimeMillis(),
                sender = "oracle",
                text = oracleReplyText,
                relatedBalls = currentPlay?.masterPlay?.joinToString("-") ?: ""
            )

            dbHelper.saveChatMessage(oracleMsg)
            _chatMessages.value = _chatMessages.value + oracleMsg
            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        dbHelper.clearChatHistory()
        _chatMessages.value = emptyList()
    }

    // ── Sincronización Online de La Tinka ──────────────────────────────────────

    fun syncTinkaDrawsOnline(showFeedback: Boolean = true) {
        viewModelScope.launch {
            _isSyncing.value = true
            if (showFeedback) _syncMessage.value = "Sincronizando sorteos..."

            val res = TinkaOnlineSyncService.syncTinkaHistory(getApplication(), dbHelper)

            when (res) {
                is SyncResult.Success -> {
                    loadData()
                    generateOraclePlay() // Recalcular con nuevo cooldown aplicado
                    if (showFeedback) {
                        _syncMessage.value = "✓ ¡${res.newDrawsCount} sorteo(s) nuevo(s) sincronizado(s) hasta ${res.latestDate}!"
                    }
                }
                is SyncResult.AlreadyUpToDate -> {
                    if (showFeedback) {
                        _syncMessage.value = "✓ Historial al día con el último sorteo oficial."
                    }
                }
                is SyncResult.Offline -> {
                    if (showFeedback) {
                        _syncMessage.value = "Sin conexión. Usando sorteos guardados offline."
                    }
                }
                is SyncResult.Error -> {
                    if (showFeedback) {
                        _syncMessage.value = "Aviso sync: ${res.message}"
                    }
                }
            }

            _isSyncing.value = false
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun saveCurrentPlay() {
        val result = _oracleResult.value ?: return
        val currentHour = _currentPlanetaryHour.value

        val play = SavedPlay(
            lotteryType = result.lotteryType,
            generatedDate = LocalDate.now(),
            targetDrawDate = result.targetDrawDate,
            playType = "MAESTRA",
            balls = result.masterPlay,
            riskLevel = result.riskAssessment.riskLevel.name,
            planetaryHour = currentHour.planet.spanishName
        )

        dbHelper.savePlay(play)
        _hasPlaySavedToday.value = true
        loadData()
    }

    fun updateProfile(newProfile: UserProfile) {
        dbHelper.updateUserProfile(newProfile)
        _userProfile.value = newProfile
        refreshPlanetaryClock()
        if (newProfile.notificationsEnabled) {
            PlanetaryAlarmScheduler.scheduleFavorableAlarms(getApplication())
        }
        generateOraclePlay()
        setProfileDialogOpen(false)
    }

    fun setProfileDialogOpen(open: Boolean) {
        _isProfileDialogOpen.value = open
    }

    fun setAddDrawDialogOpen(open: Boolean) {
        _isAddDrawDialogOpen.value = open
    }

    fun get24PlanetaryHours(date: LocalDate): List<PlanetaryHour> {
        val engine = PlanetaryClockEngine(_userProfile.value)
        return engine.calculatePlanetaryHours(date)
    }

    // Métodos de compatibilidad para ProfileSettingsDialog, FiveBooks y pantallas auxiliares
    fun testGeminiApiKey(apiKey: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = GeminiOracleAdvisor.testApiKey(apiKey)
            res.fold(
                onSuccess = { onResult(true, it) },
                onFailure = { onResult(false, it.message ?: "Error al conectar") }
            )
        }
    }

    fun clearGeminiResult() {
        _geminiResult.value = null
    }

    fun consultGemini(customQuestion: String? = null) {
        val q = if (customQuestion.isNullOrBlank()) "Analiza mi jugada de hoy y el semáforo" else customQuestion.trim()
        sendChatMessage(q)
    }

    fun refreshPdfCount() {
        viewModelScope.launch {
            _pdfBooksLinked.value = com.oraculoloto5x.app.pdf.NativePdfManager.countFoundBooks(getApplication())
        }
    }

    fun insertOfficialDraw(lottery: LotteryType, date: LocalDate, balls: List<Int>) {
        dbHelper.insertDrawResult(lottery, date, balls)
        loadData()
    }
}
