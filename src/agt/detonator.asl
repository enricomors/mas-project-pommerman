// Agent detonator in project 

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!detonate_bomb(X,Y,S) : placed_bomb(X,Y,S) <- 
    // .print("Received order to detonate bomb in (",X,",",Y,")");
    detonate_bomb(X,Y,S);
    -placed_bomb(X,Y,S);
    remove_fire(X,Y,S);
    // retrieve id
    jia.my_id(Id);
    // get agent name
    .concat("bomber", Id, B);
    .send(B,tell,bomb_exploded(X,Y,S)).

-!detonate_bomb(X,Y,S) : true <-
    .print("Detonate bomb failed for some reason").