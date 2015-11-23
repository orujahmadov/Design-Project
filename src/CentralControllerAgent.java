import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class CentralControllerAgent extends Agent {

    private double capacity = 0;
    private double overallDemand = 0;
    private boolean isDemandPeak = false;

    //BOOLEAN VARIABLE USED TO CHECK WHETHER ALL RESPONSES RECEIVED FROM BATTERIES
    boolean done = false;

    //indicate the number of battery agents in network
    int agents = 0;

    //list of all known battery agents
    private AID[] batteryAgents;

    private String[][] batteryStates;

    private ControllerGUI controllerGUI;

    protected void setup() {

        // Printout a welcome message
        System.out.println("Yo, wassup? Controller " + getAID().getName() + " is ready.");

        //GET LIST OF BATTERY AGENTS
        addBehaviour(new WakerBehaviour(this, 2000) {
            @Override
            protected void handleElapsedTimeout() {
                updateBatteryAgents(myAgent);
            }
        });

        controllerGUI = new ControllerGUI(this, new RequestAvailableFlexibility());
        controllerGUI.showGUI();
        // Perform the request
        addBehaviour(new GetBatteryStates());
    }

    private void updateBatteryAgents(Agent myAgent) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("power-modulation");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
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

    public class RequestAvailableFlexibility extends OneShotBehaviour {

        @Override
        public void action() {
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
                    if (batteryStates[i][1].equals("PLUGGED_FULL") || batteryStates[i][1].equals("PLUGGED_CHARGING")) {
                        disconnectedBatteries++;
                    }
                }
                controllerGUI.getLabel().setText(String.valueOf(disconnectedBatteries) + " batteries connected");
            }
            return done;
        }
    }
}
