package bankaccount.repository

import java.util.UUID
import bankaccount.model._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AccountRepositoryMutable(implicit val ex: ExecutionContext)
    extends AccountRepository {

  private val accountsStore = mutable.Map[UUID, Account]()

  override def accounts_list(): Future[List[Account]] = Future {
    accountsStore.toList.map(_._2)
  }

  override def get_account(account_Id: UUID): Future[Account] = Future {
    accountsStore(
      account_Id
    )
  }

  override def find_account(account_Id: UUID): Future[Option[Account]] = Future {
    accountsStore.get(
      account_Id
    )
  }

  override def account_details(
      id: UUID
  ): Future[Either[APIError, Account]] = ???

  override def create_account(create: create_account): Future[Account] = Future {
    val account =
      Account(
        id = UUID.randomUUID(),
        owner_user_id = create.owner_user_id,
        amount = 0,
        name = create.name
      )
    accountsStore.put(account.id, account)
    account
  }

  override def update_account(
      id: UUID,
      update: update_account
  ): Future[Either[APIError, Account]] = Future {
    accountsStore
      .get(id)
      .map { account =>
        {
          val updatedAccount = account.copy(name = update.name)
          accountsStore.put(account.id, updatedAccount)
          Right(updatedAccount)
        }
      }
      .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
  }

  override def delete_account(id: UUID): Future[Either[APIError, Unit]] =
    Future {
      Right(accountsStore.remove(id))
    }

  override def replenishment_account(
      id: UUID,
      additionAmount: Int
  ): Future[Either[APIError, ChangeAccountAmountResult]] = Future {
    accountsStore
      .get(id)
      .map { account =>
        {
          val updatedAccount =
            account.copy(amount = account.amount + additionAmount)
          accountsStore.put(account.id, updatedAccount)
          Right(
            ChangeAccountAmountResult(
              id,
              updatedAccount.amount
            )
          )
        }
      }
      .getOrElse(Left(APIError("Мы не смогли найти аккаунт!!!")))
  }

  override def cash_out_account(
      id: UUID,
      withdrawalAmount: Int
  ): Future[Either[APIError, ChangeAccountAmountResult]] = Future {
    accountsStore
      .get(id)
      .map { account =>
        val updatedAccount =
          account.copy(amount = account.amount - withdrawalAmount)
        accountsStore.put(account.id, updatedAccount)
        Right(
          ChangeAccountAmountResult(
            id,
            updatedAccount.amount
          )
        )
      }
      .getOrElse(Left(APIError("Мы не смогли найти счет!!!")))
  }

  
  override def transfer_by_account_Id(
      transfer: transfer_by_account_Id
  ): Future[Either[APIError, ChangeAccountAmountResult]] = ???
}
