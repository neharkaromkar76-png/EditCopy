package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TemplateEntity::class], version = 1, exportSchema = false)
abstract class EditCopyDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: EditCopyDatabase? = null

        fun getDatabase(context: Context): EditCopyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EditCopyDatabase::class.java,
                    "editcopy_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
