import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Banditore extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello World. I am the auctioneer!");
        System.out.println("My local-name is " + getAID().getLocalName());
        System.out.println("My GUID is " + getAID().getName());

        addBehaviour(new AuctionBehaviour(this, 1000));
    }

    @Override
    protected void takeDown() {
        System.out.println("Banditore: " + getAID().getName() + " terminating.");
    }

    private class AuctionBehaviour extends TickerBehaviour {
        private int step = 0;
        private long startTime;
        private final List<Bid> bids = new ArrayList<>();
        private final List<String> auctionItems = new ArrayList<>();

        class Bid {
            private final AID bidder;
            private final int price;

            public Bid(AID bidder, int price) {
                this.bidder = bidder;
                this.price = price;
            }

            public AID getBidder() {
                return bidder;
            }

            public int getPrice() {
                return price;
            }
        }

        public AuctionBehaviour(Agent a, long period) {
            super(a, period);
            auctionItems.add("vaso");
            auctionItems.add("quadro");
            auctionItems.add("orologio");
            auctionItems.add("collana");
        }

        @Override
        protected void onTick() {
            switch (step) {
                case 0:
                    if (!(auctionItems.isEmpty())) {
                        startAuction(auctionItems.get(0));
                        step = 1;
                    }else{
                        terminateAuction();
                        myAgent.doDelete();
                    }
                    break;
                case 1:
                    collectBids();
                    break;
                case 2:
                    findWinner();
                    step = 3;
                    break;
                case 3:
                    clearBids();
                    auctionItems.remove(0);
                    step = 0;
                    break;
            }
        }

        private void startAuction(String obj) {
            System.out.println("Auction started for object '" + obj + "'");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("start_auction");
            msg.setConversationId("auction");
            msg.setReplyWith("start_auction");
            for (AID receiver : getReceivers()) {
                msg.addReceiver(receiver);
            }
            myAgent.send(msg);
            startTime = System.currentTimeMillis();
        }

        private void collectBids() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    int price = Integer.parseInt(msg.getContent());
                    bids.add(new Bid(msg.getSender(), price));
                    System.out.println("Received bid: " + price + " from " + msg.getSender().getLocalName());
                }
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > 5000) {
                System.out.println("The time to send bid has expired");
                step = 2;
            } else {
                block(1000);
            }
        }

        private void findWinner() {
            if (bids.size() > 1) {
                Collections.sort(bids, Comparator.comparingInt(Bid::getPrice));
                Bid highestBid = bids.get(bids.size() - 1);
                Bid secondHighestBid = bids.get(bids.size() - 2);
                communicateResult(highestBid, secondHighestBid);
                bids.remove(bids.size() -1);
                communicateResult();
            } else {
                System.out.println("Not enough bids received.");
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("auction_failed");
                msg.setConversationId("auction_failed");
                for (AID receiver : getReceivers()) {
                    msg.addReceiver(receiver);
                }
                myAgent.send(msg);
            }
        }

        private void communicateResult(Bid winner, Bid secondHighest) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("auction_winner(" + winner.getBidder().getLocalName() + ", " + secondHighest.getPrice() + ")");
            msg.setConversationId("auction_winner");
            msg.addReceiver(winner.getBidder());
            myAgent.send(msg);
        }

        private void communicateResult() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("auction_lose");
            msg.setConversationId("auction_lose");

            for (Bid receiver : bids) {
                msg.addReceiver(receiver.getBidder());
            }
            myAgent.send(msg);
        }

        private void clearBids() {
            bids.clear();
            System.out.println("All bids cleared.");
        }

        private void terminateAuction() {
            System.out.println("Auction terminated");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("terminate_auction");
            msg.setConversationId("auction");
            msg.setReplyWith("terminate_auction");
            for (AID receiver : getReceivers()) {
                msg.addReceiver(receiver);
            }
            myAgent.send(msg);
        }

        private List<AID> getReceivers() {
            List<AID> receivers = new ArrayList<>();
            receivers.add(new AID("Compratore1", AID.ISLOCALNAME));
            receivers.add(new AID("Compratore2", AID.ISLOCALNAME));
            receivers.add(new AID("Compratore3", AID.ISLOCALNAME));
            return receivers;
        }
    }
}
