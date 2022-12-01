package com.example.mobilesoftware.view.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ImageEntity::class,TripEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun imageDao(): ImageDao

    abstract fun tripDao(): TripDao

    companion object{
        private val DATABASE_NAME = "SoftwareAssignment"
        // For Singleton instantiation
        @Volatile private var db_instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return db_instance ?: synchronized(this) {
                db_instance ?: buildDatabase(context).also { db_instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    // Wipes and rebuilds instead of migrating if no Migration object specified.
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}