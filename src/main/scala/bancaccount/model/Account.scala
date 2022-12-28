package bankaccount.model

import java.util.UUID

final case class Account(
    id: UUID = UUID.randomUUID(),
    owner_user_id: UUID,
    amount: Int = 0,
    name: Option[String]
)
final case class create_account(name: Option[String], owner_user_id: UUID)
final case class update_account(name: Option[String])

final case class ChangeAccountAmountResult(
    id: UUID,
    amount: Int
)
