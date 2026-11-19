package com.oraculoloto5x.app.data.models

data class NumberAnalysis(
    val ballNumber: Int,
    val cheiroCompoundName: String,
    val cheiroIsFortunate: Boolean,
    val cheiroIsWarning: Boolean,
    val cheiroSingleRoot: Int,
    val cheiroExplanation: String,
    val millmanExplanation: String,
    val millmanMatchesBirthPath: Boolean,
    val phillipsFillsMissingDigit: Boolean,
    val phillipsArrowDescription: String,
    val phillipsPyramidConnection: String,
    val westcottIsPrime: Boolean,
    val westcottIsPerfect: Boolean,
    val westcottExplanation: String,
    val mccantsTriad: String,
    val mccantsIsNaturalMatch: Boolean,
    val historicalFrequencyCount: Int = 0,
    val compositeScore: Double = 0.0
) {
    val singleDigit: Int
        get() {
            var n = ballNumber
            while (n > 9) {
                var s = 0
                var temp = n
                while (temp > 0) {
                    s += temp % 10
                    temp /= 10
                }
                n = s
            }
            return n
        }
}
