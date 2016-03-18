package GUI;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControllerGUI extends JFrame {

    JLabel label;
    public ControllerGUI(Agent agent, Behaviour behaviour) {
        super(agent.getLocalName());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JButton startButton = new JButton("Request available flexibility");
        startButton.setBounds(50, 60, 80, 30);
        label = new JLabel("TEST");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agent.addBehaviour(behaviour);
                label.setText("STARTED");
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
