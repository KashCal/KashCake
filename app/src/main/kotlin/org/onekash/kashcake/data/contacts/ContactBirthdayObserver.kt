package org.onekash.kashcake.data.contacts

import android.database.ContentObserver
import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactBirthdayObserver(
    handler: Handler,
    private val scope: CoroutineScope,
    private val debounceMs: Long = 500L,
    private val onContactsChanged: () -> Unit
) : ContentObserver(handler) {

    private var debounceJob: Job? = null

    override fun onChange(selfChange: Boolean) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceMs)
            onContactsChanged()
        }
    }

    fun cancelPending() {
        debounceJob?.cancel()
        debounceJob = null
    }
}
