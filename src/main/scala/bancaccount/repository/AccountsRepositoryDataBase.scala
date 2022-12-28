package bankaccount.repository

import java.util.UUID
import bankaccount.db.AccountDb._
import bankaccount.model._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

class AccountsRepositoryDataBase(implicit val ec: ExecutionContext, db: Database)
    extends AccountRepository {

  override def accounts_list(): Future[Seq[Account]] = {
    db.run(accountTable.result)
  }

  override def get_account(id: UUID): Future[Account] = {
    db.run(accountTable.filter(_.id === id).result.head)
  }

  override def find_account(id: UUID): Future[Option[Account]] = {
    db.run(accountTable.filter(_.id === id).result.headOption)
  }

  def account_details(id: UUID): Future[Either[APIError, Account]] = {
    val query = accountTable.filter(_.id === id)
    for {
      accountOpt <- db.run(query.result.headOption)
      res = accountOpt
        .map(Right(_))
        .getOrElse(Left(APIError("Счёт не найден!")))
    } yield res
  }

  override def create_account(create: create_account): Future[Account] = {
    val newAccount =
      Account(owner_user_id = create.owner_user_id, name = create.name)

    for {
      _ <- db.run(accountTable += newAccount)
      res <- get_account(newAccount.id)
    } yield res
  }

  override def update_account(
      id: UUID,
      update_account: update_account
  ): Future[Either[APIError, Account]] = {
    val query = accountTable.filter(_.id === id).map(_.name)

    for {
      oldAccountNameOpt <- db.run(query.result.headOption)
      newName = update_account.name
      updatedName = oldAccountNameOpt
        .map { oldName =>
          {
            if (oldName == newName)
              Left(APIError("Такое имя уже существует, пожалуйста, придумайте новое"))
            else Right(newName)
          }
        }
        .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
      future = updatedName.map(name => db.run { query.update(name) }) match {
        case Right(future) => future.map(Right(_))
        case Left(s)       => Future.successful(Left(s))
      }
      updated <- future
      res <- find_account(id)
    } yield updated.map(_ => res.get)
  }

  override def delete_account(id: UUID): Future[Either[APIError, Unit]] = {
    val query = accountTable.filter(_.id === id)
    for {
      accountOpt <- db.run(query.result.headOption)
      deletedAccount = accountOpt
        .map(account => {
          if (account.amount != 0) {
            Left(APIError("Нельзя удалить не нулевой счёт"))
          } else {
            Right(account)
          }
        })
        .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
      future = deletedAccount.map(amount => db.run { query.delete }) match {
        case Right(future) => future.map(Right(_))
        case Left(s)       => Future.successful(Left(s))
      }
      deleted <- future
    } yield { deleted.map(Right(_)) }
  }

  override def replenishment_account(
      id: UUID,
      additionAmount: Int
  ): Future[Either[APIError, ChangeAccountAmountResult]] = {
    val query = accountTable.filter(_.id === id).map(_.amount)

    for {
      oldAccountOpt <- db.run(query.result.headOption)
      updatedAmount = oldAccountOpt
        .map { oldAmount => Right(oldAmount + additionAmount) }
        .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
      future = updatedAmount.map(amount =>
        db.run { query.update(amount) }
      ) match {
        case Right(future) => future.map(Right(_))
        case Left(s)       => Future.successful(Left(s))
      }
      updated <- future
      res <- find_account(id)
    } yield updated.map(_ => ChangeAccountAmountResult(id, res.get.amount))
  }

  override def cash_out_account(
      id: UUID,
      withdrawalAmount: Int
  ): Future[Either[APIError, ChangeAccountAmountResult]] = {
    val query = accountTable.filter(_.id === id).map(_.amount)

    for {
      oldAccountOpt <- db.run(query.result.headOption)
      updatedAmount = oldAccountOpt
        .map { oldAmount =>
          {
            if (oldAmount >= withdrawalAmount)
              Right(oldAmount - withdrawalAmount)
            else
              Left(APIError("Недостаточно средств на счету!"))
          }
        }
        .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
      future = updatedAmount.map(amount =>
        db.run { query.update(amount) }
      ) match {
        case Right(future) => future.map(Right(_))
        case Left(s)       => Future.successful(Left(s))
      }
      updated <- future
      res <- find_account(id)
    } yield updated.map(_ => ChangeAccountAmountResult(id, res.get.amount))
  }

  override def transfer_by_account_Id(
      transfer: transfer_by_account_Id
  ): Future[Either[APIError, ChangeAccountAmountResult]] = {
    val senderAccountQuery =
      accountTable.filter(_.id === transfer.senderaccount_Id).map(_.amount)
    val recipientAccountQuery =
      accountTable.filter(_.id === transfer.recipientaccount_Id).map(_.amount)

    for {
      senderAccountOpt <- db.run(senderAccountQuery.result.headOption)
      recipientAccountOpt <- db.run(recipientAccountQuery.result.headOption)
      transfer_amount = transfer.transfer_amount
      senderAmountUpd = senderAccountOpt
        .map { senderAmount =>
          {
            if (senderAmount >= transfer_amount)
              Right(senderAmount - transfer_amount)
            else
              Left(APIError("Недостаточно средств на счету!"))
          }
        }
        .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
      recipientAmountUpd = recipientAccountOpt
        .map { recipientAmount =>
          {
            Right(recipientAmount + transfer_amount)
          }
        }
        .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
      senderFuture = senderAmountUpd.map(amount =>
        db.run { senderAccountQuery.update(amount) }
      ) match {
        case Right(future) => {
          recipientAmountUpd.map(amount =>
            db.run { recipientAccountQuery.update(amount) }
          ) match {
            case Right(future) => future.map(Right(_))
            case Left(s)       => Future.successful(Left(s))
          }
        }
        case Left(s) => Future.successful(Left(s))
      }
      updated <- senderFuture
      res <- find_account(transfer.senderaccount_Id)
    } yield updated.map(_ =>
      ChangeAccountAmountResult(transfer.senderaccount_Id, res.get.amount)
    )
  }

}
