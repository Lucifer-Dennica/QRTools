package com.luciferdennica.qrtools.ui.screens.generator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.luciferdennica.qrtools.R
import com.luciferdennica.qrtools.qr.DotShape
import com.luciferdennica.qrtools.qr.QrGenerator
import com.luciferdennica.qrtools.qr.QrStyle
import com.luciferdennica.qrtools.qr.QrTypeBuilders
import com.luciferdennica.qrtools.util.ClipboardUtils
import com.luciferdennica.qrtools.util.GalleryUtils
import com.luciferdennica.qrtools.util.IntentUtils

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
    var showStyling by remember { mutableStateOf(false) }
    var style by remember { mutableStateOf(QrStyle()) }

    // Логотип
    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            loadBitmap(context, uri)?.let { bmp ->
                style = style.copy(logo = bmp)
                if (qrContent.isNotBlank()) qrBitmap = QrGenerator.generate(qrContent, style = style)
            }
        }
    }

    // Выбор контакта через StartActivityForResult (надёжнее, чем PickContact)
    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                readContact(context, uri)?.let { (name, phone, email) ->
                    contactName = name
                    contactPhone = phone
                    contactEmail = email
                    if (name.isBlank() && phone.isBlank() && email.isBlank()) {
                        Toast.makeText(context, "Не удалось прочитать контакт", Toast.LENGTH_SHORT).show()
                    }
                } ?: Toast.makeText(context, "Не удалось прочитать контакт", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Разрешение на контакты
    val contactsPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openContactPicker(context, contactLauncher)
        } else {
            Toast.makeText(context, "Без разрешения нельзя выбрать контакт", Toast.LENGTH_SHORT).show()
        }
    }

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

    fun regenerateIfPossible() {
        if (qrContent.isNotBlank()) {
            qrBitmap = QrGenerator.generate(qrContent, style = style)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.generator)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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

            when (type) {
                GenType.TEXT -> OutlinedTextField(
                    value = textField, onValueChange = { textField = it },
                    label = { Text(stringResource(R.string.field_text)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                GenType.URL -> OutlinedTextField(
                    value = urlField, onValueChange = { urlField = it },
                    label = { Text(stringResource(R.string.field_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = wifiSecExpanded,
                            onDismissRequest = { wifiSecExpanded = false }
                        ) {
                            listOf(
                                "WPA" to R.string.wifi_wpa,
                                "WEP" to R.string.wifi_wep,
                                "nopass" to R.string.wifi_none
                            ).forEach { (value, labelRes) ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(labelRes)) },
                                    onClick = {
                                        wifiSec = value
                                        wifiSecExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                GenType.CONTACT -> {
                    OutlinedButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.READ_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                openContactPicker(context, contactLauncher)
                            } else {
                                contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.qr_pick_contact))
                    }

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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStyling = !showStyling },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.qr_styling),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (showStyling) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (showStyling) {
                StylingSection(
                    style = style,
                    onStyleChange = { newStyle ->
                        style = newStyle
                        regenerateIfPossible()
                    },
                    onLogoUpload = { logoLauncher.launch("image/*") }
                )
            }

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
                        qrBitmap = QrGenerator.generate(content, style = style)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_generate))
            }

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
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(280.dp)
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
                        onClick = {
                            IntentUtils.shareImage(context, bmp, "qrtools_${System.currentTimeMillis()}.png")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(R.string.btn_share_qr))
                    }
                }

                OutlinedButton(
                    onClick = {
                        val ok = ClipboardUtils.copyImage(context, bmp)
                        if (ok) {
                            Toast.makeText(context, "QR скопирован", Toast.LENGTH_SHORT).show()
                        } else {
                            ClipboardUtils.copy(context, qrContent)
                        }
                    },
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

// ========================================================
// СТИЛИЗАЦИЯ
// ========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StylingSection(
    style: QrStyle,
    onStyleChange: (QrStyle) -> Unit,
    onLogoUpload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.qr_presets),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QrStyle.PRESETS.forEach { (name, preset) ->
                    PresetChip(
                        name = name,
                        preset = preset,
                        selected = style == preset,
                        onClick = { onStyleChange(preset) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.qr_dot_color),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            ColorPaletteRow(
                palette = QrStyle.DOT_PALETTE,
                selectedColor = style.dotColor,
                onSelect = { main, partner ->
                    onStyleChange(style.copy(dotColor = main, dotColor2 = partner))
                }
            )

            Text(
                text = stringResource(R.string.qr_bg_color),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            ColorPaletteRow(
                palette = QrStyle.BG_PALETTE,
                selectedColor = style.bgColor,
                onSelect = { main, partner ->
                    onStyleChange(style.copy(bgColor = main, bgColor2 = partner))
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.qr_gradient_dots),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = style.dotGradient,
                    onCheckedChange = { onStyleChange(style.copy(dotGradient = it)) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.qr_gradient_bg),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = style.bgGradient,
                    onCheckedChange = { onStyleChange(style.copy(bgGradient = it)) }
                )
            }

            Text(
                text = stringResource(R.string.qr_dot_shape),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotShape.values().forEach { shape ->
                    FilterChip(
                        selected = style.dotShape == shape,
                        onClick = { onStyleChange(style.copy(dotShape = shape)) },
                        label = { Text(stringResource(shape.titleRes)) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.qr_logo),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onLogoUpload,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.qr_logo_upload))
                }
                if (style.logo != null) {
                    TextButton(onClick = { onStyleChange(style.copy(logo = null)) }) {
                        Text(stringResource(R.string.qr_logo_remove))
                    }
                }
            }

            style.logo?.let { logo ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = logo.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    name: String,
    preset: QrStyle,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (preset.bgGradient)
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(preset.bgColor, preset.bgColor2))
                    else
                        androidx.compose.ui.graphics.SolidColor(preset.bgColor)
                )
                .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (preset.dotGradient)
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(preset.dotColor, preset.dotColor2))
                        else
                            androidx.compose.ui.graphics.SolidColor(preset.dotColor)
                    )
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ColorPaletteRow(
    palette: List<Pair<Color, Color>>,
    selectedColor: Color,
    onSelect: (Color, Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palette.forEach { (main, partner) ->
            val isSelected = main.toArgb() == selectedColor.toArgb()
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(main)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onSelect(main, partner) }
            )
        }
    }
}

// ========================================================
// ХЕЛПЕРЫ
// ========================================================

private fun openContactPicker(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
    launcher.launch(intent)
}

private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = false
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()
}

/** Читает контакт по URI: возвращает Triple(имя, телефон, email). */
private fun readContact(context: Context, uri: Uri): Triple<String, String, String>? {
    return runCatching {
        var name = ""
        var phone = ""
        var email = ""
        var contactId: String? = null

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: ""

                val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                if (idIdx >= 0) contactId = cursor.getString(idIdx)
            }
        }

        if (contactId != null) {
            // Телефон
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(contactId),
                null
            )?.use { phoneCursor ->
                if (phoneCursor.moveToFirst()) {
                    val idx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (idx >= 0) phone = phoneCursor.getString(idx) ?: ""
                }
            }
            // Email
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                arrayOf(contactId),
                null
            )?.use { emailCursor ->
                if (emailCursor.moveToFirst()) {
                    val idx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                    if (idx >= 0) email = emailCursor.getString(idx) ?: ""
                }
            }
        }

        Triple(name, phone, email)
    }.getOrNull()
}
