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

class CashRoute(repository: AccountRepository)(implicit
    ec: ExecutionContext
) extends FailFastCirceSupport {
  def route = (path("cash") & post) {
    entity(as[CashOperation]) { cashOperation =>
      {
        cashOperation.op_type match {
          case "REPLENISH_ACCOUNT" =>
            onSuccess(
              repository.replenishment_account(
                cashOperation.account_Id,
                cashOperation.amount_change
              )
            ) {
              case Right(value) => complete(value)
              case Left(s) =>
                complete(StatusCodes.NotAcceptable, s)
            }
          case "CASHING_OUT" =>
            onSuccess(
              repository.cash_out_account(
                cashOperation.account_Id,
                cashOperation.amount_change
              )
            ) {
              case Right(value) => complete(value)
              case Left(s) =>
                complete(StatusCodes.NotAcceptable, s)
            }
          case _ =>
            complete(StatusCodes.NotAcceptable, "Запрос некоректен")
        }
      }
    }
  }
}
