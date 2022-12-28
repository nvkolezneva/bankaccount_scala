package bankaccount.model

import java.util.UUID

final case class Category(
    id: UUID = UUID.randomUUID(),
    name: String,
    cashback_percent: Int
)
