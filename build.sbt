name := "RealmsOfTrinityTracker"

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
	"postgresql" % "postgresql" % "9.1-901.jdbc4",
	"com.typesafe.play" %% "anorm" % "2.3.6"
)

assemblyJarName in assembly := "RealmsOfTrinityTracker.jar"

mainClass := Some("aghost7.realmsoftrinitytracker.Main")