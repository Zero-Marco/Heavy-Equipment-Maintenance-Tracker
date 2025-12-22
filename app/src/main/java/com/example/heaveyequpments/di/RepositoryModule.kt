package com.example.heaveyequpments.di

import com.example.heaveyequpments.data.repository.HeavyEquipmentsRepository
import com.example.heaveyequpments.data.repository.HeavyEquipmentsRepositoryImpl
import com.example.heaveyequpments.data.repository.MaintenanceLogRepository
import com.example.heaveyequpments.data.repository.MaintenanceLogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHeavyEquipmentsRepository(
        impl: HeavyEquipmentsRepositoryImpl
    ): HeavyEquipmentsRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceLogRepository(
        impl: MaintenanceLogRepositoryImpl
    ): MaintenanceLogRepository

}