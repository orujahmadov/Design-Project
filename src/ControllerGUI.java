import jade.core.Agent;
import jade.core.behaviours.Behaviour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControllerGUI extends JFrame {
    ControllerGUI(Agent agent, Behaviour behaviour) {
        super(agent.getLocalName());

        JPanel panel = new JPanel();
        JButton startButton = new JButton("Request available flexibility");
        startButton.setBounds(50, 60, 80, 30);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agent.addBehaviour(behaviour);
            }
        });
        panel.add(startButton);
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
}
