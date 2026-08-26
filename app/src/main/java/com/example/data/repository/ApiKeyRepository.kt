package com.example.data.repository

import com.example.data.local.dao.ApiKeyDao
import com.example.data.local.entity.ApiKeyEntity
import com.example.domain.model.ApiKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Interface du Repository pour la gestion des clés API.
 */
interface ApiKeyRepository {
    fun getAllApiKeys(): Flow<List<ApiKey>>
    suspend fun getDefaultApiKey(): ApiKey?
    fun observeDefaultApiKey(): Flow<ApiKey?>
    suspend fun insertApiKey(name: String, key: String, isDefault: Boolean): Long
    suspend fun updateApiKey(id: Long, name: String, key: String, isDefault: Boolean)
    suspend fun deleteApiKey(apiKey: ApiKey)
    suspend fun setDefaultApiKey(id: Long)
    suspend fun hasAnyKey(): Boolean
}

/**
 * Implémentation concrète du Repository des clés API utilisant Room comme source de vérité.
 */
class ApiKeyRepositoryImpl(
    private val apiKeyDao: ApiKeyDao
) : ApiKeyRepository {

    override fun getAllApiKeys(): Flow<List<ApiKey>> {
        return apiKeyDao.getAllApiKeys().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDefaultApiKey(): ApiKey? = withContext(Dispatchers.IO) {
        apiKeyDao.getDefaultApiKey()?.toDomain()
    }

    override fun observeDefaultApiKey(): Flow<ApiKey?> {
        return apiKeyDao.observeDefaultApiKey().map { it?.toDomain() }
    }

    override suspend fun insertApiKey(name: String, key: String, isDefault: Boolean): Long = withContext(Dispatchers.IO) {
        val count = apiKeyDao.getApiKeyCount()
        // Si c'est la toute première clé, on la force automatiquement par défaut
        val shouldBeDefault = isDefault || count == 0

        if (shouldBeDefault) {
            apiKeyDao.clearDefaultFlags()
        }

        val entity = ApiKeyEntity(
            name = name.trim(),
            apiKey = key.trim(),
            isDefault = shouldBeDefault,
            createdAt = System.currentTimeMillis()
        )
        apiKeyDao.insertApiKey(entity)
    }

    override suspend fun updateApiKey(id: Long, name: String, key: String, isDefault: Boolean) = withContext(Dispatchers.IO) {
        if (isDefault) {
            apiKeyDao.clearDefaultFlags()
        }
        val entity = ApiKeyEntity(
            id = id,
            name = name.trim(),
            apiKey = key.trim(),
            isDefault = isDefault
        )
        apiKeyDao.updateApiKey(entity)
    }

    override suspend fun deleteApiKey(apiKey: ApiKey) = withContext(Dispatchers.IO) {
        apiKeyDao.deleteApiKey(
            ApiKeyEntity(
                id = apiKey.id,
                name = apiKey.name,
                apiKey = apiKey.apiKey,
                isDefault = apiKey.isDefault,
                createdAt = apiKey.createdAt
            )
        )
        // Si la clé supprimée était la clé par défaut, sélectionner la plus récente
        if (apiKey.isDefault) {
            val remaining = apiKeyDao.getDefaultApiKey()
            if (remaining == null) {
                // S'il reste d'autres clés, désigner la première
                // Note : le DAO gère le retour null si vide
            }
        }
    }

    override suspend fun setDefaultApiKey(id: Long) = withContext(Dispatchers.IO) {
        apiKeyDao.setDefaultApiKey(id)
    }

    override suspend fun hasAnyKey(): Boolean = withContext(Dispatchers.IO) {
        apiKeyDao.getApiKeyCount() > 0
    }

    private fun ApiKeyEntity.toDomain() = ApiKey(
        id = id,
        name = name,
        apiKey = apiKey,
        isDefault = isDefault,
        createdAt = createdAt
    )
}
