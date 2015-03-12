package aghost7.realmsoftrinitytracker

import anorm._
import java.sql._

case class Error(message: String)

object Error {
	def insertBatch(errors: Traversable[Error])(implicit con: Connection): Unit =
		if(!errors.isEmpty){
			val sqlErr = SQL("INSERT INTO errors(message) VALUES ({msg})")
			
			errors.foldLeft(sqlErr.asBatch) { (batch, err) =>
				batch.addBatchParams(err.message)
			}.execute
		}
	def deleteBatch(ids: Traversable[Int]) (implicit con: Connection): Unit = 
		if(!ids.isEmpty){
			val delSql = SQL("DELETE FROM errors WHERE id ={i}")
			ids
				.foldLeft(delSql.asBatch) { (b, i) => b.addBatchParams(i) }
				.execute()
		}
}