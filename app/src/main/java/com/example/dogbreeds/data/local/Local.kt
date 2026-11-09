package com.example.dogbreeds.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "breeds")
data class BreedEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val temperament: String,
    val origin: String?,
    val lifeSpan: String,
    val description: String?,
    val imageUrl: String,
    val isFavorite: Boolean
)

@Dao
interface BreedDao {

    @Query("SELECT * FROM breeds ORDER BY name")
    fun observeAll(): Flow<List<BreedEntity>>

    @Query("SELECT * FROM breeds WHERE isFavorite = 1 ORDER BY name")
    fun observeFavorites(): Flow<List<BreedEntity>>

    @Query("SELECT * FROM breeds WHERE id = :id")
    fun observeById(id: Int): Flow<BreedEntity?>

    @Query("SELECT id FROM breeds WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Query("SELECT COUNT(*) FROM breeds")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(breeds: List<BreedEntity>)

    @Query("UPDATE breeds SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Int)
}

@Database(entities = [BreedEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun breedDao(): BreedDao
}

/** Источник данных из локальной БД. */
class LocalDataSource(private val dao: BreedDao) {
    val breeds: Flow<List<BreedEntity>> = dao.observeAll()
    val favorites: Flow<List<BreedEntity>> = dao.observeFavorites()

    fun observeBreed(id: Int): Flow<BreedEntity?> = dao.observeById(id)
    suspend fun getFavoriteIds(): List<Int> = dao.getFavoriteIds()
    suspend fun count(): Int = dao.count()
    suspend fun upsertAll(breeds: List<BreedEntity>) = dao.upsertAll(breeds)
    suspend fun toggleFavorite(id: Int) = dao.toggleFavorite(id)
}
