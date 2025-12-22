package com.example.heaveyequpments.di

import android.content.Context
import com.example.heaveyequpments.data.local.AppDatabase
import com.example.heaveyequpments.data.local.dao.HeavyEquipentsDao
import com.example.heaveyequpments.data.local.dao.MaintenanceLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {

        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideHeavyEquipmentsDao(database: AppDatabase): HeavyEquipentsDao {
        return database.heavyEquipmentsDao()
    }

    @Provides
    fun provideMaintenanceDao(database: AppDatabase): MaintenanceLogDao {
        return database.maintenanceDao()
    }
}