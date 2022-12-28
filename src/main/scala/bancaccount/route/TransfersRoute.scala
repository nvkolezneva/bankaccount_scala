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

class TransfersRoute(repository: AccountRepository)(implicit
    ec: ExecutionContext
) extends FailFastCirceSupport {
  def route = (path("transfer" / "by_account_id") & post) {
    entity(as[transfer_by_account_Id]) { transfer_by_account_Id =>
      {
        onSuccess(repository.transfer_by_account_Id(transfer_by_account_Id)) {
          case Right(value) => complete(value)
          case Left(s) =>
            complete(StatusCodes.NotAcceptable, s)
        }
      }
    }
  }
}
