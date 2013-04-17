package eu.gloria.presentation.shell.services;

import javax.xml.ws.WebServiceException;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.core.security.client.Credentials;
import eu.gloria.gs.services.core.security.client.ThreadCredentialsStore;
import eu.gloria.gs.services.repository.user.UserRepositoryException;
import eu.gloria.gs.services.repository.user.UserRepositoryInterface;
import eu.gloria.gs.services.repository.user.data.UserInformation;
import eu.gloria.presentation.shell.request.ServiceException;

public class UserRepositoryService extends Service {

	private UserRepositoryInterface user = null;
	private String systemUsername;
	private String systemPassword;

	public UserRepositoryService() {
	}

	private synchronized UserRepositoryInterface getService() {
		if (user == null) {
			try {
				user = GSClientProvider.getUserRepositoryClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return user;
	}

	public void setSystemUsername(String username) {
		this.systemUsername = username;
	}

	public void setSystemPassword(String password) {
		this.systemPassword = password;
	}

	public static void setCredentials(String username, String password) {
		Credentials credentials = new Credentials();
		credentials.setPassword(password);
		credentials.setUsername(username);
		ThreadCredentialsStore.storeCredentials(credentials);
	}

	public String getCredentialsUsername() {
		return ThreadCredentialsStore.getCredentials().getUsername();
	}

	public String getCredentialsPassword() {
		return ThreadCredentialsStore.getCredentials().getPassword();
	}

	public boolean authenticateUser() throws ServiceException {

		this.getService();

		try {
			Credentials credentials = ThreadCredentialsStore.getCredentials();
			String currentUser = credentials.getUsername();
			String currentPass = credentials.getPassword();

			credentials.setUsername(this.systemUsername);
			credentials.setPassword(this.systemPassword);

			boolean authenticated = user.authenticateUser(currentUser,
					currentPass);

			credentials.setPassword(currentPass);
			credentials.setUsername(currentUser);
			return authenticated;

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getUserPassword(String username) throws ServiceException {

		this.getService();

		try {
			UserInformation userInformation = user.getUserInformation(username);

			return userInformation.getPassword();

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void setUserPassword(String username, String password)
			throws ServiceException {

		this.getService();

		try {
			user.changePassword(username, password);
			setCredentials(username, password);

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void createUser(String username) throws ServiceException {

		this.getService();

		try {
			user.createUser(username);

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void activateUser(String username, String password)
			throws ServiceException {

		this.getService();

		try {
			user.activateUser(username, password);

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void deactivateUser(String username, String password)
			throws ServiceException {

		this.getService();

		try {
			user.deactivateUser(username, password);

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public boolean isUserActivated(@Parameter(name = "username") String username)
			throws ServiceException {

		this.getService();

		try {
			UserInformation userInfo = user.getUserInformation(username);

			return userInfo.getPassword() != null;

		} catch (UserRepositoryException e) {
			if (e.getMessage().contains("is not activated"))
				return false;

			throw new ServiceException(e.getMessage());
		}
	}

	public boolean containsUser(String username) throws ServiceException {

		this.getService();

		try {
			return user.containsUser(username);

		} catch (UserRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
