package bankaccount

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import bankaccount.db.InitDb
import bankaccount.repository.AccountsRepositoryDataBase
import bankaccount.route._
import scala.concurrent.ExecutionContext
import scala.io.StdIn
import slick.jdbc.PostgresProfile.api._

object BankAccountDbApp extends App {
  implicit val system: ActorSystem = ActorSystem("BankAccountApp")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val db = Database.forConfig("database.postgres")

  new InitDb().prepare()
  val repository = new AccountsRepositoryDataBase
  val accountsRoute = new AccountsRoute(repository).route
  val transfersRoute = new TransfersRoute(repository).route
  val cashRoute = new CashRoute(repository).route
  val helloRoute = new HelloRoute().route

  val bindingFuture =
    Http()
      .newServerAt("0.0.0.0", 8081)
      .bind(
        helloRoute
          ~ accountsRoute
          ~ transfersRoute
          ~ cashRoute
      )

  println(
    s"Сервер запущен. Перейдите по ссылке -> http://localhost:8081/hello \n Нажмите RETURN для остановки..."
  )
  StdIn.readLine()
}
