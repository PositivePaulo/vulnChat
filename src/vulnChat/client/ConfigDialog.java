package vulnChat.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


/**
 * Defines a window inheriting from {@link JFrame} in order to make a configuration window
 * for a {@link Client} to set a window title, an IP address and a port number to connect to.
 * @author Paul Mabileau
 * @version 0.1
 */
public class ConfigDialog extends JFrame {
	private static final long serialVersionUID = 907344703389239762L;
	
	/**
	 * Builds the desired configuration dialog with a bit of customization available:
	 * @param client - the calling {@link Client} instance
	 * @param title - the window's title as a {@link String}
	 * @param defaultIP - the IP address as a {@link String} that will be put by default in the corresponding {@link JTextField} upon startup
	 * @param defaultPort - the default port number as a {@link String} that will be put by default in the corresponding {@link JTextField} upon startup
	 */
	public ConfigDialog(Client client, String title, String defaultIP, String defaultPort) {
		super("Configuration");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		
		JPanel srvIPpanel = new JPanel();
		JTextField srvIPfield = new JTextField(defaultIP, 16);
		srvIPpanel.add(new JLabel("Server IP Address", SwingConstants.LEFT));
		srvIPpanel.add(srvIPfield);
		mainPanel.add(srvIPpanel);
		
		JPanel srvPortPanel = new JPanel();
		JTextField srvPortField = new JTextField(defaultPort, 5);
		srvPortPanel.add(new JLabel("Server Port", SwingConstants.LEFT));
		srvPortPanel.add(srvPortField);
		mainPanel.add(srvPortPanel);
		
		JPanel nicknamePanel = new JPanel();
		JTextField nicknameField = new JTextField(20);
		nicknamePanel.add(new JLabel("Nickname", SwingConstants.LEFT));
		nicknamePanel.add(nicknameField);
		mainPanel.add(nicknamePanel);
		
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton("OK"), cancelButton = new JButton("Cancel");
		okButton.setActionCommand("ok");
		cancelButton.setActionCommand("cancel");
		
		ActionListener cmdListen = new ActionListener() {					// Detects user clicks on the 'OK' button or the 'Cancel' button.
			public void actionPerformed(ActionEvent event) {
				if (event.getActionCommand().equals("ok")) {
					String ip = srvIPfield.getText().trim(), port = srvPortField.getText().trim(), nickname = nicknameField.getText().trim();
					if (ip.matches("^[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}$") && port.matches("[0-9]+")) {
						try {
							client.setChatterName(nickname);
							client.connectTo(ip, Integer.parseInt(port));
							client.setRunning(true);
							(new Thread(new ServerWorker(client))).start();
							client.startChatWindow();
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					}
				}
				ConfigDialog.this.dispose();
			}
		};
		
		okButton.addActionListener(cmdListen);
		cancelButton.addActionListener(cmdListen);
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		mainPanel.add(buttonsPanel);
		
		this.add(mainPanel);
		this.setResizable(false);
	}
	
	/**
	 * Computes the graphics for the console window, starts it and makes it visible to the user.
	 */
	public void start() {
		this.pack();
		this.setVisible(true);
		this.requestFocus();
	}
	
	/**
	 * Stops completely this console, frees the occupied resources and disposes the frame.
	 */
	public void stop() {
		this.dispose();
	}
}