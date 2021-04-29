package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.ui.Country
import org.joda.time.Instant
import org.joda.time.LocalDate

interface VaccinationCertificate {

    val firstName: String
    val lastName: String
    val dateOfBirth: LocalDate

    val vaccinatedAt: Instant

    val vaccinationName: String
    val vaccinationManufacturer: String

    val chargeId: String

    val certificateIssuer: String
    val certificateCountry: Country

    val certificateId: String

    val identifier: VaccinatedPersonIdentifier
        get() = ""
}
