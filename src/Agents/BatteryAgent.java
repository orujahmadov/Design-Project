package Agents;

import Enums.ChargingStation;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class BatteryAgent extends Agent {

    //BATTERY STATES
    private static int UNPLUGGED = 0;
    private static int PLUGGED_CHARGING = 1;
    private static int PLUGGED_FULL = 2;

    private int currentState;

    // The current charge of the battery in percent
    private int currentCharge;

    //Current station battery connected
    private ChargingStation currentStation;


    protected void setup() {

        registerThisAgent();
        addRandomBatteryStation();
        addRandomWeight();
        addRandomBatteryState();
        addRandomChargePercent();

        // Printout a welcome message
        System.out.println("Battery " + getAID().getName() + " is ready.");

        //THIS BEHAVIOUR MANUALLY CHARGING BATTERY + 1% in every 6 seconds for public, 4% for private
        addBehaviour(new TickerBehaviour(this, 9000) {
            @Override
            protected void onTick() {
                if (currentState == UNPLUGGED) {
                    currentCharge += 0;
                } else if (currentState == PLUGGED_CHARGING) {
                    if (currentCharge == 100) {
                        setBatteryState(PLUGGED_FULL);
                    } else {
                        currentCharge += currentStation == ChargingStation.PUBLIC ? 5 : 1;
                    }
                } else if (currentState == PLUGGED_FULL) {
                    //if battery charged fully, it might be unplugged right away or hold in that state for a bit
                    //we will assign 50/50 probability to this case
                    Random random = new Random();
                    if (random.nextInt(2) == 0) {
                        setBatteryState(UNPLUGGED);
                    } else {
                        //hold on this state
                    }

                }
            }
        });

        //THIS BEHAVIOUR MANUALLY DESCHARGING BATTERY - 1% in every 24 seconds
        addBehaviour(new TickerBehaviour(this, 48000) {
            @Override
            protected void onTick() {
                if (currentState == UNPLUGGED && currentCharge > 0) {
                    currentCharge -= 1;
                } else if (currentCharge == 0) {
                    setBatteryState(PLUGGED_CHARGING);
                }
            }
        });


        addBehaviour(new ResponseAvailableFlexibility());

    }

    private void registerThisAgent() {

        // Register the battery agent in the yellow pages
        AMSAgentDescription dfd = new AMSAgentDescription();
        dfd.setName(getAID());
        try {
            AMSService.register(this, dfd);
        } catch (FIPAException fe) {
//            fe.printStackTrace();
        }
    }

    private void addRandomWeight() {
        Random random = new Random();
    }

    private void addRandomChargePercent() {
        Random random = new Random();
        currentCharge = random.nextInt(100);
    }

    private void addRandomBatteryState() {
        Random random = new Random();
        currentState = random.nextInt(3);
        setBatteryState(currentState);
    }

    private void addRandomBatteryStation() {
        Random random = new Random();
        int r = random.nextInt(2);
        setBatteryStation(r);
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
                    response += currentState == PLUGGED_CHARGING ? "PLUGGED_CHARGING:"+currentCharge : "PLUGGED_FULL:100";
                } else if (currentState == UNPLUGGED) {
                    replyMessage.setPerformative(ACLMessage.CANCEL);
                    response += "UNPLUGGED:x";
                } else {
                    System.out.println("WTF");
                }
                replyMessage.setContent(response);
                myAgent.send(replyMessage);
            } else {
                block();
            }
        }
    }

    private int getRemainingChargeTime() {

        int remainingTime = 0;

        if (currentState == PLUGGED_FULL) {
            remainingTime = 0;
        } else if (currentState == PLUGGED_CHARGING) {
            remainingTime = (100 - currentCharge) * ((currentStation == ChargingStation.PRIVATE)?15:60);
        }

        return remainingTime;
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
                this.currentCharge = 100;
                break;
        }
    }

    private void setBatteryStation(int random) {
        if (random == 1) {
            this.currentStation = ChargingStation.PUBLIC;
        } else {
            this.currentStation = ChargingStation.PRIVATE;
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            AMSService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("Battery-agent " + getAID().getName() + " terminating.");
    }


}
