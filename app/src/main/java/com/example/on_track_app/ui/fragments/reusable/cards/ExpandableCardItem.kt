package com.example.on_track_app.ui.fragments.reusable.cards

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.on_track_app.ui.theme.OnTrackAppTheme


@Composable
fun ExpandableCardItem(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val mod = modifier
        .fillMaxWidth()
    Card(
        modifier = mod
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )
    ){
        Card(
            onClick = { expanded = !expanded },
            modifier = mod
                .padding(end = 5.dp, bottom = 5.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                // Espacio cuando expandido
                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ExpandableCardItemPreview() {
    OnTrackAppTheme(darkTheme = false) {
        ExpandableCardItem(
            title = "Título de ejemplo",
            content = "Este es el contenido que aparece al expandir la tarjeta. "
        )
    }
}

