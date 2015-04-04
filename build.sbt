name := "RealmsOfTrinityTracker"

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
	"postgresql" % "postgresql" % "9.1-901.jdbc4",
	"com.typesafe.play" %% "anorm" % "2.3.6",
	"org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

assemblyJarName in assembly := "RealmsOfTrinityTracker.jar"

mainClass := Some("aghost7.realmsoftrinitytracker.Main")

fork in run := true

connectInput in run := true