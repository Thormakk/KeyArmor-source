package com.thormakk.keyarmor.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thormakk.keyarmor.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAll(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity)

    @Query("UPDATE tags SET name = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) > 0 FROM tags WHERE LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun existsByName(name: String, excludeId: String = ""): Boolean

    @Query("SELECT COUNT(*) > 0 FROM tags WHERE defaultKey = :key AND id != :excludeId")
    suspend fun existsByDefaultKey(key: String, excludeId: String = ""): Boolean

    @Query("UPDATE tags SET defaultKey = :key WHERE id = :id")
    suspend fun setDefaultKey(id: String, key: String)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tags: List<TagEntity>)
}
