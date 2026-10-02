package com.luciferdennica.qrtools.ui.screens.generator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.qr.QrGenerator
import com.luciferdennica.qrtools.qr.QrTypeBuilders
import com.luciferdennica.qrtools.util.ClipboardUtils
import com.luciferdennica.qrtools.util.GalleryUtils
import com.luciferdennica.qrtools.util.IntentUtils
import android.widget.Toast

private enum class GenType(val labelRes: Int) {
    TEXT(R.string.gen_text),
    URL(R.string.gen_url),
    WIFI(R.string.gen_wifi),
    CONTACT(R.string.gen_contact),
    SMS(R.string.gen_sms)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(nav: NavController) {
    val context = LocalContext.current

    var type by remember { mutableStateOf(GenType.TEXT) }
    var expanded by remember { mutableStateOf(false) }

    // Поля
    var textField by remember { mutableStateOf("") }
    var urlField by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPass by remember { mutableStateOf("") }
    var wifiSec by remember { mutableStateOf("WPA") }
    var wifiSecExpanded by remember { mutableStateOf(false) }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var smsPhone by remember { mutableStateOf("") }
    var smsMessage by remember { mutableStateOf("") }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrContent by remember { mutableStateOf("") }

    // Разрешение на запись (Android 9-)
    val storagePermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            qrBitmap?.let {
                val ok = GalleryUtils.saveToGallery(context, it, "qrtools_${System.currentTimeMillis()}")
                Toast.makeText(
                    context,
                    context.getString(if (ok) R.string.saved_gallery else R.string.save_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
        }
    }

    fun saveCurrentToGallery() {
        val bmp = qrBitmap ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val ok = GalleryUtils.saveToGallery(context, bmp, "qrtools_${System.currentTimeMillis()}")
            Toast.makeText(
                context,
                context.getString(if (ok) R.string.saved_gallery else R.string.save_failed),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.generator)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Выбор типа
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = stringResource(type.labelRes),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.gen_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GenType.values().forEach { t ->
                        DropdownMenuItem(
                            text = { Text(stringResource(t.labelRes)) },
                            onClick = {
                                type = t
                                expanded = false
                                qrBitmap = null
                                qrContent = ""
                            }
                        )
                    }
                }
            }

            // Поля по типу
            when (type) {
                GenType.TEXT -> {
                    OutlinedTextField(
                        value = textField, onValueChange = { textField = it },
                        label = { Text(stringResource(R.string.field_text)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                GenType.URL -> {
                    OutlinedTextField(
                        value = urlField, onValueChange = { urlField = it },
                        label = { Text(stringResource(R.string.field_url)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                GenType.WIFI -> {
                    OutlinedTextField(
                        value = wifiSsid, onValueChange = { wifiSsid = it },
                        label = { Text(stringResource(R.string.field_wifi_ssid)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wifiPass, onValueChange = { wifiPass = it },
                        label = { Text(stringResource(R.string.field_wifi_password)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = wifiSecExpanded,
                        onExpandedChange = { wifiSecExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = when (wifiSec) {
                                "WPA" -> stringResource(R.string.wifi_wpa)
                                "WEP" -> stringResource(R.string.wifi_wep)
                                else -> stringResource(R.string.wifi_none)
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.field_wifi_security)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wifiSecExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = wifiSecExpanded,
                            onDismissRequest = { wifiSecExpanded = false }
                        ) {
                            listOf("WPA" to R.string.wifi_wpa, "WEP" to R.string.wifi_wep, "nopass" to R.string.wifi_none)
                                .forEach { (val_, labelRes) ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(labelRes)) },
                                        onClick = { wifiSec = val_; wifiSecExpanded = false }
                                    )
                                }
                        }
                    }
                }
                GenType.CONTACT -> {
                    OutlinedTextField(
                        value = contactName, onValueChange = { contactName = it },
                        label = { Text(stringResource(R.string.field_contact_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPhone, onValueChange = { contactPhone = it },
                        label = { Text(stringResource(R.string.field_contact_phone)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactEmail, onValueChange = { contactEmail = it },
                        label = { Text(stringResource(R.string.field_contact_email)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                GenType.SMS -> {
                    OutlinedTextField(
                        value = smsPhone, onValueChange = { smsPhone = it },
                        label = { Text(stringResource(R.string.field_sms_phone)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = smsMessage, onValueChange = { smsMessage = it },
                        label = { Text(stringResource(R.string.field_sms_message)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // Кнопка "Сгенерировать"
            Button(
                onClick = {
                    val content = when (type) {
                        GenType.TEXT -> QrTypeBuilders.text(textField)
                        GenType.URL -> if (urlField.isBlank()) "" else QrTypeBuilders.url(urlField)
                        GenType.WIFI -> if (wifiSsid.isBlank()) "" else QrTypeBuilders.wifi(wifiSsid, wifiPass, wifiSec)
                        GenType.CONTACT -> if (contactName.isBlank()) "" else QrTypeBuilders.contact(contactName, contactPhone, contactEmail)
                        GenType.SMS -> if (smsPhone.isBlank()) "" else QrTypeBuilders.sms(smsPhone, smsMessage)
                    }
                    if (content.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.fill_fields), Toast.LENGTH_SHORT).show()
                    } else {
                        qrContent = content
                        qrBitmap = QrGenerator.generate(content)
                        if (qrBitmap == null) {
                            Toast.makeText(context, context.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_generate))
            }

            // Превью QR
            qrBitmap?.let { bmp ->
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.generated_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(260.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { saveCurrentToGallery() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.btn_save_gallery))
                    }
                    OutlinedButton(
                        onClick = { IntentUtils.shareText(context, qrContent) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.btn_share_qr))
                    }
                }

                OutlinedButton(
                    onClick = { ClipboardUtils.copy(context, qrContent) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.action_copy))
                }
            }
        }
    }
}
