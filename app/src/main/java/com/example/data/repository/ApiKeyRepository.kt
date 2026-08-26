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
    companion object {
        const val DEFAULT_BUILTIN_API_KEY = "AIzaSyAz35xRYYG9VTKnWT0-cFExPdJaVr2v4EM"
        const val DEFAULT_BUILTIN_KEY_NAME = "Clé YouTube Data v3 (Samuel Driver / Défaut)"
    }

    fun getAllApiKeys(): Flow<List<ApiKey>>
    suspend fun getDefaultApiKey(): ApiKey
    fun observeDefaultApiKey(): Flow<ApiKey>
    suspend fun getCandidateKeys(preferredKey: String? = null): List<String>
    suspend fun ensureDefaultKeyExists(): ApiKey
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
            if (entities.isEmpty()) {
                listOf(createBuiltinApiKeyDomain(1L))
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun ensureDefaultKeyExists(): ApiKey = withContext(Dispatchers.IO) {
        val currentDefault = apiKeyDao.getDefaultApiKey()
        if (currentDefault != null) {
            return@withContext currentDefault.toDomain()
        }

        // Vérifie si la clé built-in existe déjà dans la base
        val existingBuiltin = apiKeyDao.findByKey(ApiKeyRepository.DEFAULT_BUILTIN_API_KEY)
        if (existingBuiltin != null) {
            apiKeyDao.setDefaultApiKey(existingBuiltin.id)
            return@withContext existingBuiltin.copy(isDefault = true).toDomain()
        }

        // S'il existe d'autres clés, marque la première comme défaut
        val allKeys = apiKeyDao.getAllApiKeysList()
        if (allKeys.isNotEmpty()) {
            val first = allKeys.first()
            apiKeyDao.setDefaultApiKey(first.id)
            return@withContext first.copy(isDefault = true).toDomain()
        }

        // Sinon, insère la clé par défaut officielle
        val entity = ApiKeyEntity(
            name = ApiKeyRepository.DEFAULT_BUILTIN_KEY_NAME,
            apiKey = ApiKeyRepository.DEFAULT_BUILTIN_API_KEY,
            isDefault = true,
            createdAt = System.currentTimeMillis()
        )
        val id = apiKeyDao.insertApiKey(entity)
        createBuiltinApiKeyDomain(id)
    }

    override suspend fun getDefaultApiKey(): ApiKey = withContext(Dispatchers.IO) {
        val fromDb = apiKeyDao.getDefaultApiKey()
        if (fromDb != null) {
            fromDb.toDomain()
        } else {
            ensureDefaultKeyExists()
        }
    }

    override fun observeDefaultApiKey(): Flow<ApiKey> {
        return apiKeyDao.observeDefaultApiKey().map { entity ->
            entity?.toDomain() ?: createBuiltinApiKeyDomain(1L)
        }
    }

    /**
     * Retourne la liste ordonnée et dédupliquée des clés à essayer pour un appel API
     * (Robuste Fallback en cascade).
     */
    override suspend fun getCandidateKeys(preferredKey: String?): List<String> = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<String>()

        // 1. Clé préférée ou explicitement demandée
        if (!preferredKey.isNullOrBlank()) {
            candidates.add(preferredKey.trim())
        }

        // 2. Clé active par défaut en base
        val defaultEntity = apiKeyDao.getDefaultApiKey()
        if (defaultEntity != null && defaultEntity.apiKey.isNotBlank()) {
            candidates.add(defaultEntity.apiKey.trim())
        }

        // 3. Toutes les autres clés enregistrées par l'utilisateur
        val allKeys = apiKeyDao.getAllApiKeysList()
        for (keyEntity in allKeys) {
            if (keyEntity.apiKey.isNotBlank()) {
                candidates.add(keyEntity.apiKey.trim())
            }
        }

        // 4. Clé officielle intégrée (Fallback absolu)
        candidates.add(ApiKeyRepository.DEFAULT_BUILTIN_API_KEY)

        // Déduplication tout en préservant l'ordre de priorité
        candidates.distinct()
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

    private fun createBuiltinApiKeyDomain(id: Long = 1L) = ApiKey(
        id = id,
        name = ApiKeyRepository.DEFAULT_BUILTIN_KEY_NAME,
        apiKey = ApiKeyRepository.DEFAULT_BUILTIN_API_KEY,
        isDefault = true,
        createdAt = System.currentTimeMillis()
    )
}
