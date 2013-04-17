package eu.gloria.presentation.shell.services;

import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import eu.gloria.presentation.shell.request.ShellRequest;

public class ShellService extends Service implements ApplicationContextAware {

	private ApplicationContext context;

	private ShellService() {
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;

	}

	public String[] getCommandNames() {
		String[] allBeanNames = context.getBeanNamesForType(ShellRequest.class);

		ArrayList<String> commandList = new ArrayList<>();

		for (String beanName : allBeanNames) {
			if (beanName.endsWith("Command")) {
				commandList.add(beanName.replaceFirst("Command", ""));
			}
		}

		return commandList.toArray(new String[0]);
	}

	public String[] getOptionNames(@Parameter(name="command") String command) {
		String[] allBeanNames = context.getBeanNamesForType(ShellRequest.class);

		ArrayList<String> optionList = new ArrayList<>();

		for (String beanName : allBeanNames) {
			if (beanName.startsWith(command + "-")) {
				optionList.add(beanName.replaceFirst(command, "").replaceFirst("-", ""));
			}
		}

		return optionList.toArray(new String[0]);
	}
}
