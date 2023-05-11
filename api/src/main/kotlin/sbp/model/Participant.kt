package sbp.model

import com.noumenadigital.npl.api.generated.sbp.ParticipantFacade
import com.noumenadigital.npl.api.generated.support.PaymentDetailsFacade
import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import java.util.UUID

open class Participant(
    var name: String,
    var surname: String,
    var mail: String,
    var details: PaymentDetailsFacade,
    var accountHash: String,
) {
    constructor(facade: ParticipantFacade) : this(
        name = facade.fields.name,
        surname = facade.fields.surname,
        mail = facade.fields.mail,
        details = facade.fields.paymentDetails,
        accountHash = facade.fields.accountHash
    )
}

object User1 : Participant(
    name = "User",
    surname = "1",
    mail = "user1@mail.com",
    details = PaymentDetailsFacade(
        iban = "ZZ0000000000000001",
        name = "User 1",
        address = "User 1, Address 1111"
    ),
    accountHash = "hashuser1"
)

object User2 : Participant(
    name = "User",
    surname = "2",
    mail = "user2@mail.com",
    details = PaymentDetailsFacade(
        iban = "ZZ0000000000000002",
        name = "User 2",
        address = "User 2, Address 2222"
    ),
    accountHash = "hashuser2"
)

object User3 : Participant(
    name = "User",
    surname = "3",
    mail = "user3@mail.com",
    details = PaymentDetailsFacade(
        iban = "ZZ0000000000000003",
        name = "User 3",
        address = "User 3, Address 3333"
    ),
    accountHash = "hashuser3"
)

class ParticipantDetailsWithUUID(
    val uuid: UUID,
    name: String,
    surname: String,
    mail: String,
    details: PaymentDetailsFacade,
    accountHash: String,
) : Participant(name, surname, mail, details, accountHash) {
    constructor(uuid: UUID, facade: ParticipantFacade) : this(
        uuid = uuid,
        name = facade.fields.name,
        surname = facade.fields.surname,
        mail = facade.fields.mail,
        details = facade.fields.paymentDetails,
        accountHash = facade.fields.accountHash
    )

    constructor(uuid: UUID, model: ParticipantCreateModel) : this(
        uuid = uuid,
        name = model.name,
        surname = model.surname,
        mail = model.mail,
        details = model.details,
        accountHash = model.accountHash
    )
}

class ParticipantCreateModel(
    name: String,
    surname: String,
    mail: String,
    details: PaymentDetailsFacade,
    accountHash: String,
    val password: String,
) : Participant(name, surname, mail, details, accountHash)

val participantDetailsLens = Body.auto<ParticipantDetailsWithUUID>().toLens()
val participantCreateModelLens = Body.auto<ParticipantCreateModel>().toLens()
