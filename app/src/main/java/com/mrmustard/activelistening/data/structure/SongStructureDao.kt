package com.mrmustard.activelistening.data.structure

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SongStructureDao {

    @Query(
        """
        SELECT * FROM song_structure_sections
        WHERE song_key = :songKey
        ORDER BY version, position
        """,
    )
    suspend fun getSections(songKey: String): List<SongStructureSectionEntity>

    @Query("DELETE FROM song_structure_sections WHERE song_key = :songKey")
    suspend fun deleteSections(songKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SongStructureSectionEntity>)

    @Transaction
    suspend fun replaceStructure(
        songKey: String,
        sections: List<SongStructureSectionEntity>,
    ) {
        deleteSections(songKey)
        insertSections(sections)
    }
}
