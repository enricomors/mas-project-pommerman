/*===========================*/
/*      INITIAL BELIEFS      */
/*===========================*/

alive.
blast_strenght(1).
ammo(1). // number of bombs

/*=======*/
/* RULES */
/*=======*/

// returns the number of enemies alive
enemies_alive(NE) :- .count(alive(A), NE).

// returns true if there are enemies in the visual field
enemies_in_visual_field(Enemies) :- .findall(cell(X,Y),cell(X,Y,enemy),Enemies) & 
                                    .length(Enemies, NE) & NE > 0.

bombs_in_visual_field(Bombs) :- .findall(cell(X,Y,Bs),cell(X,Y,bomb(Bs)),Bombs) &
                                .length(Bombs, NB) & NB > 0.

walls_in_visual_field(Walls) :- .findall(cell(X,Y),cell(X,Y,wooden_wall),Walls) &
                                .length(Walls, NW) & NW > 0.                         

powerup_in_visual_field(Powerups) :- .findall(cell(X,Y),cell(X,Y,inc_blast),Blast) &
                                     .findall(cell(X,Y),cell(X,Y,inc_bombs),Bombs) &
                                     .concat(Blast,Bombs,Powerups) &
                                     .length(Powerups, NP) & NP > 0.

// checks whether two positions are adjacent
adjacent(X,Y,X1,Y1) :- 
    (X1 == X+1 & Y1 == Y) |
    (X1 == X-1 & Y1 == Y) |
    (X1 == X & Y1 == Y+1) |
    (X1 == X & Y1 == Y-1).

/*===========================*/
/*       INITIAL GOALS       */
/*===========================*/

/*!act.*/
!initialize.

/*===========================*/
/*          PLANS            */
/*===========================*/

/* Sends broadcast message to other agents saying you're alive */ 
@alive[atomic]
+alive : true <- .broadcast(tell,im_alive).

/* Received message from other agent who is alive */
@enemy_alive[atomic] 
+im_alive[source(A)] <- 
    +alive(A).

@enemy_dead[atomic]
+im_dead[source(A)] : alive <-
    -alive(A);
    !check_game_over.

/* I've received im_alive message from all 3 enemies, the game can start */
+!initialize : enemies_alive(NE) & NE == 3 <-
    // .print("LET THE GAMES BEGIN");
    +game_started.

/* I've not received the im_alive message from all 3 enemies, wait some more */
+!initialize : not enemies_alive(NE) | enemies_alive(NE) & NE \== 3 <-
    // .print("Wait for other agents to send the im_alive message");
    .wait("+im_alive(A)", 500);
    !initialize.

/* Initialize fail handling */
-!initialize : true <-
    // .print("!initialize failed, retry");
    !initialize.

/* Agent dies due to fire */
@ouch[atomic]
+cell(X,Y,fire) : pos(X,Y) <-
    .print("********* I LOST! *********");
    -alive;
    .drop_all_intentions;
    .drop_all_events;
    .broadcast(tell,im_dead);
    remove_agent(X,Y).

/* Check if the game is over after an agent dies */
+!check_game_over : alive & enemies_alive(NE) & NE == 0 <-
    .print("********* I WON! *********");
    .drop_all_intentions;
    .drop_all_events;
    .stopMAS.


+!check_game_over : enemies_alive(NE) & NE > 0 <- 
    .print("Enemies still alive. Continue the game.").

/** 
* Start of the game:
* unlikely to have enemies around, just check for the nearest wooden wall
* and aim for it.
*/
+game_started : enemies_alive(NE) & alive & pos(X,Y) <- // & game_started
    jia.compute_distances(X,Y,Result);
    Result = [Dist,Prev,Items];
    !check_wooden_wall_reachable(X,Y,Dist,Prev,Items).

/**
* The agent just moved in a new position:
* check if the new position has a power-up on it and if it is safe.
* If position is not safe, move again and repeat
* Otherwise, check if we can drop a bomb
* If we cannot drop a bomb, look for reachable objects 
* If not, move randomly to a new position */
+!check_new_pos : game_started & enemies_alive(NE) & alive & pos(X,Y) <-
    // .print("check_new_pos");
    jia.compute_distances(X,Y,Result);
    Result = [Dist,Prev,Items];
    !check_cell_has_powerup(X,Y);
    !check_unsafe_pos(X,Y,Dist,Prev,Items).

/* Ricerca del percorso: A*. */
+!check_cell_has_powerup(X,Y) : cell(X,Y,inc_blast) & blast_strenght(S) <-
    // .print("check_cell_has_powerup");
    pick_powerup(X,Y,inc_blast);
    // .print("Blast strenght increased by 1");
    -+blast_strenght(S + 1).

+!check_cell_has_powerup(X,Y) : cell(X,Y,inc_bombs) & ammo(A) <-
    // .print("check_cell_has_powerup");
    pick_powerup(X,Y,inc_bombs);
    // .print("Ammo increased by 1");
    -+ammo(A + 1).

-!check_cell_has_powerup(X,Y) : true <-
    .print("No powerup on current pos").

+!check_unsafe_pos(X,Y,Dist,Prev,Items) : jia.is_position_unsafe(X,Y,Items,Dist,Directions) & .length(Directions, D) & D > 0 <- 
    // .print("check_unsafe_pos");
    jia.find_safe_direction(X,Y,Directions,Items,[H|T]);
    do(H);
    // after moving we need to check new position
    !check_new_pos.

-!check_unsafe_pos(X,Y,Dist,Prev,Items) : true <- 
    // .print("Position is safe!");
    !check_bomb_dropped;
    !check_enemy_adjacent(X,Y,Dist,Prev,Items).

+!check_enemy_adjacent(X,Y,Dist,Prev,Items) : cell(XE,YE,enemy) & adjacent(X,Y,XE,YE) & blast_strenght(S) <-
    // .print("check_enemy_adjacent");
    !maybe_bomb(X,Y,S,Dist,Prev,Items).

-!check_enemy_adjacent(X,Y,Dist,Prev,Items) : true <- 
    // .print("Enemy not adjacent");
    !check_enemy_reachable(X,Y,Dist,Prev,Items).

+!check_enemy_reachable(X,Y,Dist,Prev,Items) : enemies_in_visual_field(Enemies) & jia.is_object_in_radius(X,Y,Dist,Enemies,3,E) & .length(E,NE) & NE > 0 <- // 
    // .print("check_enemy_reachable");
    jia.get_direction_to_object(X,Y,E,Dist,Prev,Dir);
    do(Dir);
    // after moving we need to check new position
    !check_new_pos.

-!check_enemy_reachable(X,Y,Dist,Prev,Items) : true <- 
    // .print("Enemy is not reachable");
    !check_powerup_reachable(X,Y,Dist,Prev,Items).

+!check_powerup_reachable(X,Y,Dist,Prev,Items) : powerup_in_visual_field(Powerups) & jia.is_object_in_radius(X,Y,Dist,Powerups,2,P) & .length(P,NP) & NP > 0 <-
    // .print("check_powerup_reachable");
    jia.get_direction_to_object(X,Y,P,Dist,Prev,Dir);
    do(Dir);
    // after moving we need to check new position
    !check_new_pos.

-!check_powerup_reachable(X,Y,Dist,Prev,Items) : true <- 
    // .print("No powerup reachable");
    !check_wooden_wall_adjacent(X,Y,Dist,Prev,Items).

+!check_wooden_wall_adjacent(X,Y,Dist,Prev,Items) : cell(XW,YW,wooden_wall) & adjacent(X,Y,XW,YW) & blast_strenght(S) <- 
    // .print("wooden_wall_adjacent");
    !maybe_bomb(X,Y,S,Dist,Prev,Items).

-!check_wooden_wall_adjacent(X,Y,Dist,Prev,Items) : true <- 
    // .print("No adjacent wooden wall.");
    !check_wooden_wall_reachable(X,Y,Dist,Prev,Items).

+!check_wooden_wall_reachable(X,Y,Dist,Prev,Items) : walls_in_visual_field(Walls) & jia.is_object_in_radius(X,Y,Dist,Walls,3,W) & .length(W,NW) & NW > 0<-
    // .print("check_wooden_wall_reachable");
    /* Find path to reach wooden wall */
    jia.get_direction_to_object(X,Y,W,Dist,Prev,Dir);
    do(Dir);
    // after moving we need to check new position
    !check_new_pos.

-!check_wooden_wall_reachable(X,Y,Dist,Prev,Items) : true <- 
    // .print("No wooden wall reachable");
    !move_randomly.

+!maybe_bomb(X,Y,S,Dist,Prev,Items) : ammo(A) & A > 0 & jia.is_safe_to_drop(X,Y,S,Dist,Items) <-
    // .print("maybe_bomb");
    drop_bomb(X,Y,S);
    +placed_bomb(X,Y,S);
    -+ammo(A - 1);
    jia.my_id(Id);
    .concat("detonator", Id, D);
    .send(D,tell,placed_bomb(X,Y,S));
    // check position after placing bomb (updating dist and items)
    !check_new_pos.

-!maybe_bomb(X,Y,S,Dist,Prev,Items) : true <-
    // .print("Cannot drop bomb - No ammo");
    do(skip);
    !check_new_pos.

+!check_bomb_dropped : placed_bomb(Xb,Yb,S) <-
    // .print("detonate bomb");
    jia.my_id(Id);
    .concat("detonator", Id, D);
    .send(D,achieve,detonate_bomb(Xb,Yb,S)).

-!check_bomb_dropped : not placed_bomb(_,_,_) <-
    .print("no bombs placed").

@bomb_exp[atomic]
+bomb_exploded(X,Y,S) : ammo(A) <-
    // .print("bomb in ",X,",",Y," exploded");
    -placed_bomb(X,Y,S);
    // update ammo
    // .print("Ammo increase by 1");
    -+ammo(A + 1).

+!move_randomly : alive & pos(X,Y) <-
    // .print("move_randomly");
    jia.get_random_direction(X,Y,Direction);
    // .print("do(",Direction,")");
    do(Direction);
    !check_new_pos.
