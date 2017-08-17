## Introduction

This page contains a few initial notes on observation group requirements and design.  The idea is to ensure that we understand what we're trying to accomplish before we spend too much time doing an implementation.

## Group Concept

In the existing OCS groups are explicitly part of the science program hierarchy, cannot be nested, and have limited impact on scheduling.  An observation can be in exactly zero or one group.  For OCS3, our idea is to capture groups by tagging observations (and other groups?).  This is the same concept behind "labels" in gmail.  What appear to be folders of emails are in fact just collections of emails with the same label.  Groups will work in a similar way in OCS3, except that there will be different types of group each with their own characteristics and scheduling implications.  It remains an open question how to portray groups in the user interface.  Like gmail, one can imagine a view of folders of observations for instance but folders probably won't capture timing constraints in an intuitive way.

## Nested Groups

To make more sophisticated scheduling constraints possible, it should be possible to tag a group itself with a group label.  Unfortunately "nesting" groups also invites the possibility of creating recursive groups.  "Group A" can be tagged with "Group B" and vice versa. It isn't clear what, if anything, should be done to prevent this other than to report any impossible scheduling constraints that result from nested groups.

## Group Types

Groups can be used to solve various problems in science program design.  In this section we attempt to enumerate the various options that will be needed.

* _Organizational Folders._  These are arbitrary groups of observations with no impact on scheduling.  They can be used to organize observations that are logically similar or in some way important to the user.

* _Any Groups._ This is an unordered group with a number (or possibly a percentage) parameter.  The idea is that *n* of the items in the group must be completed before the group is considered complete.  For example, this would cover the possibility of choosing amongst various options of identical value.

  - Putting two items in a group and requiring just one of them to be done is effectively an "or" group.

  - Setting n to the number of items in the group (or setting the percentage to 100 if that's the way these are done) would effectively result in an "and" group.

* _Timing Groups._  We express timing constraints between two observations and/or groups with a timing group.  A timing group has a min/max time bounds specifying how much time must or may pass between execution of two consecutive items.

  - It would be useful to extend this to *n* ordered items but it isn't clear to me how, beyond building up a hierarchy.

  - Maybe this is too simplistic.  Are there other types of timing constraints besides min / max time bounds?

  - Explicit ordering of items without any additional timing consideration could be represented via a timing group with no max time bounds and a min time bounds of 0.

## Acquisitions and Telluric Standards

Two common group use cases that have been mentioned in the past are telluric standard stars and acquisition observations. After discussion with Bryan and Andy we have concluded that neither warrants special group types.

### Acquisitions

The need for separate acquisition observations is a reflection of poor modeling choices in the existing codebase.  In particular they are necessary so that (differently configured) acquisition and science observations can be found by the OT browser which only takes into account the static instrument configuration.  In the new model, the acquisition sequence can just be included with the science sequence in the same observation and the need for separate observations and therefore a grouping of some kind disappears.

### Telluric Standards 

Standard star observations must be paired with a corresponding science observation and observed just before or just after the science observation at roughly the same declination and average airmass, hinting at the need to place timing constraints between the two.  Fortunately the proper telluric standard for an observation can be calculated on demand, the observation created, and a timing constraint generated without human intervention.  We envision that no more user input than marking an observation as needing a telluric standard should be necessary.
