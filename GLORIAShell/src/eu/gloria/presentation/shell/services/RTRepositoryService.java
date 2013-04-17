package eu.gloria.presentation.shell.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.ws.WebServiceException;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.repository.rt.RTRepositoryException;
import eu.gloria.gs.services.repository.rt.RTRepositoryInterface;
import eu.gloria.gs.services.repository.rt.data.RTAvailability;
import eu.gloria.presentation.shell.request.ServiceException;

public class RTRepositoryService extends Service {

	private RTRepositoryInterface rt = null;

	public RTRepositoryService() {
	}

	private synchronized RTRepositoryInterface getService() {
		if (rt == null) {
			try {
				rt = GSClientProvider.getRTRepositoryClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return rt;
	}

	public String[] getAllObservatories() throws ServiceException {

		this.getService();

		try {
			return rt.getAllObservatoryNames().toArray(new String[0]);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAllRT(String observatory) throws ServiceException {

		this.getService();

		try {
			return rt.getAllRTInObservatory(observatory).toArray(new String[0]);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAllRTDeviceNames(String rtName) throws ServiceException {

		this.getService();

		try {
			return rt.getDeviceNames(rtName).toArray(new String[0]);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void setObservatory(String rtName, String observatory)
			throws ServiceException {

		this.getService();

		try {
			rt.setRTObservatory(rtName, observatory);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getObservatory(String rtName) throws ServiceException {

		this.getService();

		try {
			return rt.getRTObservatory(rtName);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void registerObservatory(String observatory, String city,
			String country) throws ServiceException {

		this.getService();

		try {
			rt.registerObservatory(observatory, city, country);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	private Date parseStringDate(String stringDate) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

		return format.parse(stringDate);
	}

	public void setRTAvailability(@Parameter(name = "rt") String rtName,
			@Parameter(name = "from") String fromTime,
			@Parameter(name = "to") String toTime) throws ServiceException {

		this.getService();

		RTAvailability rtAvailability = new RTAvailability();

		try {
			rtAvailability.setStartingTime(this.parseStringDate(fromTime));
			rtAvailability.setEndingTime(this.parseStringDate(toTime));

		} catch (ParseException e1) {
			throw new ServiceException(e1.getMessage());
		}

		try {
			rt.setRTAvailability(rtName, rtAvailability);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void registerRT(@Parameter(name = "rt") String rtName,
			@Parameter(name = "owner") String owner,
			@Parameter(name = "url") String url) throws ServiceException {

		this.getService();

		try {
			rt.registerRT(rtName, owner, url);
		} catch (RTRepositoryException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
