import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class BatteryAgent extends Agent {

    //BATTERY STATES
    private static int UNPLUGGED = 0;
    private static int PLUGGED_CHARGING = 1;
    private static int PLUGGED_FULL = 2;
    private static int WAITING = 3;

    private int currentState;

    // The current charge of the battery in percent
    private int currentCharge;


    protected void setup() {

        registerThisAgent();
        addRandomBatteryState();
        addRandomChargePercent();

        // Printout a welcome message
        System.out.println("Battery " + getAID().getName() + " is ready.");

        //THIS BEHAVIOUR MANUALLY CHARGING BATTERY + 1% in every 6 seconds
        addBehaviour(new TickerBehaviour(this, 6000) {
            @Override
            protected void onTick() {
                if (currentState == UNPLUGGED || currentState == WAITING) {
                    currentCharge += 0;
                } else if (currentState == PLUGGED_CHARGING) {
                    if (currentCharge == 100) {
                        currentCharge+=0;
                        setBatteryState(PLUGGED_FULL);
                    } else {
                        currentCharge+=1;
                    }
                }
            }
        });


        addBehaviour(new ResponseAvailableFlexibility());

    }

    private void registerThisAgent() {

        // Register the battery agent in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("power-modulation");
        sd.setName("DemandResponse");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void addRandomChargePercent() {
        Random random = new Random();
        currentCharge = random.nextInt(100);
    }

    private void addRandomBatteryState() {
        Random random = new Random();
        currentState = random.nextInt(4);
        setBatteryState(currentState);
    }


    public class ResponseAvailableFlexibility extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage message = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            if (message != null) {
                ACLMessage replyMessage = message.createReply();
                String response = "";
                if (currentState == PLUGGED_CHARGING || currentState == PLUGGED_FULL) {
                    replyMessage.setPerformative(ACLMessage.AGREE);
                    response+=currentState==PLUGGED_CHARGING?"PLUGGED_CHARGING":"PLUGGED_FULL";
                } else if (currentState == UNPLUGGED || currentState == WAITING) {
                    replyMessage.setPerformative(ACLMessage.CANCEL);
                    response+=currentState==UNPLUGGED?"UNPLUGGED":"WAITING";
                }
                replyMessage.setContent(response);
                myAgent.send(replyMessage);
            } else {
                block();
            }
        }
    }

    private void setBatteryState(int state) {
        switch (state) {
            case 0:
                this.currentState = UNPLUGGED;
                break;
            case 1:
                this.currentState = PLUGGED_CHARGING;
                break;
            case 2:
                this.currentState = PLUGGED_FULL;
                break;
            case 3:
                this.currentState = WAITING;
                break;
        }
    }

    private int getBatteryState(){
        return currentState;
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("Battery-agent "+getAID().getName()+" terminating.");
    }

}
