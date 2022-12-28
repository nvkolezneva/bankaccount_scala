package bankaccount.model

import java.util.UUID
import java.time.LocalDateTime

final case class CashOperation(
    account_Id: UUID,
    amount_change: Int,
    op_type: String // REPLENISH_ACCOUNT или CASHING_OUT
)

final case class CashOperationHistory(
    id: UUID = UUID.randomUUID(),
    account_Id: UUID,
    amount_change: Int,
    op_type: String, // REPLENISH_ACCOUNT или CASHING_OUT
    created_at: LocalDateTime = LocalDateTime.now()
)
