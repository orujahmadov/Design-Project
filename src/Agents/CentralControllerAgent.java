package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import GUI.ControllerGUI;

import Enums.ChargingStation;

public class CentralControllerAgent extends Agent {

    //BOOLEAN VARIABLE USED TO CHECK WHETHER ALL RESPONSES RECEIVED FROM BATTERIES
    boolean done = false;

    //indicate the number of battery agents in network
    int agents = 0;

    //list of all known battery agents
    private AID[] batteryAgents;

    private String[][] batteryStates;

    private ControllerGUI controllerGUI;

    private BufferedWriter out;

    private int bufferCounter = 0;

    protected void setup() {

        //CREATE TEXT FILE TO WRITE RESULTS
        try {
            out = new BufferedWriter(new FileWriter("simulation1.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Printout a welcome message
        System.out.println("hey, I " + getAID().getName() + " is ready.");

        //GET LIST OF BATTERY AGENTS
        addBehaviour(new WakerBehaviour(this, 10000) {
            @Override
            protected void handleElapsedTimeout() {
                updateBatteryAgents(myAgent);
            }
        });

        addBehaviour(new GetBatteryStates());
        controllerGUI = new ControllerGUI(this, new RequestAvailableFlexibility(this, 1000));
        controllerGUI.showGUI();
    }

    private void updateBatteryAgents(Agent myAgent) {
        AMSAgentDescription template = new AMSAgentDescription();
        SearchConstraints searchConstraints = new SearchConstraints();
        searchConstraints.setMaxResults(1000L);
        searchConstraints.setMaxDepth(1000L);
        try {
            AMSAgentDescription[] result = AMSService.search(myAgent, template, searchConstraints);
            System.out.println("Found the following battery agents:");
            batteryAgents = new AID[result.length];
            agents = result.length;
            batteryStates = new String[result.length][2];
            for (int i = 0; i < result.length; ++i) {
                batteryAgents[i] = result[i].getName();
                System.out.println(batteryAgents[i].getName());
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public class RequestAvailableFlexibility extends TickerBehaviour {

        public RequestAvailableFlexibility(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            for (int i = 0; i < batteryAgents.length; i++) {
                message.addReceiver(batteryAgents[i]);
            }
            message.setContent("Can you shut down?");
            myAgent.send(message);
            if (done == true) {
                done = false;
                batteryStates = new String[agents][2];
                addBehaviour(new GetBatteryStates());
            }
        }

    }


    private class GetBatteryStates extends Behaviour {

        int counter = 0;

        @Override
        public void action() {
            ACLMessage batteryFlexibility = myAgent.receive();
            if (batteryFlexibility != null) {
                batteryStates[counter][0] = batteryFlexibility.getSender().getName();
                batteryStates[counter][1] = batteryFlexibility.getContent();
                counter++;
            }
        }

        @Override
        public boolean done() {
            done = false;
            if (batteryStates == null) {
                done = false;
            } else if (counter == batteryStates.length) {
                done =  true;
                int disconnectedBatteries = 0;
                for (int i = 0; i < batteryStates.length; i++) {
                    if(batteryStates[i][1] != null) {
                        if (batteryStates[i][1].split(":")[0].equals("PLUGGED_FULL")) {
                            disconnectedBatteries++;
                        } else if (batteryStates[i][1].split(":")[0].equals("PLUGGED_CHARGING")) {
                            if (Integer.parseInt(batteryStates[i][1].split(":")[1]) > 50) {
                                disconnectedBatteries++;
                            }
                        }
                    }
                }

                try {
                    out.write(disconnectedBatteries+"\n");
                    if (bufferCounter > 1000 ) {
                        out.close();
                    } else {
                        bufferCounter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            return done;
        }
    }

}
