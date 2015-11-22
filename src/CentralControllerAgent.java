import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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

    //list of all known battery agents
    private AID[] batteryAgents;

    private String[][] batteryStates;

    protected void setup() {

        // Printout a welcome message
        System.out.println("Yo, wassup? Controller " + getAID().getName() + " is ready.");

        //GET LIST OF BATTERY AGENTS
        addBehaviour(new WakerBehaviour(this, 15000) {
            @Override
            protected void handleElapsedTimeout() {
                updateBatteryAgents(myAgent);
            }
        });
        // Perform the request
        addBehaviour(new RequestAvailableFlexibility(this, 30000));
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

    public class RequestAvailableFlexibility extends WakerBehaviour {


        public RequestAvailableFlexibility(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        public void handleElapsedTimeout() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            for (int i = 0; i < batteryAgents.length; i++) {
                message.addReceiver(batteryAgents[i]);
            }
            message.setContent("Can you shut down?");
            myAgent.send(message);
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
                System.out.println(batteryStates[counter][0]+" is in state " + batteryStates[counter][1]);
                counter++;
            }
        }

        @Override
        public boolean done() {
            if (batteryStates == null) {
                return false;
            } else {
                return counter == batteryStates.length;
            }
        }
    }
}
