-- Database initialization script...

CREATE TABLE snapshots (
	id SERIAL,
	account_name TEXT NOT NULL,
	char_name TEXT NOT NULL,
	"level" TEXT NOT NULL,
	"mode" TEXT NOT NULL,
	area TEXT NOT NULL,
	epithet TEXT,
	stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE errors(
	id SERIAL,
	message TEXT NOT NULL,
	stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);