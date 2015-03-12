package aghost7.realmsoftrinitytracker

import java.sql.Connection
import java.sql.Timestamp

import scala.io.Source
import scala.util.Either

import aghost7.scriptly._
import anorm._

import AnormSerializers._



object Main extends App {
	
	val delay = 60 * 1000 * 20
	val uri = "jdbc:postgresql://localhost:5432/realmsoftrinitytracker?user=postgres&password=650153135hW426"
	val sqlCon = SqlConnection(uri) _
	val writer = Writer("exceptions.txt") _
	
	val htmlPattern = """<([a-z\/][^<>]+[a-z"'\/]|[\/]?[a-z])>""".r
	
	val noDonation = """^([A-z0-9_ -]+)([ ]{1,2}-[ ]{1,2})([A-z0-9-_ ,']+)(,[ ]+)(.+)([ ]+<[ ]+)([A-z ]*)([ ]+>[ ]+\([ ]+)(.+)(\))$""".r
	val withDonation = """^([A-z0-9_ -]+)([ ]{0,1}\([ ]+)(.+)([ ]+\)[ ]+)([A-z0-9-_ ,']+)(,[ ]+)(.+)([ ]+<[ ]+)([A-z ]*)([ ]+>[ ]+\([ ]+)(.+)(\))$""".r
	val dungeonMaster = """^(< Dungeon Master > *[(] *)([A-z -]+)( *[)] *)(.+)( *[-] *)([A-z0-9-_ ,']+)( *< *)([A-z ]*)( *> *)$""".r
	
	/** Fetches the lines needed for processing and strips out the html tags. */
	def getLines: Iterator[String] = Source
		.fromURL("http://www.realmsoftrinity.com/PlayersOnline.aspx")
		.getLines
		.dropWhile { !_.contains("<h4>Player, Character, Level, Location</h4></br></br>") }
		.drop(1)
		.takeWhile { !_.contains("<h3>Epic Relevel Honors</h3>") }
		.map { htmlPattern.replaceAllIn(_, "") }
		.filter { _.trim != "" }
	
	/** Returns an instance of either a Snapshot or Error entry for the database
	 *  based on the regex matches.
	 */
	def deciferLine(line: String): Either[Snapshot, Error] = line match {
		case noDonation(accountName, _, charName, _, level, _, mode, _, area, _) =>
			Left(Snapshot(
				accountName.trim, 
				charName.trim, 
				level.trim, 
				mode.trim, 
				area.trim))
		case withDonation(accountName, _, epithet, _, charName, _, level, _, mode, _, area, _) =>
			Left(Snapshot(
				accountName.trim, 
				charName.trim, 
				level.trim, 
				mode.trim, 
				area.trim, 
				Some(epithet.trim)))
		case dungeonMaster(_, epithet, _, accountName, _, charName, _, mode, _) =>
			Left(Snapshot(
				accountName.trim, 
				charName.trim, 
				"**DM Hidden**", 
				mode.trim, 
				"**DM Hidden**",
				Some(epithet.trim)))
		case _ =>
			Right(Error(line))
	}
	
	/** This is the loop responsible for fetching the data every 20 minutes. */
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
				Snapshot.insertBatch(snapshots)
				Error.insertBatch(errors)
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
	
	/** Goes through all errors and runs them through the updated deciferLine 
	 *  method. Deletes the error entry and inserts the corrected snapshot.
	 */
	def patchDatabase: Unit = {
		sqlCon { implicit cont =>
			type StampedSnapshots = List[(Timestamp, Snapshot)]
			val (snapshots, delete) = 
				SQL("SELECT * FROM errors")()
					.foldLeft[(StampedSnapshots, List[Int])]((Nil, Nil)) { 
						case((snapshots, delete), row) =>
							deciferLine(row[String]("message")) match {
								case Left(snp) => 
									val id = row[Int]("id")
									val stamp = row[Timestamp]("stamp")
									((stamp, snp) :: snapshots, id :: delete)
								case Right(err) => (snapshots, delete)
							}
					}
			//println("fixed " + delete.length)
			
			Snapshot.insertBatchWithTimestamp(snapshots)
			Error.deleteBatch(delete)
			
		}
	}
	
	mainLoop
	//patchDatabase
}