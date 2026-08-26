package com.example.presentation.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Classe abstraite de base pour les Presenters du pattern MVP.
 *
 * Explication technique :
 * - Gère le cycle de vie de la Vue attachée pour éviter toute fuite de mémoire.
 * - Fournit un `CoroutineScope` dédié avec `SupervisorJob` et `Dispatchers.Main`
 *   qui est automatiquement annulé lors du détachement de la Vue (`detachView()`).
 */
abstract class BasePresenter<V : BaseView> {

    protected var view: V? = null
        private set

    private var presenterJob = SupervisorJob()
    protected var presenterScope = CoroutineScope(Dispatchers.Main + presenterJob)
        private set

    /**
     * Attache la vue au Presenter.
     */
    open fun attachView(view: V) {
        this.view = view
        if (presenterJob.isCancelled) {
            presenterJob = SupervisorJob()
            presenterScope = CoroutineScope(Dispatchers.Main + presenterJob)
        }
    }

    /**
     * Détache la vue et annule les Coroutines en cours pour prévenir les fuites mémoire.
     */
    open fun detachView() {
        this.view = null
        presenterJob.cancel()
    }

    /**
     * Vérifie si la vue est actuellement attachée.
     */
    protected val isViewAttached: Boolean
        get() = view != null
}
