
# Target Environment Notes

The target environment describes the collection of targets associated with an observation.  This document explores ideas for how we might represent this collection in a relational database. The target environment consists of three distinct parts:

1. _Asterism_. The science targets, or "asterism".  Before GHOST, this was always a single target. With the arrival of GHOST, we will need instrument-specific asterisms where particular targets are associated with various instrument features like the state of guide fibers and fiber "agitators".

2. _Guide Stars_. Selected guide stars, the probes with which they are associated and alternatives in case the chosen stars are not adequate for some reason. 

3. _User Targets_. So-called "user" targets for blind offsets, tuning stars, etc.

A goal is to keep the data model as flat as possible, so my initial thought is that there is no need for a `target_environment` table to tie these things together.  Instead, we have `observation` of course and then tables for the specific parts of the environment.


## Asterism

Asterisms for new instruments like GHOST incorporate instrument-specific features.  It seems clear that we'll need a structure somewhat like the one we have for static instrument configuration wherein there is a "base" table with an asterism id and asterism type along with type-specific tables holding the details.  Currently we have four types of asterism:

* Single Target
* GHOST Dual Target
* GHOST Beam Switching 
* GHOST High Resolution

Clearly asterisms can be relevant only to particular instruments.  It would be nice to ensure that the observation's instrument corresponds to the asterism's instrument so we cannot, for instance, use a GHOST Dual Target asterism with a GMOS observation.

First a discriminator:

```sql
CREATE TYPE asterism_type AS ENUM (
  'SingleTarget',
  'GhostDualTarget'
--  'GhostBeamSwitching',
--  'GhostHighRes'
);
```

Next we'll define the base table as the tuple (id, type, instrument).  We keep up with the instrument to make sure it corresponds to the observation and asterism type.


```sql
-- "Base" asterism table.  Here we make the primary key be a combination of
-- (asterism_id, asterism_type, instrument).  This is done so that the sub-
-- tables can have a foreign key reference to asterism that includes all three,
-- ensuring that they match.

CREATE TABLE asterism (
  asterism_id    SERIAL,
  asterism_type  asterism_type NOT NULL,
  instrument     identifier    NOT NULL    REFERENCES e_instrument,
  PRIMARY KEY (asterism_id, asterism_type, instrument)
);
```

Now we update observation to reference its one and only asterism.

```sql
-- Add columns to observation to track the associated asterism.  Along with
-- instrument, these become a foreign key pointing into the base asterism
-- table.

ALTER TABLE observation
  ADD asterism_id integer         NOT NULL,
  ADD asterism_type asterism_type NOT NULL;

-- Add a foreign key reference to the corresponding asterism. This ensures
-- that the type and instrument correspond to the observation.
ALTER TABLE observation
  ADD FOREIGN KEY (asterism_id, asterism_type, instrument) REFERENCES asterism ON DELETE CASCADE;

-- Ensure that no asterism appears in multiple observations.
ALTER TABLE observation
  ADD CONSTRAINT unique_asterism_per_obs UNIQUE (asterism_id);
```

Finally, we create the specific asterism sub-tables.  For the `single_target_asterism` any instrument except GHOST may be used.

```sql
-- Single target asterism can have any instrument except GHOST but it should
-- match the "base" and observation.

CREATE TABLE single_target_asterism (
  asterism_id   integer       NOT NULL,
  asterism_type asterism_type NOT NULL,
  instrument    identifier    NOT NULL,
  target_id     integer       NOT NULL  REFERENCES target(target_id),
  PRIMARY KEY (asterism_id, asterism_type, instrument) REFERENCES asterism ON DELETE CASCADE,
  CONSTRAINT is_single CHECK (asterism_type = 'SingleTarget'),
  CONSTRAINT is_not_ghost CHECK (instrument != 'GHOST')
);


-- A placeholder dual target asterism, just to have a second instance of
-- asterism.

CREATE TABLE ghost_dual_target_asterism (
  asterism_id   integer       NOT NULL,
  asterism_type asterism_type NOT NULL,
  instrument    identifier    NOT NULL,
  target_id_1   integer       NOT NULL  REFERENCES target(target_id),
  target_id_2   integer       NOT NULL  REFERENCES target(target_id),
  -- more configuration here for guide fibers, etc.
  PRIMARY KEY (asterism_id, asterism_type, instrument) REFERENCES asterism ON DELETE CASCADE,
  CONSTRAINT is_ghost_dual (asterism_type = 'GhostDualTarget'),
  CONSTRAINT is_ghost      (instrument    = 'GHOST')
);
```

An observation will be deleted if its asterism is deleted, but we'll need a trigger to delete the asterism when the corresponding observation is deleted.


## Guide Environment

Guiding is complicated enough to warrant its own "environment".  I think we can simplify it quite a bit though:

First, in the current model there is at least one extra layer of complexity that I think we can do away with.  We have a `GuideEnvironment` which has an automatic guide group and zero or more manual guide groups.  Within each manual group, a given guide probe may have multiple alternative guide stars.  I would like to eliminate the bottom layer, pushing it up one level.  In particular, if there is a need to keep multiple alternatives for a given guide probe, then this can be done with multiple groups.  In theory this could explode the number of manual guide groups necessary in cases where there are multiple probes each with its own list of alternative guide stars. In reality, this case doesn't happen, I think.

Second, I think we can remove the concept of order from the options.  Conceptually we have a set of options.  I don't believe that the order represents user preference but rather ensures that the alternatives aren't scrambled each time the target environment is viewed.  We can accomplish the same goal by sorting the coordinates, say by the tuple (Probe, RA, Dec) or perhaps by magnitude since that is probably the most relevant field for guiding.

Nevertheless we have to keep up with the selected manual group, if any.  (A selected manual group implies that we are rejecting the automatic guide group selection.)

First, we need to distinguish the automatic guide group from any manual groups.

```sql
CREATE TYPE guide_group_type AS ENUM (
  'Auto',
  'Manual'
);
```

Next, a guide group will need to track the observation with which it is associated and whether or not it is selected.

```sql
CREATE TABLE guide_group (
  guide_group_id   SERIAL                PRIMARY KEY,
  guide_group_type guide_group_type      NOT NULL,
  observation_id   character varying(40) NOT NULL REFERENCES observation ON DELETE CASCADE,
  selected         boolean               NOT NULL
);

-- Only one selected group per observation
CREATE UNIQUE INDEX ON guide_group(observation_id) WHERE (selected);

-- Only one auto group per observation
CREATE UNIQUE INDEX ON guide_group(observation_id) WHERE (guide_group_type = 'Auto');
```

Finally we need a table tying guide stars to the specific guide probes with which they will be tracked and the group in which they found.

```sql
CREATE TYPE guide_probe AS ENUM (
  'Pwfs1',
  'Pwfs2',
  'Gmos OI'
  -- etc.
);

CREATE TABLE guide_star (
  guide_star_id   SERIAL      PRIMARY_KEY,
  guide_probe     guide_probe NOT NULL,
  guide_group_id  integer     NOT NULL    REFERENCES guide_group ON DELETE CASCADE,
  target_id       integer     NOT NULL    REFERENCES target      ON DELETE CASCADE,
  UNIQUE (guide_group_id, guide_probe) -- No group should have more than one guide star assigned to a particular probe
);
```

Nothing here prevents us from adding guide probes that don't correspond to the observation's instrument (e.g., a GMOS OI guide star for a GHOST observation).  I don't see a good way to lock that down though.



## User Targets

User targets should be fairly straightforward.  They associate a user target type and target id with an observation. We need a user target type enum:

```sql
CREATE TYPE user_target_type AS ENUM (
  'BlindOffset',
  'OffAxis',
  'TuningStar',
  'Other' 
);
```

and a user target table:

```sql
CREATE TABLE user_target (
  user_target_id   SERIAL                PRIMARY KEY,
  target_id        integer               REFERENCES target(target_id),
  user_target_type user_target_type      NOT NULL,
  observation_id   character varying(40) NOT NULL REFERENCES observation ON DELETE CASCADE
);
```


