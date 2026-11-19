package com.oraculoloto5x.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.oraculoloto5x.app.ai.GeminiOracleAdvisor
import com.oraculoloto5x.app.data.models.UserProfile
import com.oraculoloto5x.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ProfileSettingsDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSaveProfile: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf(currentProfile.fullName) }
    var commonName by remember { mutableStateOf(currentProfile.commonName) }
    var birthDateStr by remember { mutableStateOf(currentProfile.birthDate.toString()) }
    var latStr by remember { mutableStateOf(currentProfile.latitude.toString()) }
    var lonStr by remember { mutableStateOf(currentProfile.longitude.toString()) }
    var tz by remember { mutableStateOf(currentProfile.timeZone) }
    var notificationsEnabled by remember { mutableStateOf(currentProfile.notificationsEnabled) }

    // Gemini API settings
    var apiKey by remember { mutableStateOf(GeminiOracleAdvisor.getApiKey(context)) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(GeminiOracleAdvisor.getSelectedModel(context)) }
    var isTestingKey by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, GoldPrimary, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "PERFIL DE USUARIO Y COORDENADAS",
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Afinidad astronómica y numerológica personalizada",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre Completo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = GoldLight,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commonName,
                    onValueChange = { commonName = it },
                    label = { Text("Nombre de Uso Diario") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = GoldLight,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthDateStr,
                    onValueChange = { birthDateStr = it },
                    label = { Text("Fecha de Nacimiento (AAAA-MM-DD)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = GoldLight,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latStr,
                        onValueChange = { latStr = it },
                        label = { Text("Latitud") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                            focusedLabelColor = GoldLight,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lonStr,
                        onValueChange = { lonStr = it },
                        label = { Text("Longitud") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                            focusedLabelColor = GoldLight,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tz,
                    onValueChange = { tz = it },
                    label = { Text("Zona Horaria") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = GoldLight,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Switch de Notificaciones Offline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alertas Planetarias Locales",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aviso 10 min antes de Horas de Sol/Luna y cierre de lotería (AlarmManager offline)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldLight,
                            checkedTrackColor = GoldDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sección Google Gemini AI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141C2B))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "GOOGLE GEMINI AI (ORÁCULO 5X)",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Sintetiza la sabiduría de los 5 tratados con tu perfil natal y horas planetarias. API Key precargada y editable:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = {
                                apiKey = it
                                testResult = null
                            },
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("AQ.Ab8...", color = TextMuted) },
                            singleLine = true,
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Alternar visibilidad",
                                        tint = GoldPrimary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Botón Probar API Key
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (apiKey.isNotBlank() && !isTestingKey) {
                                        isTestingKey = true
                                        testResult = null
                                        scope.launch {
                                            val res = GeminiOracleAdvisor.testApiKey(apiKey.trim(), selectedModel)
                                            res.fold(
                                                onSuccess = { testResult = Pair(true, it) },
                                                onFailure = { testResult = Pair(false, it.message ?: "Error al conectar") }
                                            )
                                            isTestingKey = false
                                        }
                                    }
                                },
                                enabled = apiKey.isNotBlank() && !isTestingKey,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF263345),
                                    contentColor = GoldLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isTestingKey) {
                                    CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Probando...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Probar API Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            testResult?.let { (success, message) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (success) EmeraldAccent else SemaphoreRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (success) "✓ Conexión OK" else "Error",
                                        color = if (success) EmeraldLight else SemaphoreRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        testResult?.takeIf { !it.first }?.let { (_, errorMsg) ->
                            Text(
                                text = errorMsg,
                                color = SemaphoreRed,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }

                        Text(
                            text = "Modelo a Utilizar:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedModel == GeminiOracleAdvisor.MODEL_FLASH,
                                onClick = { selectedModel = GeminiOracleAdvisor.MODEL_FLASH },
                                label = { Text("2.5-flash", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DarkBackground,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedModel == GeminiOracleAdvisor.MODEL_STABLE,
                                onClick = { selectedModel = GeminiOracleAdvisor.MODEL_STABLE },
                                label = { Text("1.5-flash (Estable)", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DarkBackground,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.weight(1.3f)
                            )
                            FilterChip(
                                selected = selectedModel == GeminiOracleAdvisor.MODEL_PRO,
                                onClick = { selectedModel = GeminiOracleAdvisor.MODEL_PRO },
                                label = { Text("2.5-pro", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DarkBackground,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val parsedDate = try {
                                LocalDate.parse(birthDateStr.trim())
                            } catch (e: Exception) {
                                currentProfile.birthDate
                            }
                            val parsedLat = latStr.toDoubleOrNull() ?: currentProfile.latitude
                            val parsedLon = lonStr.toDoubleOrNull() ?: currentProfile.longitude

                            // Save Gemini settings
                            GeminiOracleAdvisor.saveApiKey(context, apiKey)
                            GeminiOracleAdvisor.saveSelectedModel(context, selectedModel)

                            onSaveProfile(
                                currentProfile.copy(
                                    fullName = fullName.trim(),
                                    commonName = commonName.trim(),
                                    birthDate = parsedDate,
                                    latitude = parsedLat,
                                    longitude = parsedLon,
                                    timeZone = tz.trim(),
                                    notificationsEnabled = notificationsEnabled
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = DarkBackground
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
