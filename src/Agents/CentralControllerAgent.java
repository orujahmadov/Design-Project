package Agents;

import GUI.ControllerGUI;
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
import jfreeChart.ChartFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RefineryUtilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CentralControllerAgent extends Agent {

    //BOOLEAN VARIABLE USED TO CHECK WHETHER ALL RESPONSES RECEIVED FROM BATTERIES
    boolean done = true;
    boolean started = false;
    boolean addCurrentChargeToMessage = false;
    boolean addPriorityToMessage = false;

    //indicate the number of battery agents in network
    int agents = 0;
    int numberOfSamples = 0;
    int samplePeriod;

    //list of all known battery agents
    private AID[] batteryAgents;
    private String[][] batteryStates;
    private ControllerGUI controllerGUI;
    private int bufferCounter = 1;

    XYSeries xySeries = new XYSeries("demo");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH.mm");

    protected void setup() {

        // Printout a welcome message
        System.out.println("hey, I " + getAID().getName() + " is ready.");

        //GET LIST OF BATTERY AGENTS
        addBehaviour(new WakerBehaviour(this, 10000) {
            @Override
            protected void handleElapsedTimeout() {
                updateBatteryAgents(myAgent);
            }
        });

        controllerGUI = new ControllerGUI(this);
        controllerGUI.showGUI();

        addBehaviour(new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                if (started) {
                    myAgent.addBehaviour(new RequestAvailableFlexibility(myAgent, samplePeriod * 1000));
                    started = false;
                }
            }
        });
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
        } catch (FIPAException fe) {
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
            if (done == true && bufferCounter != -1) {
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
            } else {
                if (counter == batteryStates.length) {
                    done = true;
                    int disconnectedBatteries = 0;
                    for (int i = 0; i < batteryStates.length; i++) {
                        if (batteryStates[i][1] != null) {
                            if (batteryStates[i][1].split(":")[0].equals("PLUGGED_FULL")) {
                                disconnectedBatteries++;
                            } else if (batteryStates[i][1].split(":")[0].equals("PLUGGED_CHARGING")) {
                                if (addCurrentChargeToMessage && addPriorityToMessage) {
                                    if (Integer.parseInt(batteryStates[i][1].split(":")[1]) > 50 && Integer.parseInt(batteryStates[i][1].split(":")[2]) < 5) {
                                        disconnectedBatteries++;
                                    }
                                } else if (addCurrentChargeToMessage && !addPriorityToMessage) {
                                    if (Integer.parseInt(batteryStates[i][1].split(":")[1]) > 50) {
                                        disconnectedBatteries++;
                                    }
                                } else if (!addCurrentChargeToMessage && !addPriorityToMessage) {
                                    disconnectedBatteries ++;
                                }
                            }
                        }
                    }

                    Double currentTime = Double.parseDouble(simpleDateFormat.format(new Date())) * 100;
                    xySeries.add(new Double(currentTime), new Double(disconnectedBatteries));
                    controllerGUI.getLabel().setText("Remaining Time: " + (numberOfSamples - bufferCounter));
                    if (bufferCounter >= numberOfSamples) {
                        bufferCounter = -1;
                        displayChart(xySeries);
                    } else {
                        bufferCounter++;
                    }
                }
            }
            return done;
        }
    }

    private static void displayChart(XYSeries xy) {
        ChartFrame chart = new ChartFrame("demo", xy);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }

    public void setNumberOfSamples(int numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setSamplePeriod(int samplePeriod) {
        this.samplePeriod = samplePeriod;
    }

    public void setCurrentCharge(boolean addCurrentChargeToMessage) {
        this.addCurrentChargeToMessage = addCurrentChargeToMessage;
    }

    public void setPriority(boolean addPriorityToMessage) {
        this.addPriorityToMessage = addPriorityToMessage;
    }

}
