package bankaccount

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import java.util.UUID
import bankaccount.model._
import bankaccount.repository.AccountRepositoryMutable
import bankaccount.route._
import scala.concurrent.ExecutionContext
import scala.io.StdIn

object BankAccountMemoryApp extends App {
  // Создаём систему акторов
  implicit val system: ActorSystem = ActorSystem("BankAccountApp")
  implicit val ec: ExecutionContext = system.dispatcher

  // Создаём данные для хранения в репозитории
  val repository: AccountRepositoryMutable = new AccountRepositoryMutable
  val user: User = User(
    id = UUID.randomUUID(),
    first_name = "Kolezneva",
    last_name = "Nadezhda",
    patricity = Some("Valentinovna"),
    phone = "+79044757569",
    priority_account_id = None,
    is_admin = Some(false)
  )
  repository
    .create_account(
      create_account(name = Some("test_account"), owner_user_id = user.id)
    )
    .map { account =>
      {
        repository.replenishment_account(account.id, 500)
        repository.cash_out_account(account.id, 500)
      }
    }

  val accountsRoute = new AccountsRoute(repository).route
  val helloRoute = new HelloRoute().route

  val bindingFuture =
    Http().newServerAt("0.0.0.0", 8081).bind(helloRoute ~ accountsRoute)

  println(
    s"Сервер запущен. Перейдите по ссылке -> http://localhost:8081/hello \n Нажмите RETURN для остановки..."
  )
  StdIn.readLine()
}
