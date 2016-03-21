package GUI;

import Agents.CentralControllerAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ControllerGUI extends JFrame {

    JLabel remainingTime;
    JFrame mainFrame;

    public ControllerGUI(Agent agent) {
        super(agent.getLocalName());

             /*
        Number of Samples,
        Frequency of Samples,
        Information to exchange,
        Remaining Time,
        Start Button,
        Percentage to disconnect
         */
        mainFrame = new JFrame("test");
        mainFrame.setLayout(new GridLayout(4,1));


        //Header panel
        JPanel headerPanel = new JPanel(new GridLayout(2,1));
        JLabel headerLabel = new JLabel("CONTROLLER");
        JLabel headerTextLabel = new JLabel("Cusotmize simulation here");
        headerPanel.add(headerLabel);
        headerPanel.add(headerTextLabel);

        //SAMPLING INFO PANEL
        JPanel samplesPanel = new JPanel(new GridLayout(2,2));
        JLabel numberOfSamples = new JLabel("Number of samples");
        JTextField samplesTextField = new JTextField();
        samplesTextField.setMaximumSize(new Dimension(10,3));
        JLabel frequencyOfSamples = new JLabel("Frequency of samples (sec)");
        JTextField frequencyTextField = new JTextField();
        frequencyTextField.setMaximumSize(new Dimension(10,3));
        samplesPanel.add(numberOfSamples);
        samplesPanel.add(samplesTextField);
        samplesPanel.add(frequencyOfSamples);
        samplesPanel.add(frequencyTextField);

        //INFO TO EXCHANGE
        JPanel infoPanel = new JPanel(new GridLayout(2,2));
        JLabel currentcharge = new JLabel("Current charge");
        JCheckBox samplesCheckbox = new JCheckBox();
        JLabel chargingtime = new JLabel("Remaining charging time");
        JCheckBox chargingCheckbox = new JCheckBox();
        infoPanel.add(currentcharge);
        infoPanel.add(samplesCheckbox);
        infoPanel.add(chargingtime);
        infoPanel.add(chargingCheckbox);

        JPanel controlPanel = new JPanel(new GridLayout(2,1));
        JButton startButton = new JButton("Start");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((CentralControllerAgent)agent).setNumberOfSamples(Integer.parseInt(samplesTextField.getText()));
                ((CentralControllerAgent)agent).setSamplePeriod(Integer.parseInt(frequencyTextField.getText()));
                ((CentralControllerAgent)agent).setStarted(true);
                startButton.setText("STARTED");
            }
        });

        remainingTime = new JLabel("Remaining Time: ");
        controlPanel.add(startButton);
        controlPanel.add(remainingTime);


        mainFrame.add(headerPanel);
        mainFrame.add(samplesPanel);
        mainFrame.add(infoPanel);
        mainFrame.add(controlPanel);


    }

    public void showGUI() {
        mainFrame.pack();
        mainFrame.setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
    }

    public JLabel getLabel() {
        return remainingTime;
    }
}
