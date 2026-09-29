package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.TechnicianStatus
import com.example.ui.theme.*

@Composable
fun TechnicianStatusChip(status: TechnicianStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, dotColor) = when (status) {
        TechnicianStatus.AVAILABLE -> Triple(StatusSuccessContainer, StatusSuccess, StatusSuccess)
        TechnicianStatus.ON_JOB -> Triple(StatusInfoContainer, StatusInfo, StatusInfo)
        TechnicianStatus.EN_ROUTE -> Triple(WarmAmberContainer, WarmAmberOnContainer, WarmAmberTertiary)
        TechnicianStatus.OFF_DUTY -> Triple(LightSurfaceVariant, Color.Gray, Color.Gray)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun JobStatusChip(status: JobStatus, isEmergency: Boolean = false, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when {
        isEmergency -> EmergencyRedContainer to EmergencyRed
        status == JobStatus.SCHEDULED -> IceBluePrimaryContainer to IceBlueOnPrimaryContainer
        status == JobStatus.EN_ROUTE -> WarmAmberContainer to WarmAmberOnContainer
        status == JobStatus.IN_PROGRESS -> CoolTealContainer to CoolTealOnContainer
        status == JobStatus.COMPLETED -> StatusSuccessContainer to StatusSuccess
        else -> LightSurfaceVariant to Color.DarkGray
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isEmergency) "EMERGENCY: ${status.label}" else status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PaymentStatusChip(status: PaymentStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        PaymentStatus.PENDING -> StatusWarningContainer to StatusWarning
        PaymentStatus.PAID_UPI -> StatusSuccessContainer to StatusSuccess
        PaymentStatus.PAID_STRIPE -> CoolTealContainer to CoolTealOnContainer
        PaymentStatus.PAID_CASH -> IceBluePrimaryContainer to IceBlueOnPrimaryContainer
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RatingStars(rating: Float, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            if (i <= rating.toInt()) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = WarmAmberTertiary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
