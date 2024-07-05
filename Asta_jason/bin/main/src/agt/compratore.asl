/** Initial beliefs and rules*/
interlocutor(banditore).

/** Initial goals */

/** Plans*/
+!create_budget
  <- .random(Value, 1000);
     Budget = math.floor(Value * 1000);
     +budget(Budget);
     .print("Initial budget: ", Budget).

// Partecipa all'asta
+start_auction(O)[source(B)]
    <- !create_budget;
       !make_bid(B).

+!make_bid(B) : interlocutor(B) & budget(X) & X > 0
    <- .print("Sending bid to: ", B);
       .send(B, tell, bid(X));
       .print("Submitted bid: ", X).

// Ricevi i risultati dell'asta
+auction_winner(Winner, Price)[source(B)]
  <- .print("Auction won by: ", Winner, ", with price: ", Price).

+auction_failed[source(B)]
  <- .print("Auction failed: not enough bids").