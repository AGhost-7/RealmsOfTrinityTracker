package aghost7.realmsoftrinitytracker

import scala.io.Source
import anorm._
import java.sql.{Connection, Timestamp}

import globals._

object Tracker extends Runnable {
	
	val htmlPattern = """<([a-z\/][^<>]+[a-z"'\/]|[\/]?[a-z])>""".r
	
	val rgCharName = """([A-z0-9-_ ,'&]+)"""
	val rgAccName = """([A-z0-9-_ ,'&]+)"""
	val rgArea = """([A-z 0-9-&'.]+)"""
	val rgLevel = """([ER0-9 ]+)"""
	val rgMode = """([A-z, ]*)"""
	val rgDonation = """([A-z -]+)"""	
	
	val rgNoDonation = s"""$rgAccName([ ]{1,2}-[ ]{1,2})$rgCharName(,[ ]+)$rgLevel([ ]+<[ ]+)$rgMode([ ]+>[ ]+\\([ ]+)$rgArea(\\))""".r
	val rgWithDonation = s"""${rgAccName}([ ]*\\([ ]*)${rgDonation}([ ]*\\)[ ]*)${rgCharName}(,[ ]+)${rgLevel}([ ]+<[ ]+)${rgMode}([ ]+>[ ]+\\([ ]+)${rgArea}(\\))""".r
	val rgDungeonMaster = """^(< Dungeon Master > *[(] *)([A-z -]+)( *[)] *)(.+)( *[-] *)([A-z0-9-_ ,']+)( *< *)([A-z ]*)( *> *)$""".r
	
	val thread = new Thread(this)
	
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
		case rgNoDonation(accountName, _, charName, _, level, _, mode, _, area, _) =>
			Left(Snapshot(
				accountName.trim, 
				charName.trim, 
				level.trim, 
				mode.trim, 
				area.trim))
		case rgWithDonation(accountName, _, epithet, _, charName, _, level, _, mode, _, area, _) =>
			Left(Snapshot(
				accountName.trim, 
				charName.trim, 
				level.trim, 
				mode.trim, 
				area.trim, 
				Some(epithet.trim)))
		case rgDungeonMaster(_, epithet, _, accountName, _, charName, _, mode, _) =>
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
	
	/** Checks to see if there are any players that I want to catch online. */
	def checkAccountWatch(
			snapshots: Traversable[Snapshot], 
			lastSnapshots: Traversable[Snapshot])
			(implicit con: Connection): Unit = {
		
		val watches = SQL("SELECT account_name FROM account_watch")()
					.map { _[String]("account_name") }
		
		val matches = snapshots
				.filter { snp =>
					if(snp.mode.contains("AFK")) 
						false
					else if(watches.contains(snp.accountName))
						lastSnapshots
							.find { _.accountName == snp.accountName }
							.map { _.area != snp.area }
							.getOrElse(false)
					else
						false
				}
				.map { _.accountName }
		
		
		if(!matches.isEmpty){
			val list = matches.map { " - " +_ }.mkString("\n")
			ControlBar.notify("Users currently online", "Users online:\n" + list)
		}
	}
	
	/** This is the loop responsible for fetching the data every 20 minutes. */
	def mainLoop(lastSnapshots: Traversable[Snapshot]): Unit = {
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
				checkAccountWatch(snapshots, lastSnapshots)
			}
			
			Thread.sleep(delay)
			mainLoop(snapshots)
			
		} catch {
			case _: InterruptedException =>
				// do nothing I guess...
			case err: Throwable => 
				err.printStackTrace()
				writer { put => put(err) }
				Thread.sleep(delay)
				mainLoop(lastSnapshots)
		}
		
	}
	
	def run : Unit = mainLoop(Nil)
	
	
	
}