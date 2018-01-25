--
-- Introduces asterism table and updates to observation to reference it.
--

-- Add Ghost to e_instrument enum.
INSERT INTO e_instrument
         (id,      short_name, long_name, obsolete)
  VALUES ('Ghost', 'GHOST',    'GHOST',   false   );

-- Asterism discriminator

CREATE TYPE asterism_type AS ENUM (
  'SingleTarget'
-- GhostDualTarget, GhostHighResolution in the future
);

ALTER TYPE asterism_type OWNER TO postgres;


-- Add asterism_type to observation.

ALTER TABLE observation
  ADD asterism_type asterism_type NOT NULL;

-- Add an admittedly redundant unique constraint so that it asterism type can
-- serve as a foreign key referential integrity constraint in the asterism
-- tables.

ALTER TABLE observation
  ADD UNIQUE (program_id, observation_index, asterism_type);

-- Single target asterism can have any instrument except GHOST but it should
-- match the observation.

CREATE TABLE single_target_asterism (
  program_id        text          NOT NULL,
  observation_index id_index      NOT NULL,
  instrument        identifier    NOT NULL,
  asterism_type     asterism_type NOT NULL,
  target_id         integer       NOT NULL  REFERENCES target,
  PRIMARY KEY (program_id, observation_index, instrument),
  FOREIGN KEY (program_id, observation_index, instrument) REFERENCES observation ON DELETE CASCADE,
  FOREIGN KEY (program_id, observation_index, asterism_type) REFERENCES observation (program_id, observation_index, asterism_type) ON DELETE CASCADE,
  CONSTRAINT is_single CHECK (asterism_type = 'SingleTarget'),
  CONSTRAINT is_not_ghost CHECK (instrument != 'Ghost')
);

ALTER TABLE single_target_asterism OWNER TO postgres;
