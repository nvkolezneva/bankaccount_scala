package bankaccount.db

import bankaccount.db.AccountDb._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api._

class InitDb(implicit val ec: ExecutionContext, db: Database) {
  def prepare(): Future[_] = {
    db.run(accountTable.schema.createIfNotExists)
  }
}
