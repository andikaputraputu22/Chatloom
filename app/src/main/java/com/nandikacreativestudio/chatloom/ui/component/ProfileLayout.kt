package com.nandikacreativestudio.chatloom.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import com.nandikacreativestudio.chatloom.R

@Composable
fun ProfileLayout(
    userData: FirebaseUser?,
    onLogoutClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userData?.photoUrl,
            contentDescription = "Photo Profile",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.default_photo_profile),
            error = painterResource(id = R.drawable.default_photo_profile),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = userData?.displayName ?: "User Chatloom",
            color = colors.onSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onLogoutClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ExitToApp,
                contentDescription = "Sign Out",
                tint = colors.onSurface
            )
        }
    }
}