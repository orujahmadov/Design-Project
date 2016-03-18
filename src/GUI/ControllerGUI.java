package GUI;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ControllerGUI extends JFrame {

    JLabel label;
    JTextField textField;

    public ControllerGUI(Agent agent, Behaviour behaviour) {
        super(agent.getLocalName());

        JPanel panel = new JPanel();
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton startButton = new JButton("Request available flexibility");
        startButton.setBounds(50, 60, 80, 30);
        label = new JLabel("");

        JLabel sampleCountLabel = new JLabel("Number of Samples");
        textField = new JTextField(6);
        controlPanel.add(sampleCountLabel);
        controlPanel.add(textField);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agent.addBehaviour(behaviour);
                startButton.setText("STARTED");
            }
        });

        panel.add(startButton);
        panel.add(label);
        getContentPane().add(panel);


    }

    public void showGUI() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public JLabel getLabel() {
        return label;
    }
}
