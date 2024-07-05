import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class Compratore extends Agent {
    protected void setup() {
        System.out.println("Hello World. I'am a buyer!");
        System.out.println("My local-name is " + getAID().getLocalName());
        System.out.println("My GUID is " + getAID().getName());

        OneShotBehaviour createBudgetBehaviour = new CreateBudgetBehaviour();
        addBehaviour(createBudgetBehaviour);
    }

    protected void takeDown() {
        System.out.println("Compratore: " + getAID().getName() + " terminating.");
    }
}

class CreateBudgetBehaviour extends OneShotBehaviour {
    private int budget;
    private static final Random RANDOM = new Random();

    public static int getRandomInt(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    @Override
    public void action() {
        budget = getRandomInt(200, 1000);
        System.out.println(myAgent.getAID().getName() + ", initial budget: " + budget);
        myAgent.addBehaviour(new Auction(budget));
    }
}

class Auction extends CyclicBehaviour {
    private int budget;

    private int budgetPart;

    private int countVictory = 0;

    private AID auctioneerAgent = new AID("Banditore", AID.ISLOCALNAME);

    Auction(int budget) {
        this.budget = budget;
        this.budgetPart = CreateBudgetBehaviour.getRandomInt(1, 2);
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            String content = msg.getContent();
            switch (msg.getPerformative()) {
                case ACLMessage.INFORM:
                    if (content.equals("start_auction")) {
                        startAuction();
                    } else if (content.startsWith("auction_winner")) {
                        handleAuctionWinner(content);
                    } else if (content.equals("auction_failed")) {
                        handleAuctionFailed();
                    } else if (content.equals("auction_lose")){
                        handleAuctionLose();
                    } else if(content.equals("terminate_auction")){
                        myAgent.doDelete();
                    }
                    break;
                default:
                    break;
            }
        } else {
            block();
        }
    }

    private void startAuction() {
        if(budget > 0){
            int bid = budget;
            if(budgetPart > 1) {
                bid = bid / budgetPart;
                budgetPart--;
            }
            makeBid(bid);
        }else{
            myAgent.doDelete();
        }
    }

    private void makeBid(int bid) {
        //System.out.println("Sending bid to: " + auctioneerAgent.getLocalName() + ", my name is: " + myAgent.getLocalName());
        ACLMessage bidMsg = new ACLMessage(ACLMessage.PROPOSE);
        bidMsg.addReceiver(auctioneerAgent);
        bidMsg.setContent(String.valueOf(bid));
        bidMsg.setReplyWith("start_auction");
        myAgent.send(bidMsg);
        System.out.println("Submitted bid: " + bid + ", my name is: " + myAgent.getLocalName());
    }

    private void handleAuctionWinner(String content) {
        countVictory++;
        String[] parts = content.split(",");
        String winner = parts[0].split("\\(")[1].trim();
        String price = parts[1].split("\\)")[0].trim();
        budget = budget - Integer.parseInt(price);
        System.out.println("Auction won by: " + winner + ", with price: " + price + ", is my " + countVictory + " victory.");
        if(countVictory == 2) {
            myAgent.doDelete();
        }
    }

    private void handleAuctionFailed() {
        System.out.println("Auction failed: not enough bids");
    }

    private void handleAuctionLose() {
        System.out.println("I'm lose, my name is " + myAgent.getLocalName());
    }

}

