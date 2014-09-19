import java.net.*;
import java.io.*;


import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Lab4Server implements Runnable {
	private int complete, threads, correct, incorrect;
	private static final int PORT = 4242;
	private Socket clientSock;
	private PrintWriter out;
	private BufferedReader in;
	private double serverAnswer;

	public Lab4Server(Socket sock) throws IOException {
		clientSock = sock;
		out = new PrintWriter(sock.getOutputStream());
		in = new BufferedReader(new InputStreamReader(
				clientSock.getInputStream()));
	}

	public static void main(String args[]) throws IOException {
		@SuppressWarnings("resource")
		ServerSocket sock = new ServerSocket(PORT);
		while (true) {
			Socket client = sock.accept();
			(new Thread(new Lab4Server(client))).start();
		}
		
	}

	private synchronized void incThread() {
		threads++;
	}

	private synchronized void decThread() {
		threads--;
	}

	private synchronized void incCorrect() {
		correct++;
	}

	private synchronized void incIncorrect() {
		incorrect++;
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
						if (complete < 5) {
							out.println("AMP WORK");
							String operation = "";
							switch ((int) Math.round(Math.random() * 3)) {
							case 0:
								operation = "+";
								break;
							case 1:
								operation = "-";
								break;
							case 2:
								operation = "*";
								break;
							default:
								operation = "/";
								break;
							}
							double op1 = Math.random() * 100;
							double op2 = Math.random() * 100;
							ScriptEngineManager mgr = new ScriptEngineManager();
							ScriptEngine engine = mgr
									.getEngineByName("JavaScript");
							serverAnswer = (double) engine.eval(op1 + operation
									+ op2);
							System.out.println("Sending client AMP: " + op1
									+ " " + operation + " " + op2);
							out.println(op1 + " " + operation + " " + op2);
						} else {
							System.out.println("No work Available for client.");
							out.println("AMP NONE");
						}
						break;
					}
					case ("PUT ANSWER"): {
						double answer = Double.parseDouble(in.readLine());
						System.out.println("Answer:" + answer);
						if (answer == serverAnswer)
							incCorrect();
						else
							incIncorrect();
						out.println("AMP OK");
						complete++;

						break;
					}
					case ("GET STATUS"): {
						out.println("AMP STATUS");
						String status = "THREADS = " + threads + ", CORRECT = "
								+ correct + ", INCORRECT = " + incorrect;
						out.println(status);
						break;
					}
					case ("END SESSION"): {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		decThread();
		
	}
}
