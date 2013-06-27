package eu.gloria.presentation.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.presentation.shell.request.Interpreter;
import eu.gloria.presentation.shell.request.ServiceException;
import eu.gloria.presentation.shell.request.SyntaxException;
import eu.gloria.presentation.shell.services.UserRepositoryService;

public class GLORIAShell {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 2)
			System.err.println("Use: GLORIAShell <username> <password>");

		System.out.println("GLORIA Shell Client, v0.1.1");
		System.out.println("Developed by Fernando Serena, Ciclope Group. UPM");
		System.out.println("Copyleft @ 2013 - All wrongs reserved.");

		System.out.println();

		// System.err.close();

		GSClientProvider.setHost("localhost");
		GSClientProvider.setPort("8443");

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"shellbeans.xml");

		boolean authenticated = false;

		UserRepositoryService.setCredentials(args[0], args[1]);

		UserRepositoryService userService = (UserRepositoryService) context
				.getBean("userService");

		try {
			authenticated = userService.authenticateUser();
		} catch (ServiceException e) {
			System.out.println("ERROR: " + e.getMessage());
			System.exit(-1);
		}

		if (authenticated) {
			System.out.println("Access granted for user " + args[0] + "!");
		} else {
			System.out.println("Authentication ERROR: " + "The user '"
					+ args[0] + "' cannot be authenticated.");
			System.exit(-1);
		}

		String input = "";

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		Interpreter interpreter = (Interpreter) context.getBean("interpreter");

		while (input != null) {
			System.out.print(">");
			try {
				input += br.readLine();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				context.close();
				System.exit(-1);
			}

			if (input != null && input.contains(";")) {
				String[] messages = input.split(";");
				for (String message : messages) {
					if (!message.equals("")) {
						try {
							System.out.println(interpreter
									.processRequest(message + ";"));
						} catch (ServiceException e) {
							System.out.println("Service ERROR: "
									+ e.getMessage());
						} catch (SyntaxException e) {
							System.out.println("Syntax ERROR: "
									+ e.getMessage());
						}
					}
				}

				input = "";
			} else if (input.equals("exit")) {
				context.close();
				System.exit(0);
			}
		}

	}

}
