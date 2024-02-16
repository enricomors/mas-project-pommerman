// Agent alice in project pommerman

/* Initial beliefs and rules */

alive(self).

/* Initial goals */

!eliminate_enemies.

/* Plans */

+!eliminate_enemies 
    : true 
    <- .print("I want to kill everybody.").
