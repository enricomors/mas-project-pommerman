// Agent alice in project pommerman

/* Initial beliefs and rules */

alive(self).


/* GOALS */

!start.

!eliminate_enemies.

!move_to(bunker).


/* PLANS */

+!move_to(bunker) : not at(bunker) <- 
    move_agent;
    !move_to(bunker). 

+!start : alive(self) <- 
    .print("I'm alive!.").

+!eliminate_enemies : true <- 
    .print("I want to kill everybody.").
