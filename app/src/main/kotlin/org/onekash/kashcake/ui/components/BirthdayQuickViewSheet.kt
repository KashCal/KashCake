package org.onekash.kashcake.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.onekash.kashcake.data.db.entity.Birthday
import org.onekash.kashcake.domain.BirthdayUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayQuickViewSheet(
    birthday: Birthday,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with photo/initial
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (birthday.photoUri != null) {
                    AsyncImage(
                        model = birthday.photoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            birthday.name.first().uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        birthday.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        BirthdayUtils.formatBirthdayDate(birthday.month, birthday.day, birthday.year),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    birthday.ageOnNextBirthday()?.let { age ->
                        Text(
                            "Turning $age",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Days until
            val daysUntil = birthday.daysUntilBirthday()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (daysUntil == 0L) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        when {
                            daysUntil == 0L -> "Birthday Today!"
                            daysUntil == 1L -> "Birthday Tomorrow!"
                            else -> "$daysUntil days until birthday"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Wish buttons - Text and Email
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { sendTextWish(context, birthday) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Text")
                }

                FilledTonalButton(
                    onClick = { sendEmailWish(context, birthday) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Email")
                }
            }
        }
    }
}

private fun getContactPhone(context: Context, lookupKey: String?): String? {
    if (lookupKey == null) return null

    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val selection = "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?"
    val selectionArgs = arrayOf(lookupKey)

    context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (phoneIndex >= 0) {
                return cursor.getString(phoneIndex)
            }
        }
    }
    return null
}

private fun getContactEmail(context: Context, lookupKey: String?): String? {
    if (lookupKey == null) return null

    val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
    val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
    val selection = "${ContactsContract.CommonDataKinds.Email.LOOKUP_KEY} = ?"
    val selectionArgs = arrayOf(lookupKey)

    context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            if (emailIndex >= 0) {
                return cursor.getString(emailIndex)
            }
        }
    }
    return null
}

private fun sendTextWish(context: Context, birthday: Birthday) {
    val firstName = birthday.name.split(" ").first()
    val message = "Happy Birthday, $firstName!"
    val phone = getContactPhone(context, birthday.contactLookupKey)

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = if (phone != null) {
            Uri.parse("sms:$phone")
        } else {
            Uri.parse("sms:")
        }
        putExtra("sms_body", message)
    }
    context.startActivity(intent)
}

private fun sendEmailWish(context: Context, birthday: Birthday) {
    val firstName = birthday.name.split(" ").first()
    val message = "Happy Birthday, $firstName! Wishing you an amazing day!"
    val email = getContactEmail(context, birthday.contactLookupKey)

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = if (email != null) {
            Uri.parse("mailto:$email")
        } else {
            Uri.parse("mailto:")
        }
        putExtra(Intent.EXTRA_SUBJECT, "Happy Birthday, $firstName!")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(intent)
}
