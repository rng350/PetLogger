package com.hfad.petlogger.common.util

class GetWeightDifferenceDisplayUseCase {
    private val getWeightGramsDisplayUseCase = GetWeightGramsDisplayUseCase()
    operator fun invoke(curWeightGramsAmt: Int, prevWeightGramsAmt: Int?): String {
        return if (prevWeightGramsAmt != null) {
            val weightDiffAmt = curWeightGramsAmt - prevWeightGramsAmt
            "${if (weightDiffAmt>=0) "+" else ""}${getWeightGramsDisplayUseCase(weightDiffAmt)}"
        } else "---"
    }
}