import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Asta {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "5001");
        profile.setParameter(Profile.GUI, "true");
        profile.setParameter(Profile.NO_MTP, "true");
        profile.setParameter(Profile.CONTAINER_NAME, "Asta");

        try {
            AgentContainer mainContainer = rt.createMainContainer(profile);
            mainContainer.start();

            AgentController agentControllerAuctioneer = mainContainer.createNewAgent("Banditore", "Banditore", new Object[0]);
            agentControllerAuctioneer.start();

            String[] agentNames = {"Compratore1", "Compratore2", "Compratore3"};

            for (String agentName : agentNames) {
                AgentController agentControllerBuyer = mainContainer.createNewAgent(agentName, "Compratore", new Object[0]);
                agentControllerBuyer.start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
