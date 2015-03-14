package aghost7.realmsoftrinitytracker

import java.sql.Connection
import java.sql.Timestamp

import scala.io.Source
import scala.util.Either

import aghost7.scriptly._
import anorm._

import AnormSerializers._
import globals._


object Main extends App {
	
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
							Tracker.deciferLine(row[String]("message")) match {
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
	
	Loadscreen.animate { 
		Tracker.thread.start
		// Since its lazily initialized...
		ControlBar 
		println("Initialized!")
	}

	//patchDatabase
}