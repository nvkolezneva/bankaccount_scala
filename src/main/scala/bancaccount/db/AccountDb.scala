package bankaccount.db

import java.util.UUID
import bankaccount.model.Account
import slick.jdbc.PostgresProfile.api._

object AccountDb {
  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {

    val id = column[UUID]("id", O.PrimaryKey)
    val owner_user_id = column[UUID]("owner_user_id")
    val amount = column[Int]("amount")
    val name = column[Option[String]]("name")

    override def * = (
      id,
      owner_user_id,
      amount,
      name
    ) <> ((Account.apply _).tupled, Account.unapply _)
  }

  val accountTable = TableQuery[AccountTable]
}
