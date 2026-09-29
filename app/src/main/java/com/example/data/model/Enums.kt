package com.example.data.model

enum class TechnicianStatus(val label: String) {
    AVAILABLE("Available"),
    EN_ROUTE("En Route"),
    ON_JOB("On Job"),
    OFF_DUTY("Off Duty")
}

enum class JobStatus(val label: String) {
    SCHEDULED("Scheduled"),
    EN_ROUTE("Technician En Route"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class PaymentStatus(val label: String) {
    PENDING("Payment Pending"),
    PAID_UPI("Paid via BHIM/UPI"),
    PAID_STRIPE("Paid via Card/Stripe"),
    PAID_CASH("Paid in Cash")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    HINDI("hi", "Hindi", "हिंदी")
}

enum class UserRole {
    ADMIN_DISPATCHER,
    CUSTOMER
}
