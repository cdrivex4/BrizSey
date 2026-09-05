package sc.meteo.seymeteo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sc.meteo.seymeteo.data.db.dao.AlertDao
import sc.meteo.seymeteo.data.db.dao.FavouriteDao
import sc.meteo.seymeteo.data.db.dao.ForecastDao
import sc.meteo.seymeteo.data.db.entity.CachedAlertEntity
import sc.meteo.seymeteo.data.db.entity.CachedForecastEntity
import sc.meteo.seymeteo.data.db.entity.FavouriteLocationEntity

@Database(
    entities = [
        CachedForecastEntity::class,
        CachedAlertEntity::class,
        FavouriteLocationEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class SeyMeteoDatabase : RoomDatabase() {

    abstract fun forecastDao(): ForecastDao
    abstract fun alertDao(): AlertDao
    abstract fun favouriteDao(): FavouriteDao

    companion object {
        @Volatile
        private var INSTANCE: SeyMeteoDatabase? = null

        fun getInstance(context: Context): SeyMeteoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SeyMeteoDatabase::class.java,
                    "seymeteo.db"
                )
                    .fallbackToDestructiveMigration()  // safe for v1 — swap for migrations in production
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
