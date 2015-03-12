package aghost7.realmsoftrinitytracker

import java.sql._
import anorm._
import anorm.ParameterValue.toParameterValue

case class Snapshot(
		accountName: String, 
		charName: String, 
		level:String, 
		mode: String, 
		area:String, 
		epithet: Option[String] = None) {

	override def toString = 
		s"""Account name: $accountName
			|Character name: $charName
			|Level: $level
			|Mode: $mode
			|Area: $area
			|Epithet: $epithet""".stripMargin
}

object Snapshot {
	def insertBatch(snapshots: Traversable[Snapshot])(implicit con: Connection): Unit =
		if(!snapshots.isEmpty){
			val sqlSnp = SQL("""
					INSERT INTO snapshots(account_name, char_name, "level", "mode", area, epithet)
					VALUES({account_name}, {char_name}, {level}, {mode}, {area}, {epithet})
				""")
				
			snapshots.foldLeft(sqlSnp.asBatch) { (accu, snp)=>
				accu.addBatchParams(
						snp.accountName, 
						snp.charName, 
						snp.level, 
						snp.mode,
						snp.area,
						snp.epithet)
			}.execute()
		}
	
	def insertBatchWithTimestamp(snapshots: Traversable[(Timestamp, Snapshot)])(implicit con: Connection): Unit = 
		if(!snapshots.isEmpty){
			val sqlSnp = SQL("""
					INSERT INTO snapshots(account_name, char_name, "level", "mode", area, epithet, stamp)
					VALUES({account_name}, {char_name}, {level}, {mode}, {area}, {epithet}, {stamp})
				""")
				
			snapshots.foldLeft(sqlSnp.asBatch) { case (accu, (stmp, snp))=>
				accu.addBatchParams(
						snp.accountName, 
						snp.charName, 
						snp.level, 
						snp.mode,
						snp.area,
						snp.epithet,
						stmp)
			}.execute()
		}
}