package com.example.petsmatchingapp.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox


class JFCheckBox(context: Context, attributeSet: AttributeSet): AppCompatCheckBox(context, attributeSet) {

    init {
        applyFont()
    }


    private fun applyFont(){
        val typeface: Typeface = Typeface.createFromAsset(context.assets,"jfopenhuninn.ttf")
        setTypeface(typeface)
    }

}