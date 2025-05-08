package com.labs.applabs.utils

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labs.applabs.R

open class StepIndicatorActivity : AppCompatActivity() {

    protected fun updateStepIndicator(currentStep: Int) {
        val steps = listOf(
            findViewById<TextView>(R.id.step1),
            findViewById<TextView>(R.id.step2),
            findViewById<TextView>(R.id.step3)
        )
        val lines = listOf(
            findViewById<View>(R.id.line1),
            findViewById<View>(R.id.line2)
        )

        for ((i, step) in steps.withIndex()) {
            step.setBackgroundResource(
                if (i <= currentStep) R.drawable.circle_selected else R.drawable.circle_unselected
            )
        }

        for ((i, line) in lines.withIndex()) {
            line.setBackgroundColor(
                if (i < currentStep) getColor(R.color.marineBlue) else getColor(R.color.gray)
            )
        }
    }
}
