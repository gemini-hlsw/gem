## Seqexec / Gem Proposal

The new seqexec currently under development works with an OCS2 `ConfigSequence` which it obtains from the existing Observing Database over 
our custom RPC protocol.  The `ConfigSequence` is essentially a `List[Config]` where each `Config` is roughly a `Map[String, Object]`.  The
`Object`s in the sequence vary from primitive values to specific enumerations like `Flamingos2.Filter`.  Client code has to know the expected 
type of the `Object` associated with each key in order to work with it.

At the same time, a new sequence model is being developed as part of the OCS3 effort.  The new sequence model avoids the indignity and risk of
having to downcast from `Object` to the "known" type for a key.  Eventually the new seqexec will abandon the OCS2 sequence in favor of the new
model.  This brief document explores the possibility of developing the new seqexec directly on the OCS3 sequence, bypassing the intermediate step
of targeting OCS2 first.


## Current Status

The new seqexec is slated for production well before the OCS3 code will be deployed in full. Nevertheless the sequence model itself along with
Flamingos 2 and GCal configurations are fairly well baked.  I believe that with comparatively little effort enough of the sequence would be
available to comply with the needs of the seqexec.

### What We Have

* Sequence as a `List[Step]`.
* `Step` as an ADT with bias, dark, gcal, science, and smart gcal options.
* Telescope offset configuration for science steps.
* Complete gcal configuration for gcal steps.
* Smart gcal expansion for Flamingos 2.
* Instrument configuration for Flamingos 2.
* Data-access code for reading/writing the sequence to a postgres database.

All the above is type safe and straightforward to use.

### What We're Missing

* All other instrument configurations.
* "Guide With" values.
* Ready/Complete status.
* others?


## Advantages of Working with OCS3 Sequence

* Avoids eventually having to circle back and rewrite parts of the seqexec to migrate to the new model.
* Exercises the new sequence model well before it would otherwise be available for production, partially addressing the governing board concerns.
* Exposes the new program model and database to the rest of the high-level team.
* Necessitates combining the current `ocs3` and `gem` builds and deployment models, which keeps them from diverging.
* Concentrates all OCS2 to OCS3 translation code in one place, instead of having both seqexec and "gem" projects translating and depending on OCS2.
* Allows us to flesh out dataset record, observing events, and logs in the new program model and postgres database.


## Sketching Out a Course

The technical issue preventing the new seqexec from working with the new sequence model today is that it must continue to read sequences from the
existing Observing Database.  The new science program model is not complete and there is no UI for editing it.  A potential solution is to offer
an enqueue service that:

* Contacts the ODB and requests the OCS2 `ConfigSequence` over trpc. 
* Translates the config sequence into a minimal science program with just enough information to contain the sequence.
* Writes the sequence to postgres database tables, replacing any existing sequence information for the observation.

The seqexec could then work directly with the new sequence in the postgres database.  The enqueue service could be developed separately from the
rest of the combined seqexec/gem project because it will be dependent on OCS2 versions of pervasive libraries like `scalaz`.  The rest of the new
codebase is then freed of its OCS2 shackles.  (Though of course, the seqexec will still need to use the XMLPRC OCS2 `wdba` services to record
execution events in the Observing Database.)

As Carlos suggested, pressing the "Queue" button in today's OT could be made to trigger the enqueue service to perform these steps.

To keep the seqexec on schedule, I could write the enqueue service and provide support for working with the new sequence model as necessary.
Outside of the enqueue service, I don't have reason to anticipate a lengthy or difficult transition to the new sequence model.  As new instruments
are incorporated they would be developed directly in the new science program model.


