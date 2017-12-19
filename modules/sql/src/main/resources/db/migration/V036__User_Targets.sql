--
-- Adds user targets to the model.
--

CREATE TABLE e_user_target_type (
  id         identifier            PRIMARY KEY,
  short_name character varying(20) NOT NULL,
  long_name  character varying(20) NOT NULL,
  obsolete   boolean               NOT NULL
);

ALTER TABLE e_user_target_type OWNER TO postgres;

COPY e_user_target_type (id, short_name, long_name, obsolete) FROM stdin;
BlindOffset	Blind Offset	Blind Offset	f
OffAxis	Off Axis	Off Axis	f
TuningStar	Tuning Star	Tuning Star	f
Other	Other	Other	f
\.

