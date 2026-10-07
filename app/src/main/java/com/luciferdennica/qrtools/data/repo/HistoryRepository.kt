package com.luciferdennica.qrtools.data.repo

import com.luciferdennica.qrtools.data.db.ScanDao
import com.luciferdennica.qrtools.data.db.ScanEntity
import com.luciferdennica.qrtools.domain.model.ScanType
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: ScanDao) {

    fun getAll(): Flow<List<ScanEntity>> = dao.getAll()
    suspend fun getAllOnce(): List<ScanEntity> = dao.getAllOnce()
    fun getFavorites(): Flow<List<ScanEntity>> = dao.getFavorites()
    fun search(query: String): Flow<List<ScanEntity>> = dao.search(query)
    fun getByType(type: ScanType): Flow<List<ScanEntity>> = dao.getByType(type.name)
    fun searchByType(type: ScanType, query: String): Flow<List<ScanEntity>> =
        dao.searchByType(type.name, query)
    suspend fun getById(id: Long): ScanEntity? = dao.getById(id)

    /** Обычное добавление (с флагом сохранения в историю). */
    suspend fun add(
        content: String,
        format: String,
        type: ScanType,
        saveToHistory: Boolean = true
    ): Long {
        return dao.insert(
            ScanEntity(
                content = content,
                format = format,
                type = type.name,
                isHidden = !saveToHistory
            )
        )
    }

    /** Импорт записи из CSV с сохранением исходных даты и избранного. */
    suspend fun importScan(
        content: String,
        format: String,
        type: ScanType,
        timestamp: Long,
        isFavorite: Boolean
    ): Long {
        return dao.insert(
            ScanEntity(
                content = content,
                format = format,
                type = type.name,
                isFavorite = isFavorite,
                timestamp = timestamp
            )
        )
    }

    suspend fun toggleFavorite(item: ScanEntity) {
        dao.setFavorite(item.id, !item.isFavorite)
    }

    suspend fun setFavorite(id: Long, fav: Boolean) {
        dao.setFavorite(id, fav)
    }

    suspend fun togglePinned(item: ScanEntity) {
        dao.setPinned(item.id, !item.isPinned)
    }

    suspend fun setPinned(id: Long, pinned: Boolean) {
        dao.setPinned(id, pinned)
    }

    suspend fun setNote(id: Long, note: String) {
        dao.setNote(id, note)
    }

    suspend fun delete(item: ScanEntity) = dao.delete(item)

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun clearHidden() = dao.clearHidden()

    suspend fun clearAll() = dao.clearAll()
}
