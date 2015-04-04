package aghost7.realmsoftrinitytracker
import org.scalatest._

class RegexSpec extends FunSuite with Matchers {
	val withDonation = """<strong>Adam( <span class='philanthropist'>Philanthropist</span> )</strong> Orin Dawnstrike, 30  < AFK Mode > ( Primary - Trinity - Town Center )<br />"""
	val dm = """<span style="color : orange;"><strong>< Dungeon Master > ( <span class='gold'>Patron - Gold</span> )Geovex</strong> - Rembrask Ithivisk < RP Mode ></span><br />"""
	val noDonation = """<strong>Techdevil - </strong> Alanon, 23  <  > ( Primary - Trinity - Town Center )<br />"""
	
	def rmHtml(line: String) = Tracker.htmlPattern.replaceAllIn(line,"")
	
	test("Donation regex") {
		val line = rmHtml(withDonation)
		val result = Tracker.deciferLine(line)
		result.isLeft shouldBe (true) 
	}
	
	test("No donation regex") {
		val line = rmHtml(noDonation)
		val result = Tracker.deciferLine(line)
		result.isLeft shouldBe (true)
		
	}
	
	test("DM regex") {
		val line = rmHtml(dm)
		val result = Tracker.deciferLine(line)
		result.isLeft shouldBe (true)
	}
}