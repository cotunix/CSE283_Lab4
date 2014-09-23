import java.net.*;
import java.io.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@SuppressWarnings("resource")

/**
 * Multithreaded server for generating and sending AMP(A Math Problem) to clients.
 * @author Kyle Richardson
 *
 */
public class Lab4Server implements Runnable {
	private static int complete, threads, correct, incorrect;
	private static final int PORT = 4242;
	private Socket clientSock;
	private PrintWriter out;
	private BufferedReader in;
	private double serverAnswer;

	/**
	 * Initiates new instance of the AMP server.
	 * 
	 * @param sock
	 *            Socket of the client
	 * @throws IOException
	 *             General IO exception.
	 */
	public Lab4Server(Socket sock) throws IOException {
		clientSock = sock;
		out = new PrintWriter(clientSock.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(
				clientSock.getInputStream()));
	}

	public static void main(String args[]) throws IOException {

		ServerSocket sock = new ServerSocket(PORT);
		while (true) {
			try {
				Socket client = sock.accept();
				(new Thread(new Lab4Server(client))).start();
			} catch (SocketException e) {
				e.printStackTrace();
				System.out.println("Could not open socket. Terminating");
				System.exit(1);
			}

		}

	}

	/**
	 * Increments tracking of threads.
	 */

	private static synchronized void incThread() {
		threads++;
	}

	/**
	 * Decrements tracking of threads.
	 */
	private static synchronized void decThread() {
		threads--;
	}

	/**
	 * Increments the number of correct AMP's.
	 */

	private static synchronized void incCorrect() {
		correct++;
	}

	/**
	 * Increments the number of incorrect AMP's.
	 */
	private static synchronized void incIncorrect() {
		incorrect++;
	}

	/**
	 * Increments the number of completed AMP's.
	 */

	private static synchronized void incComplete() {
		complete++;
	}

	/**
	 * Sends a math problem to the client to solve, and calculates answer for
	 * correctness.
	 * 
	 * @throws ScriptException
	 *             Script does not properly eval the problem
	 */

	private void sendProblem() throws ScriptException {
		out.println("AMP WORK");
		String operation = getOperation();
		double op1 = Math.random() * 100;
		double op2 = Math.random() * 100;
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		serverAnswer = (double) engine.eval(op1 + operation + op2);
		System.out.println("Sending client AMP: " + op1 + " " + operation + " "
				+ op2);
		out.println(op1 + " " + operation + " " + op2);
	}

	/**
	 * Calculates randomly a math operation for calculations.
	 * 
	 * @return Math operation in string form.
	 */

	private String getOperation() {
		switch ((int) Math.round(Math.random() * 3)) {
		case 0:
			return "+";
		case 1:
			return "-";
		case 2:
			return "*";
		default:
			return "/";

		}

	}

	/**
	 * Returns server status to client.
	 */
	private void getStatus() {
		out.println("AMP STATUS");
		String status = "THREADS = " + threads + ", CORRECT = " + correct
				+ ", INCORRECT = " + incorrect;
		out.println(status);
	}

	/**
	 * Reads in client input, determines if answer is correct, then sends back
	 * to client that it has received the answer.
	 * 
	 * @throws NumberFormatException
	 *             Client's answer in incorrect format.
	 * @throws IOException
	 *             General IOException.
	 */

	private void putAnswer() throws NumberFormatException, IOException {
		double answer = Double.parseDouble(in.readLine());
		System.out.println("Answer:" + answer);
		if (answer == serverAnswer)
			incCorrect();
		else
			incIncorrect();
		out.println("AMP OK");

	}

	@Override
	public void run() {
		incThread();
		String input = "";
		loop: while (true) {
			try {
				if ((input = in.readLine()) != null) {
					switch (input) {
					case ("GET WORK"): {
						System.out.println("Client requesting AMP");
						if (complete < 10000) {
							incComplete();
							sendProblem();

						} else {
							System.out.println("No work Available for client.");
							out.println("AMP NONE");

						}
						break;
					}
					case ("PUT ANSWER"): {
						putAnswer();
						break;
					}
					case ("GET STATUS"): {
						getStatus();
						break;
					}
					case ("END SESSION"): {
						System.out
								.println("Client requesting termination, terminating connection");
						break loop;
					}

					default: {
						System.out
								.println("ERROR: Did not understand client's request");
						out.println("AMP ERROR");
					}

					}

				}

			} catch (NumberFormatException e) {
				System.out
						.println("Error: Client's answer not in proper format.");
			} catch (IOException e) {
				System.out.println("Error: IO error.");
			} catch (ScriptException e) {
				System.out.println("Error: AMP did not evaluate properly");
			}
		}
		decThread();

		try {
			clientSock.close();
		} catch (IOException e) {
			System.out.println("Error: Client socket did not close properly");
		}

	}
}
