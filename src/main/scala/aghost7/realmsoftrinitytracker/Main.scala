package aghost7.realmsoftrinitytracker

import java.sql.Connection

import scala.io.Source
import scala.util.Either

import aghost7.scriptly._
import anorm._

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

case class Error(message: String)

object Main extends App {
	
	val delay = 60 * 1000 * 20
	val uri = "jdbc:postgresql://localhost:5432/realmsoftrinitytracker?user=postgres&password=650153135hW426"
	val sqlCon = SqlConnection(uri) _
	val writer = Writer("exceptions.txt") _
	
	val htmlPattern = """<([a-z\/][^<>]+[a-z"'\/]|[\/]?[a-z])>""".r
	
	val noDonation = """^([A-z0-9_ -]+)([ ]{1,2}-[ ]{1,2})([A-z0-9-_ ,]+)(,[ ]+)(.+)([ ]+<[ ]+)([A-z ]*)([ ]+>[ ]+\([ ]+)(.+)(\))$""".r
	val withDonation = """^([A-z0-9_ -]+)([ ]{0,1}\([ ]+)(.+)([ ]+\)[ ]+)([A-z0-9-_ ,]+)(,[ ]+)(.+)([ ]+<[ ]+)([A-z ]*)([ ]+>[ ]+\([ ]+)(.+)(\))$""".r
	
	def getLines: Iterator[String] = Source
		.fromURL("http://www.realmsoftrinity.com/PlayersOnline.aspx")
		.getLines
		.dropWhile { !_.contains("<h4>Player, Character, Level, Location</h4></br></br>") }
		.drop(1)
		.takeWhile { !_.contains("<h3>Epic Relevel Honors</h3>") }
		.map { htmlPattern.replaceAllIn(_, "") }
		.filter { _.trim != "" }
		
	def deciferLine(line: String): Either[Snapshot, Error] = line match {
		case noDonation(accountName, _, charName, _, level, _, mode, _, area, _) =>
			Left(Snapshot(accountName, charName, level, mode, area))
		case withDonation(accountName, _, epithet, _, charName, _, level, _, mode, _, area, _) =>
			Left(Snapshot(accountName, charName, level.trim, mode, area, Some(epithet)))
		case _ =>
			Right(Error(line))
	}
	
	def insertSnapshots(snapshots: Traversable[Snapshot])(implicit con: Connection): Unit =
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
	
	def insertErrors(errors: Traversable[Error])(implicit con: Connection): Unit =
		if(!errors.isEmpty){
			val sqlErr = SQL("INSERT INTO errors(message) VALUES ({msg})")
			
			errors.foldLeft(sqlErr.asBatch) { (batch, err) =>
				batch.addBatchParams(err.message)
			}.execute
		}
	
	def mainLoop: Unit = {
		try {
			val (snapshots, errors) = getLines
					.map(deciferLine)
					.foldLeft[(List[Snapshot], List[Error])]((Nil, Nil)) { 
						case ((snps, errs), eith) =>
							eith match {
								case Left(snp) => (snp :: snps, errs)
								case Right(err) => (snps, err :: errs)
							}
					}
			
			sqlCon { implicit con =>
				insertSnapshots(snapshots)
				insertErrors(errors)
			}
			
		} catch {
			case err: Throwable => 
				err.printStackTrace()
				writer { put => put(err) }
		}	finally {
			Thread.sleep(delay)
			mainLoop
		}
		
	}
	
	mainLoop
	
}