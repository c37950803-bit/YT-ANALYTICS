package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ApiKeyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO pour la manipulation des clés API YouTube dans Room.
 *
 * Explication métier :
 * Permet d'administrer l'ensemble des clés fournies par l'utilisateur,
 * de désigner une clé active par défaut et d'assurer un basculement instantané
 * en cas de dépassement de quota (Fallback élégant).
 */
@Dao
interface ApiKeyDao {

    /**
     * Récupère toutes les clés enregistrées ordonnées avec la clé par défaut en premier.
     */
    @Query("SELECT * FROM api_keys ORDER BY isDefault DESC, createdAt DESC")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    /**
     * Récupère la clé active par défaut actuelle.
     */
    @Query("SELECT * FROM api_keys WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultApiKey(): ApiKeyEntity?

    /**
     * Flux réactif observant la clé par défaut actuelle.
     */
    @Query("SELECT * FROM api_keys WHERE isDefault = 1 LIMIT 1")
    fun observeDefaultApiKey(): Flow<ApiKeyEntity?>

    /**
     * Récupère une clé par son identifiant unique.
     */
    @Query("SELECT * FROM api_keys WHERE id = :id")
    suspend fun getApiKeyById(id: Long): ApiKeyEntity?

    /**
     * Compte le nombre total de clés enregistrées.
     */
    @Query("SELECT COUNT(*) FROM api_keys")
    suspend fun getApiKeyCount(): Int

    /**
     * Insère une nouvelle clé API.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKeyEntity): Long

    /**
     * Met à jour une clé existante (nom ou valeur de clé).
     */
    @Update
    suspend fun updateApiKey(apiKey: ApiKeyEntity)

    /**
     * Supprime une clé spécifique.
     */
    @Delete
    suspend fun deleteApiKey(apiKey: ApiKeyEntity)

    /**
     * Réinitialise le statut par défaut de toutes les clés.
     */
    @Query("UPDATE api_keys SET isDefault = 0")
    suspend fun clearDefaultFlags()

    /**
     * Transaction atomique : marque une clé comme active/par défaut
     * en désactivant d'abord toutes les autres.
     */
    @Transaction
    suspend fun setDefaultApiKey(id: Long) {
        clearDefaultFlags()
        setApiKeyAsDefault(id)
    }

    @Query("UPDATE api_keys SET isDefault = 1 WHERE id = :id")
    suspend fun setApiKeyAsDefault(id: Long)
}
