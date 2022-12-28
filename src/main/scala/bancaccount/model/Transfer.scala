package bankaccount.model

import java.util.UUID
import java.time.LocalDateTime

final case class transfer_by_account_Id(
    senderaccount_Id: UUID,
    recipientaccount_Id: UUID,
    transfer_amount: Int
)

final case class TransferHistory(
    id: UUID = UUID.randomUUID(),
    status: String,
    senderaccount_Id: UUID,
    recipientaccount_Id: UUID,
    categoryId: Option[UUID],
    transfer_amount: Int,
    cashback_amount: Option[Int],
    created_at: LocalDateTime = LocalDateTime.now()
)
