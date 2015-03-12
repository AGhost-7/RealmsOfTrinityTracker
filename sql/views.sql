
CREATE OR REPLACE VIEW "Activity (Today)" AS 
	SELECT *
	FROM snapshots
		WHERE stamp > ('now'::text::date + '01:00:00'::interval);

-- Since I'm making a couple of views for this, I might as well make a function
-- to keep things maintainable.
CREATE OR REPLACE FUNCTION is_guild(area text) RETURNS BOOLEAN
AS $$
BEGIN
	return area ~~ 'Secondary - Sanctuary%'
			OR area ~~ '%Arcane Society%'
			OR (area ~~ '%Guild%' 
				AND area <> 'Primary - Eryndlyn - Guild of Shadows ')
			OR area ~~ '%The O Cult%';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE VIEW "Guild Activity" AS
	SELECT * FROM snapshots WHERE is_guild(area);

CREATE OR REPLACE VIEW "Guild Activity (Today)" AS 
	SELECT * FROM "Activity (Today)" WHERE is_guild(area);
		
CREATE OR REPLACE VIEW "Guild Activity" AS
		SELECT * FROM snapshots WHERE is_guild(area);

CREATE OR REPLACE VIEW "Guild Account Activity" AS
	SELECT DISTINCT ON(account_name, area) *
	FROM snapshots 
		WHERE is_guild(area);
		
CREATE OR REPLACE VIEW "Guild Account Activity (Today)" AS
	SELECT DISTINCT ON(account_name, area) *
	FROM "Activity (Today)"
		WHERE is_guild(area);