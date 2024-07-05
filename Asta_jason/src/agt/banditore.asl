/** Initial beliefs and rules*/
interlocutor(compratore1).
interlocutor(compratore2).
interlocutor(compratore3).
object(vaso).
object(quadro).
object(orologio).

/** Initial goals */
!init.

/** Plans*/
+!init
  <- .print("Auction started for object 'vaso'");
     !start_auction(vaso);
     .wait(3000);
     .print("Auction started for object 'quadro'");
     !start_auction(quadro);
     .wait(3000);
     .print("Auction started for object 'orologio'");
     !start_auction(orologio).

// Inizializza l'asta
+!start_auction(O) 
  <- .broadcast(tell, start_auction(O));
     -step(1);
     !collect_bids.

+!collect_bids
  <- .wait(5000);
     .print("The time to send bid has expired");
     !find_winner.

+bid(Price)[source(Ag)]
  <- .print("Received bid: ", Price, " from ", Ag);
     +bid_received(Price, Ag).

+!find_winner
  <- .findall(bid_received(Price, Ag), bid_received(Price, Ag), Bids);
     .length(Bids, Len);
     .print("Bids received: ", Len);
     if (Len > 1) {
       .sort(Bids, SortedBids);
       .reverse(SortedBids, [bid_received(HighestBid, Winner) | Rest]);
       Rest = [bid_received(SecondHighestBid, _) | _];
       !comunicate_result(Winner, SecondHighestBid);
     } else {
       .print("Not enough bids received.");
       .broadcast(tell, auction_failed);
     }.

+!comunicate_result(C, P) : interlocutor(C)
  <- .send(C, tell, auction_winner(C, P));
     !clear_bids.

+!clear_bids
  <- .findall(bid_received(Price, Ag), bid_received(Price, Ag), Bids);
     !clear_each_bid(Bids).

+!clear_each_bid([])
  <- .print("All bids cleared.").

+!clear_each_bid([bid_received(Price, Ag) | Rest])
  <- -bid_received(Price, Ag);
     !clear_each_bid(Rest).
