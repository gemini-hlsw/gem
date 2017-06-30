--
-- Remove step_id from gcal.  step_gcal is used to tie gcal to step.  The
-- step_id was added to make deletion of step_gcal cascade through to the
-- gcal table.  When truncating the program table though, the truncate
-- unfortunately trickles down to gcal which also contains gcal configs
-- for smart_gcal.
--

ALTER TABLE gcal
  DROP COLUMN step_id CASCADE;

-- Delete the corresponding gcal row when a step_gcal row is removed.
CREATE FUNCTION delete_gcal() RETURNS trigger AS $delete_gcal$
  BEGIN
    DELETE FROM gcal WHERE gcal_id = OLD.gcal_id;
    RETURN OLD;
  END;
$delete_gcal$ LANGUAGE plpgsql;

CREATE TRIGGER delete_gcal AFTER DELETE ON step_gcal
  FOR EACH ROW EXECUTE PROCEDURE delete_gcal();
