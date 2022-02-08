package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.ui.text.CCLTextFormatter
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

class BoosterInfoDetailsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    personCertificatesProvider: PersonCertificatesProvider,
    private val vaccinationRepository: VaccinationRepository,
    @Assisted private val personIdentifierCode: String,
    private val format: CCLTextFormatter,
) : CWAViewModel(dispatcherProvider) {

    val shouldClose = SingleLiveEvent<Unit>()

    private val uiStateFlow = personCertificatesProvider.personCertificates.mapNotNull { certificateSet ->
        val boosterNotification = certificateSet.first { it.personIdentifier?.codeSHA256 == personIdentifierCode }
            .dccWalletInfo!!.boosterNotification.also {
            it.identifier?.let { id ->
                vaccinationRepository.acknowledgeBoosterRule(
                    personIdentifierCode = personIdentifierCode,
                    boosterIdentifier = id
                )
            }
        }
        UiState(
            titleText = format(boosterNotification.titleText),
            subtitleText = format(boosterNotification.subtitleText),
            longText = format(boosterNotification.longText),
            faqUrl = format(boosterNotification.faqAnchor)
        )
    }.catch { error ->
        // This should never happen due to checks on previous screen
        Timber.d(error, "No person found for $personIdentifierCode")
        shouldClose.postValue(Unit)
    }
    val uiState = uiStateFlow.asLiveData2()

    data class UiState(
        val titleText: String,
        val subtitleText: String,
        val longText: String,
        val faqUrl: String?,
    )

    @AssistedFactory
    interface Factory : CWAViewModelFactory<BoosterInfoDetailsViewModel> {
        fun create(
            personIdentifierCode: String
        ): BoosterInfoDetailsViewModel
    }
}