package eu.gloria.presentation.shell.services;

import javax.xml.ws.WebServiceException;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.repository.image.ImageRepositoryException;
import eu.gloria.gs.services.repository.image.ImageRepositoryInterface;
import eu.gloria.presentation.shell.request.ServiceException;

public class ImageRepositoryService extends Service {

	private ImageRepositoryInterface image = null;

	public ImageRepositoryService() {
	}

	private synchronized ImageRepositoryInterface getService() {
		if (image == null) {
			try {
				image = GSClientProvider.getImageRepositoryClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return image;
	}

	public void saveImage(@Parameter(name = "user") String user,
			@Parameter(name = "rt") String rt,
			@Parameter(name = "ccd") String ccd,
			@Parameter(name = "url") String url) throws ServiceException {

		this.getService();

		try {

			image.saveImage(user, rt, ccd, url);

		} catch (ImageRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
