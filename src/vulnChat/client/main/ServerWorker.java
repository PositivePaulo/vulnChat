package vulnChat.client.main;

import java.io.EOFException;
import java.io.IOException;

import vulnChat.data.Action;
import vulnChat.data.Bye;
import vulnChat.data.New;
import vulnChat.data.Say;


/**
 * This class, implementing {@link Runnable}, manages the chat messages server connected to a client.
 * It is meant to be done so in the background by constructing and starting a {@link Thread} object
 * of it.
 * 
 * @author Paul Mabileau
 * @version 0.4
 */
public class ServerWorker implements Runnable {
	private final Client client;
	private boolean isRunning;
	
	/**
	 * Default constructor: saves the given {@link Client} object for later use.
	 * @param client The {@link Client} object that calls this constructor
	 */
	public ServerWorker(Client client) {
		this.client = client;
	}
	
	/**
	 * Starts the background task of waiting for messages from the server and executing them for the associated client.
	 */
	public final void run() {
		String serverMsg = null;
		Action serverAction = null;
		this.isRunning = true;
		
		while (this.client.isRunning() && this.isRunning) {			// While authorized to,
			try {													// wait for a message from the server;
				if (client.settings.objTransmit.getValue()) {		// If an object is to be expected,
					do {
						try {										// read it from the stream,
							serverAction = (Action) client.getInternals().getFromServerObjectStream().readObject();
						}
						catch (EOFException exc) {
							try {
								serverAction = null;
								Thread.sleep(10);					// or wait for it if the stream has reached its end;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} while (serverAction == null);
				}
				else {
					do {											// otherwise wait for a line of text.
						serverMsg = client.getInternals().getFromServerTextReader().readLine();
					} while (serverMsg == null);
				}
				
				if (!client.settings.objTransmit.getValue()) {		// If an text has been received,
					if (serverMsg.matches("[a-z]{3}\\s[\\p{Alnum}\\p{Punct}]{1,50}\\s?.{0,1000}")) {
						serverAction = parseAction(serverMsg);		// parse it to an action;
					}
				}
				
				if (serverAction instanceof New) {					// "new" action -> a new user joind the channel,
					this.client.getInternals().getLinePrinter().println(serverAction.chatterName + " joined the channel.");
				}
				else if (serverAction instanceof Bye) {				// "bye" action -> a user left the channel,
					this.client.getInternals().getLinePrinter().println(serverAction.chatterName + " left the channel.");
				}
				else if (serverAction instanceof Say) {				// "say" action -> someone said something (probably useless, like always).
					final Say actionSay = (Say) serverAction; 
					this.client.getInternals().getLinePrinter().println(actionSay.chatterName + ": " + actionSay.message);
				}
			}
			catch (ClassNotFoundException exc) {}					// If the object is not from a known class, ignore.
			catch (IOException exc) {								// If there is a major problem at any point, close everything and report.
				this.isRunning = false;
				
				if (!this.client.getInternals().getClientSocket().isClosed()) {
					this.client.getInternals().getLinePrinter().println("Read failed");
					exc.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Parses a given textual message that respects the expected standard format to an {@link Action}
	 * object that can be easier to manipulate.
	 *  
	 * @param msg The textual transmission as a {@link String}
	 * @return The {@link Action} object which is directly equivalent to the textual transmission.
	 */
	private static final Action parseAction(String msg) {
		final String[] elements = msg.split(" ", 3);
		
		if (elements[0].equals("new")) {
			return new New(elements[1]);
		}
		else if (elements[0].equals("bye")) {
			return new Bye(elements[1]);
		}
		else if (elements[0].equals("say")) {
			return new Say(elements[1], elements[2]);
		}
		else {
			return null;
		}
	}
	
	protected final void finalize() {
		this.isRunning = false;
	}
}
