package com.example.on_track_app.ui.fragments.reusable.time

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.on_track_app.ui.theme.ButtonColorsReverse

@Composable
fun DateTimeField(
    label: String,
    onOpenCalendar: ()->Unit,
    onTime: (hour:Int, minute:Int) -> Unit,
    withTime: Boolean
) {
    Column(modifier = Modifier.wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // DEADLINE
        ButtonColorsReverse {
                colors ->
            Button(
                onClick = onOpenCalendar,
                colors = colors,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(label)
            }
        }
        if (withTime){
            Spacer(modifier = Modifier.height(8.dp))

            TimePickerField(
                onDismiss = {},
                onTimeSelected = onTime
            )
        }


    }
}