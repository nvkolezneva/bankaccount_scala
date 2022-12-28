package bankaccount.model

import java.util.UUID

final case class User(
    id: UUID = UUID.randomUUID(),
    last_name: String,
    first_name: String,
    patricity: Option[String],
    phone: String,
    priority_account_id: Option[UUID],
    is_admin: Option[Boolean]
)

final case class UserPriorityAccount(
    user_id: UUID,
    bank_account_id: UUID
)
