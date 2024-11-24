package dev.bltucker.snoozeloo.alarmdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.bltucker.snoozeloo.common.theme.SnoozelooTheme

@Composable
fun AlarmNameDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Alarm Name",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { onNameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter alarm name") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onSave(name) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Empty Name Dialog")
@Composable
private fun EmptyAlarmNameDialogPreview() {
    SnoozelooTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AlarmNameDialog(
                name = "",
                onNameChange = {},
                onDismiss = { },
                onSave = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Dialog with Name")
@Composable
private fun PopulatedAlarmNameDialogPreview() {
    SnoozelooTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AlarmNameDialog(
                name = "Morning Workout",
                onNameChange = {},
                onDismiss = { },
                onSave = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Dialog with Long Name")
@Composable
private fun LongNameAlarmNameDialogPreview() {
    SnoozelooTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AlarmNameDialog(
                name = "Early Morning Team Standup Meeting with International Team",
                onNameChange = {},
                onDismiss = { },
                onSave = { }
            )
        }
    }
}