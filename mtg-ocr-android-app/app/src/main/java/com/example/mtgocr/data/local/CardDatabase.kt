package com.example.mtgocr.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mtgocr.data.local.entity.CardEntity

@Database(entities = [CardEntity::class], version = 1, exportSchema = false)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var instance: CardDatabase? = null

        fun getInstance(context: Context): CardDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context): CardDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CardDatabase::class.java,
                "card_database"
            ).build()
    }
}