package bankaccount.route

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import bankaccount.model._
import bankaccount.repository.AccountRepository
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

class AccountsRoute(repository: AccountRepository)(implicit
    ec: ExecutionContext
) extends FailFastCirceSupport {
  def route =
    (path("accounts") & get) {
      {
        val list = repository.accounts_list()
        complete(list)
      }
    } ~
      (path("accounts") & post) {
        entity(as[create_account]) { newAccount =>
          complete(repository.create_account(newAccount))
        }
      } ~
      (path("accounts" / JavaUUID) & get) { id =>
        onSuccess(repository.account_details(id)) {
          case Right(value) => complete(value)
          case Left(s) =>
            complete(StatusCodes.NotAcceptable, s)
        }
      } ~
      (path("accounts" / JavaUUID) & put) { id =>
        entity(as[update_account]) { update_account =>
          {
            onSuccess(repository.update_account(id, update_account)) {
              case Right(value) => complete(value)
              case Left(s) =>
                complete(StatusCodes.NotAcceptable, s)
            }
          }
        }
      } ~
      (path("accounts" / JavaUUID) & delete) { id =>
        complete(repository.delete_account(id))
      }
}
