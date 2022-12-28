package bankaccount.repository

import java.util.UUID
import bankaccount.model._
import scala.concurrent.Future

trait AccountRepository {
  // Список счётов
  def accounts_list(): Future[Seq[Account]]

  // Получение счёта
  def get_account(id: UUID): Future[Account]

  // Поиск по счёту
  def find_account(id: UUID): Future[Option[Account]]

  // Детализация счёта в API
  def account_details(id: UUID): Future[Either[APIError, Account]]

  // Создание счёта
  def create_account(create: create_account): Future[Account]

  // Редактирование счёта
  def update_account(
      id: UUID,
      update: update_account
  ): Future[Either[APIError, Account]]

  // Удаление счёта
  def delete_account(id: UUID): Future[Either[APIError, Unit]]

  // Пополнение счёта
  def replenishment_account(
      id: UUID,
      additionAmount: Int
  ): Future[Either[APIError, ChangeAccountAmountResult]]

  // Обналичить со счёта
  def cash_out_account(
      id: UUID,
      withdrawalAmount: Int
  ): Future[Either[APIError, ChangeAccountAmountResult]]

  //  Перевести деньги по ID счёта
  def transfer_by_account_Id(
      transfer: transfer_by_account_Id
  ): Future[Either[APIError, ChangeAccountAmountResult]]
}
