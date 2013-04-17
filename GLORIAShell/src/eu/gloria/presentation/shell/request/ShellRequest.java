package eu.gloria.presentation.shell.request;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import eu.gloria.presentation.shell.services.Parameter;
import eu.gloria.presentation.shell.services.Service;

public class ShellRequest implements ApplicationContextAware {

	protected ApplicationContext context;

	private Service service;
	private String operation;
	protected String request;
	private String command;
	private String description;

	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String process() throws SyntaxException, ServiceException {
		ShellRequest shellRequest = null;

		try {
			String[] arguments = Interpreter.getArguments(request);

			if (arguments != null && arguments.length == 1
					&& arguments[0].equals("?")) {

				Method method = this.getMethod(this.service, this.operation);

				String operationInfo = "Parameters: (";

				Annotation[][] parameterAnnotations = method
						.getParameterAnnotations();

				Class<?>[] parameterTypes = method.getParameterTypes();

				int i = 0;

				for (Annotation[] annotations : parameterAnnotations) {
					for (Annotation annotation : annotations) {
						if (annotation instanceof Parameter) {
							operationInfo += parameterTypes[i].getSimpleName()
									+ ": ";
							operationInfo += ((Parameter) annotation).name();

							if (i < parameterAnnotations.length - 1) {
								operationInfo += ", ";
							}
						}
					}

					i++;
				}

				operationInfo += ")\n";
				operationInfo += "Description: " + this.description;

				return operationInfo;
			}

			Object returnValue = this.execute(this.service, this.operation,
					arguments);

			if (returnValue == null) {
				return "Done!";
			}

			return Interpreter.transformData(returnValue);

		} catch (SyntaxException e) {
			shellRequest = this.refine();
		}

		if (shellRequest != null) {
			return shellRequest.process();
		}

		throw new SyntaxException("");
	}

	public ShellRequest refine() throws SyntaxException, ServiceException {

		ShellRequest shellRequest = null;

		if (request.startsWith(" -")) {

			String option = request.replace(" -", "");
			StringTokenizer tokenizer = new StringTokenizer(option, " ");

			try {
				option = tokenizer.nextToken();
			} catch (NoSuchElementException e) {
				throw new SyntaxException("Option not recognized for "
						+ this.command + " command");
			}

			try {
				shellRequest = (ShellRequest) context.getBean(this.command
						+ "-" + option);
				shellRequest
						.setRequest(request.replaceFirst(" -" + option, ""));
			} catch (NoSuchBeanDefinitionException e) {
				throw new SyntaxException("Option not recognized for "
						+ this.command + " command");
			}
		}

		return shellRequest;
	}

	private Method getMethod(Object executor, String methodName) {
		Method[] methods = executor.getClass().getMethods();

		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}

		return null;
	}

	protected Object execute(Object executor, String methodName, String[] args)
			throws SyntaxException, ServiceException {

		Object[] values = null;
		Method selectedMethod = this.getMethod(executor, methodName);

		if (selectedMethod != null) {
			Class<?>[] parameterTypes = selectedMethod.getParameterTypes();

			if ((parameterTypes.length != 0 && args == null)
					|| (args != null && parameterTypes.length != args.length)) {
				throw new SyntaxException("Bad arguments number");
			}

			int i = 0;
			values = new Object[parameterTypes.length];

			for (Class<?> parameterType : parameterTypes) {
				Object value = Interpreter.parseField(parameterType, args[i]);

				values[i] = value;

				i++;
			}
		}

		Object returnValue = null;

		try {
			if (values == null) {

				returnValue = selectedMethod.invoke(executor);
			} else {
				returnValue = selectedMethod.invoke(executor, values);
			}
		} catch (IllegalAccessException e) {
			throw new SyntaxException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ServiceException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new ServiceException(e.getTargetException().getMessage());
		}

		return returnValue;
	}
}
