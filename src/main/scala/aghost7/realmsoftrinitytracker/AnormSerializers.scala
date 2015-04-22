package aghost7.realmsoftrinitytracker

import anorm._
import java.sql.Timestamp

object AnormSerializers {

	implicit def timestampColumn : Column[Timestamp] = Column.nonNull1 {
		case (value: Timestamp, meta) => Right(value)
		case _ => Left(TypeDoesNotMatch("Timestamp type mismatch"))
	}
}