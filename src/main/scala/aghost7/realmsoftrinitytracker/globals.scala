package aghost7.realmsoftrinitytracker

import aghost7.scriptly._

package object globals {
	val delay = 60 * 1000 * 20
	val uri = s"jdbc:postgresql://localhost:5432/realmsoftrinitytracker?user=postgres&password=postgres"
	val sqlCon = SqlConnection(uri) _
	val writer = Writer("exceptions.txt") _
	val appName = "RealmsOfTrinityTracker"
}