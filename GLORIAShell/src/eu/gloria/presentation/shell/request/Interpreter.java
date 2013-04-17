package eu.gloria.presentation.shell.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Interpreter implements ApplicationContextAware {

	private ApplicationContext context;

	private Interpreter() {

	}

	public static boolean verifyType(Class<?> type, String field) {

		if (type.equals(String.class)) {
			// return field.startsWith("\"") && field.endsWith("\"");
			return true;
		} else if (type.equals(String[].class)) {
			return field.startsWith("[") && field.endsWith("]");
		}

		return true;
	}

	public static Object parseField(Class<?> type, String field) {

		if (verifyType(type, field)) {

			if (type.equals(String.class)) {
				return field;// .replaceAll("\"", "");
			} else if (type.equals(String[].class)) {

				String filtered = field.substring(1);
				filtered = filtered.substring(0, filtered.length() - 1);

				StringTokenizer tokenizer = new StringTokenizer(filtered, ",");

				String[] strElements = new String[tokenizer.countTokens()];
				int i = 0;
				boolean finished = false;

				while (!finished) {
					try {
						String element = (String) tokenizer.nextElement();
						strElements[i] = element;
						i++;
					} catch (NoSuchElementException e) {
						finished = true;
					}
				}

				return strElements;
			} else if (type.equals(int.class) || type.equals(Integer.class)) {
				return Integer.parseInt(field);
			} else if (type.equals(double.class) || type.equals(Double.class)) {
				return Double.parseDouble(field);
			}
		}

		return field;
	}

	public static String transformData(Object data) {

		if (data instanceof String) {
			return (String) data;
			// return "\"" + data + "\"";
		} else if (data instanceof String[]) {
			return Arrays.toString((Object[]) data);
		} else if (data instanceof Integer) {
			return String.valueOf((Integer) data);
		} else if (data instanceof Double) {
			return String.valueOf((Double) data);
		} else if (data instanceof Boolean) {
			return String.valueOf((Boolean) data);
		}

		return null;
	}

	public static String[] getArguments(String request) throws SyntaxException {

		StringTokenizer tokenizer = new StringTokenizer(request);

		String blanks = tokenizer.nextToken("(");

		String[] argumentsArray = null;

		if (blanks.matches("[ ]*")) {

			String argumentsStr = tokenizer.nextToken(";");

			if (argumentsStr.startsWith("(") && argumentsStr.endsWith(")")) {

				argumentsStr = argumentsStr.replace("(", "").replace(")", "");
				tokenizer = new StringTokenizer(argumentsStr, ",");

				List<String> arguments = null;

				if (tokenizer.countTokens() > 0) {

					arguments = new ArrayList<String>();

					try {

						String argument = null;
						String listArgument = null;

						do {
							argument = tokenizer.nextToken().trim();

							if (argument.startsWith("[")
									&& !argument.endsWith("]")) {
								listArgument = argument;
							} else {

								if (argument.equals("")) {
									throw new SyntaxException("");
								}

								if (listArgument != null) {
									listArgument += "," + argument;

									if (listArgument.endsWith("]")) {
										arguments.add(listArgument);
										listArgument = null;
									}
								} else {
									arguments.add(argument);
								}
							}
						} while (argument != null);

					} catch (NoSuchElementException e) {
					}

					argumentsArray = arguments.toArray(new String[0]);
				}

				return argumentsArray;
			}
		}

		throw new SyntaxException("");
	}

	public String processRequest(String request) throws ServiceException,
			SyntaxException {

		ShellRequest shellRequest = null;

		StringTokenizer tokenizer = new StringTokenizer(request, " ");

		try {
			String command = tokenizer.nextToken();
			shellRequest = (ShellRequest) context.getBean(command + "Command");
			shellRequest.setRequest(request.replaceFirst(command, ""));

			return shellRequest.process();

		} catch (NoSuchElementException e) {
			throw new SyntaxException("Command not found");
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
	}
}
