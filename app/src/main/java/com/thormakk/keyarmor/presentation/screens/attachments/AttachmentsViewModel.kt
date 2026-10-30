package com.thormakk.keyarmor.presentation.screens.attachments

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thormakk.keyarmor.data.prefs.ActiveVaultPrefs
import com.thormakk.keyarmor.data.prefs.PlanPrefs
import com.thormakk.keyarmor.data.prefs.UserPlan
import com.thormakk.keyarmor.data.repository.AttachmentError
import com.thormakk.keyarmor.data.repository.AttachmentRepository
import com.thormakk.keyarmor.domain.model.Attachment
import com.thormakk.keyarmor.domain.model.VaultMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    savedStateHandle:          SavedStateHandle,
    private val planPrefs:     PlanPrefs,
    activeVaultPrefs: ActiveVaultPrefs,
    private val repository:    AttachmentRepository
) : ViewModel() {

    val entryId: String = savedStateHandle["entryId"] ?: ""

    val attachments: StateFlow<List<Attachment>> =
        repository.observeAttachments(entryId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isFreeUser: StateFlow<Boolean> = planPrefs.planFlow
        .map { it == UserPlan.FREE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), planPrefs.effectivePlan() == UserPlan.FREE)

    val isActiveVaultReadOnly: StateFlow<Boolean> = combine(
        planPrefs.planFlow,
        activeVaultPrefs.activeVaultIdFlow,
        activeVaultPrefs.activeVaultIsInheritedFlow
    ) { plan, vaultId, isInherited ->
        isInherited || (plan == UserPlan.FREE &&
            vaultId != VaultMetadata.PRIMARY_VAULT_ID)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        activeVaultPrefs.activeVaultIsInherited ||
        (planPrefs.effectivePlan() == UserPlan.FREE &&
            activeVaultPrefs.activeVaultId != VaultMetadata.PRIMARY_VAULT_ID)
    )

    private val _events = Channel<AttachmentEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onFilePicked(uri: Uri, resolver: ContentResolver) {
        if (planPrefs.effectivePlan() == UserPlan.FREE) {
            viewModelScope.launch { _events.send(AttachmentEvent.Error(AttachmentUiError.PREMIUM_REQUIRED)) }
            return
        }
        viewModelScope.launch {
            val result = repository.addAttachment(entryId, resolver, uri)
            result.onFailure { err ->
                val event = when (err) {
                    is AttachmentError.FileTooLarge    -> AttachmentEvent.Error(AttachmentUiError.FILE_TOO_LARGE)
                    is AttachmentError.MaxFilesReached -> AttachmentEvent.Error(AttachmentUiError.MAX_FILES_REACHED)
                    else                               -> AttachmentEvent.Error(AttachmentUiError.GENERIC)
                }
                _events.send(event)
            }
        }
    }

    fun onDelete(attachment: Attachment) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
        }
    }

    fun onOpen(attachment: Attachment) {
        viewModelScope.launch {
            repository.openAttachment(attachment)
                .onSuccess { file -> _events.send(AttachmentEvent.OpenFile(file, attachment.mimeType)) }
                .onFailure { _events.send(AttachmentEvent.Error(AttachmentUiError.OPEN_FAILED)) }
        }
    }
}

sealed interface AttachmentEvent {
    data class OpenFile(val file: File, val mimeType: String) : AttachmentEvent
    data class Error(val kind: AttachmentUiError) : AttachmentEvent
}

enum class AttachmentUiError { FILE_TOO_LARGE, MAX_FILES_REACHED, OPEN_FAILED, GENERIC, PREMIUM_REQUIRED }
