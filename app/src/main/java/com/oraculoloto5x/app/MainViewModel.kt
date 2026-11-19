package com.oraculoloto5x.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oraculoloto5x.app.data.knowledge.NumerologyKnowledgeBase
import com.oraculoloto5x.app.data.local.LotoDatabaseHelper
import com.oraculoloto5x.app.data.models.*
import com.oraculoloto5x.app.engine.*
import com.oraculoloto5x.app.notification.PlanetaryAlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = LotoDatabaseHelper(application)

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

    init {
        loadData()
        generateOraclePlay()
    }

    fun loadData() {
        val lottery = _selectedLottery.value
        _savedPlays.value = dbHelper.getSavedPlays(lottery)
        _drawHistory.value = dbHelper.getDrawHistory(lottery)
        refreshPlanetaryClock()
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
            val fusionEngine = LotteryFusionEngine(_userProfile.value, frequencies)

            val result = fusionEngine.generateOraclePlay(
                lotteryType = lottery,
                targetDate = targetDate,
                intentionWord = _intentionWord.value
            )
            _oracleResult.value = result

            // Seleccionar por defecto la primera bolilla de la jugada maestra para análisis
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
        loadData()
    }

    fun insertOfficialDraw(lottery: LotteryType, date: LocalDate, balls: List<Int>) {
        dbHelper.insertDrawResult(lottery, date, balls)
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
}
