// Agent bob in project pommerman

/* Initial beliefs and rules */

alive(self).

/* Initial goals */

!start.

/* Plans */

+!start : alive(self) <- .print("I'm alive!.").
