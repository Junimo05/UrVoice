package com.example.urvoices.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.urvoices.data.Converters
import com.example.urvoices.data.db.Dao.PostDao
import com.example.urvoices.data.db.Dao.UserLoginDAO
import com.example.urvoices.data.db.Entity.PostEntity
import com.example.urvoices.data.db.Entity.UserLogin

@Database(
    entities = [UserLogin::class, PostEntity::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [

    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserLoginDAO

    abstract fun postDao(): PostDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "user_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    return instance
                }
        }
    }
}