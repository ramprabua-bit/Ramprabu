package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TechnicianDao {
    @Query("SELECT * FROM technicians ORDER BY rating DESC")
    fun getAllTechnicians(): Flow<List<Technician>>

    @Query("SELECT * FROM technicians WHERE id = :id LIMIT 1")
    suspend fun getTechnicianById(id: Long): Technician?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnicians(technicians: List<Technician>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: Technician): Long

    @Update
    suspend fun updateTechnician(technician: Technician)

    @Query("UPDATE technicians SET status = :status WHERE id = :id")
    suspend fun updateTechnicianStatus(id: Long, status: TechnicianStatus)

    @Query("UPDATE technicians SET currentArea = :area WHERE id = :id")
    suspend fun updateTechnicianArea(id: Long, area: String)

    @Query("SELECT COUNT(*) FROM technicians")
    suspend fun count(): Int
}

@Dao
interface ServiceJobDao {
    @Query("SELECT * FROM service_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<ServiceJob>>

    @Query("SELECT * FROM service_jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getJobsByStatus(status: JobStatus): Flow<List<ServiceJob>>

    @Query("SELECT * FROM service_jobs WHERE customerPhone = :phone ORDER BY createdAt DESC")
    fun getJobsByCustomerPhone(phone: String): Flow<List<ServiceJob>>

    @Query("SELECT * FROM service_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): ServiceJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: ServiceJob): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<ServiceJob>)

    @Update
    suspend fun updateJob(job: ServiceJob)

    @Query("UPDATE service_jobs SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateJobStatus(id: Long, status: JobStatus, completedAt: Long? = null)

    @Query("UPDATE service_jobs SET assignedTechnicianId = :techId, assignedTechnicianName = :techName, status = :status WHERE id = :jobId")
    suspend fun assignTechnician(jobId: Long, techId: Long?, techName: String?, status: JobStatus)

    @Delete
    suspend fun deleteJob(job: ServiceJob)

    @Query("SELECT COUNT(*) FROM service_jobs")
    suspend fun count(): Int
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE customerPhone = :phone ORDER BY id DESC")
    fun getInvoicesByCustomerPhone(phone: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE jobId = :jobId LIMIT 1")
    suspend fun getInvoiceByJobId(jobId: Long): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<Invoice>)

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET paymentStatus = :status, upiTransactionRef = :ref, paidAt = :paidAt WHERE id = :id")
    suspend fun markInvoicePaid(id: Long, status: PaymentStatus, ref: String?, paidAt: Long)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int
}

@Dao
interface CustomerReviewDao {
    @Query("SELECT * FROM customer_reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<CustomerReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: CustomerReview): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<CustomerReview>)

    @Query("SELECT COUNT(*) FROM customer_reviews")
    suspend fun count(): Int
}

@Dao
interface ServiceInquiryDao {
    @Query("SELECT * FROM service_inquiries ORDER BY timestamp DESC")
    fun getAllInquiries(): Flow<List<ServiceInquiry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: ServiceInquiry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiries(inquiries: List<ServiceInquiry>)

    @Update
    suspend fun updateInquiry(inquiry: ServiceInquiry)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotification>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM app_notifications")
    suspend fun clearAll()
}
