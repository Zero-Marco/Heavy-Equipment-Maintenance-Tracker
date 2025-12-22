package com.example.heaveyequpments.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.heaveyequpments.data.Converters
import com.example.heaveyequpments.data.local.dao.HeavyEquipentsDao
import com.example.heaveyequpments.data.model.maintenance.InvoiceImage
import com.example.heaveyequpments.data.model.maintenance.Maintenance
import com.example.heaveyequpments.data.local.dao.MaintenanceLogDao
import com.example.heaveyequpments.data.model.maintenance.Mechanic
import com.example.heaveyequpments.data.model.maintenance.OtherExpense
import com.example.heaveyequpments.data.model.maintenance.PartsChanged
import com.example.heaveyequpments.data.model.maintenance.PartsChangedImage
import com.example.heaveyequpments.data.model.equipment.HeavyEquipments

@Database(
    entities = [
        HeavyEquipments::class,
        Maintenance::class,
        Mechanic::class,
        InvoiceImage::class,
        OtherExpense::class,
        PartsChanged::class,
        PartsChangedImage::class

               ], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun heavyEquipmentsDao(): HeavyEquipentsDao

    abstract fun maintenanceDao(): MaintenanceLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user_database2"
                ).build()
                INSTANCE = instance
                return instance
            }


        }
    }
}