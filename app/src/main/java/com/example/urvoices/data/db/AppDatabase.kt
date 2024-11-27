package com.example.urvoices.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.urvoices.data.Converters
import com.example.urvoices.data.db.Dao.BlockedUserDao
import com.example.urvoices.data.db.Dao.NotificationDao
import com.example.urvoices.data.db.Dao.SavedPostDao
import com.example.urvoices.data.db.Entity.BlockedUser
import com.example.urvoices.data.db.Entity.Notification
import com.example.urvoices.data.db.Entity.SavedPost

@Database(
    entities = [BlockedUser::class, SavedPost::class, Notification::class],
    version = 6,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedUserDao(): BlockedUserDao
    abstract fun savedPostDao(): SavedPostDao
    abstract fun notificationDao(): NotificationDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    return instance
                }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE blocked_users ADD COLUMN username TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE blocked_users ADD COLUMN avatarUrl TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS saved_posts (id TEXT PRIMARY KEY NOT NULL, username TEXT NOT NULL, avatarUrl TEXT NOT NULL, audioName TEXT NOT NULL, audioUrl TEXT NOT NULL)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE saved_posts ADD COLUMN userID TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE saved_posts ADD COLUMN imgUrl TEXT NOT NULL DEFAULT ''")
    }
}

//create Notification Entity
//@Entity(tableName = "notifications")
//data class Notification(
//    @PrimaryKey val id: String = "",
//    val title: String = "",
//    val message: String = "",
//    val createdAt: Long = System.currentTimeMillis(),
//    val url: String = "",
//    val isRead: Boolean = false
//)

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create Entity with correct column types
        db.execSQL("CREATE TABLE IF NOT EXISTS notifications (id TEXT PRIMARY KEY NOT NULL, type TEXT NOT NULL, message TEXT NOT NULL, isRead INTEGER NOT NULL, url TEXT NOT NULL, createdAt INTEGER NOT NULL)")
    }
}
