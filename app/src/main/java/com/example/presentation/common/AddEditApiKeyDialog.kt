package com.example.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ApiKey

/**
 * Dialogue d'ajout ou d'édition d'une clé API YouTube.
 */
@Composable
fun AddEditApiKeyDialog(
    initialKey: ApiKey? = null,
    onDismiss: () -> Unit,
    onConfirm: (id: Long, name: String, key: String, isDefault: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialKey?.name ?: "") }
    var key by remember { mutableStateOf(initialKey?.apiKey ?: "") }
    var isDefault by remember { mutableStateOf(initialKey?.isDefault ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialKey != null

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (isEditing) "Modifier la clé API" else "Nouvelle Clé API YouTube",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nom de la clé *") },
                    placeholder = { Text("ex: Clé Perso, Clé Dev") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_key_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        key = it
                        errorMessage = null
                    },
                    label = { Text("Clé API YouTube Data v3 *") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_api_key")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        modifier = Modifier.testTag("checkbox_default_key")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Définir comme clé par défaut",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Le nom de la clé est obligatoire."
                        return@Button
                    }
                    if (key.isBlank()) {
                        errorMessage = "La clé API ne peut pas être vide."
                        return@Button
                    }
                    onConfirm(initialKey?.id ?: 0L, name.trim(), key.trim(), isDefault)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("btn_save_key")
            ) {
                Text(if (isEditing) "Mettre à jour" else "Enregistrer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_cancel_key_dialog")
            ) {
                Text("Annuler")
            }
        }
    )
}

