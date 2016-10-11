package Ui;

import Model.MessageSender;
import Model.Vpn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by karui on 2016-10-03.
 */
public class VpnUi {
    private final int APP_WIDTH = 360;
    private final int APP_HEIGHT = 560;
    private final int MESSAGE_LEN = 100;
    private final String DEFAULT_PORT = "8080";
    private final String DEFAULT_ADDRESS = "localhost";
    private final String TEST_KEY = "1234567890123456";

    private JFrame applicationFrame;
    private JLayeredPane layeredPane;
    private JPanel portNumPanel;
    private JPanel identityPanel;
    private JPanel ipPanel;
    private JPanel messagePanel;

    private VpnUi() {}

    public static VpnUi getInstance() {
        if (Vpn.getVpnUi() == null) {
            return new VpnUi();
        }
        return Vpn.getVpnUi();
    }

    public void createAndShowGui() {
        initializeApplicationFrame();
        populateApplicationFrame();
        addPortNumberAndKey();
        addIdentity();
        addIp();
        addMessage();
        applicationFrame.setVisible(true);
    }

    private void initializeApplicationFrame() {
        applicationFrame = new JFrame();
        applicationFrame.getContentPane().setForeground(Color.WHITE);
        applicationFrame.getContentPane().setBackground(Color.WHITE);
        applicationFrame.setSize(new Dimension(APP_WIDTH, APP_HEIGHT));
        applicationFrame.setResizable(false);
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void populateApplicationFrame() {
        layeredPane = new JLayeredPane();
        portNumPanel = new JPanel();
        identityPanel = new JPanel();
        ipPanel = new JPanel();
        messagePanel = new JPanel();

        // TODO: beautify layouts
        createLeftAlignedPanel(portNumPanel);
        createLeftAlignedPanel(identityPanel);
        createLeftAlignedPanel(messagePanel);
        createLeftAlignedPanel(ipPanel);

        // portNumPanel is the first panel to be displayed to the user
        portNumPanel.setVisible(true);

        layeredPane.add(portNumPanel, 1);
        layeredPane.add(identityPanel, 2);
        layeredPane.add(ipPanel, 3);
        layeredPane.add(messagePanel, 4);

        applicationFrame.getContentPane().add(layeredPane);
    }

    private void createLeftAlignedPanel(JPanel panel) {
        panel.setAlignmentX(SwingConstants.LEFT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setVisible(false);
        panel.setSize(new Dimension(APP_WIDTH, APP_HEIGHT - 50));
    }

    private void addPortNumberAndKey() {
        JLabel portNumLabel = new JLabel("Enter port number: ");
        portNumLabel.setFont(new Font("Helvetica", Font.PLAIN, 24));
        portNumLabel.setForeground(new Color(51, 51, 51));

        JTextField portNumField = new JTextField(15);
        portNumField.setText(DEFAULT_PORT);
        portNumField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();
            }
        });
        portNumField.setVisible(true);

        portNumPanel.add(portNumLabel, BorderLayout.CENTER);
        portNumPanel.add(portNumField);

        JLabel label = new JLabel("Enter key: ");
        label.setFont(new Font("Helvetica", Font.PLAIN, 24));
        label.setForeground(new Color(51, 51, 51));

        JTextField textField = new JTextField(15);
        textField.setText(TEST_KEY);
        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();
            }
        });
        textField.setVisible(true);

        JButton button = new JButton("Continue");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vpn.getVpnManager().receivePort(Integer.parseInt(portNumField.getText()));
                Vpn.getVpnManager().receiveSecret(textField.getText());
                portNumPanel.setVisible(false);
                identityPanel.setVisible(true);
            }
        });

        portNumPanel.add(label, BorderLayout.CENTER);
        portNumPanel.add(textField);
        portNumPanel.add(button);
    }

    private void addIdentity() {
        JLabel label = new JLabel("I am a ");
        label.setFont(new Font("Helvetica", Font.PLAIN, 24));
        label.setForeground(new Color(51, 51, 51));

        JButton serverButton = new JButton("Server");
        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vpn.getVpnManager().initializeServer();
                identityPanel.setVisible(false);
                messagePanel.setVisible(true);
            }
        });

        JButton clientButton = new JButton("Client");
        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                identityPanel.setVisible(false);
                ipPanel.setVisible(true);
            }
        });

        identityPanel.add(label);
        identityPanel.add(serverButton);
        identityPanel.add(clientButton);
    }

    private void addIp() {
        JLabel label = new JLabel("Enter IP Address: ");
        JTextField textField = new JTextField(15); // TODO CHECK IP LENGTH
        textField.setText(DEFAULT_ADDRESS);
        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();
            }
        });
        textField.setVisible(true);

        JButton button = new JButton("Continue");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vpn.getVpnManager().receiveIp(textField.getText());
                Vpn.getVpnManager().initializeClient();
                ipPanel.setVisible(false);
                messagePanel.setVisible(true);
            }
        });

        ipPanel.add(label);
        ipPanel.add(textField);
        ipPanel.add(button);
    }

    private void addMessage() {
        JTextField textField = new JTextField(MESSAGE_LEN);
        textField.setText("Enter your text here");
        textField.setVisible(true);

        JButton button = new JButton("Send");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("=================");
                new MessageSender(textField.getText()).sendText();
                System.out.println("=================");
            }
        });

        messagePanel.add(textField);
        messagePanel.add(button);
    }
}
