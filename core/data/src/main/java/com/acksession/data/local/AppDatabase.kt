package com.acksession.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.acksession.data.local.dao.UserDao
import com.acksession.data.local.entity.UserEntity

/**
 * Room database for the application.
 *
 * This is the main database class that defines the entities and provides
 * access to the DAOs.
 *
 * Version: 1
 * Entities: UserEntity
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to UserDao for user-related database operations
     */
    abstract fun userDao(): UserDao
}

