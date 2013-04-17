package eu.gloria.presentation.shell.services;

import javax.xml.ws.WebServiceException;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.teleoperation.ccd.CCDTeleoperationException;
import eu.gloria.gs.services.teleoperation.ccd.CCDTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.mount.MountTeleoperationException;
import eu.gloria.gs.services.teleoperation.mount.MountTeleoperationInterface;
import eu.gloria.gs.services.teleoperation.mount.TrackingRate;
import eu.gloria.gs.services.teleoperation.scam.SCamTeleoperationException;
import eu.gloria.gs.services.teleoperation.scam.SCamTeleoperationInterface;
import eu.gloria.presentation.shell.request.ServiceException;

public class TeleoperationService extends Service {

	private SCamTeleoperationInterface scam = null;
	private MountTeleoperationInterface mount = null;
	private CCDTeleoperationInterface ccd = null;

	public TeleoperationService() {
	}

	private synchronized SCamTeleoperationInterface getSCamService() {
		if (scam == null) {
			try {
				scam = GSClientProvider.getSCamTeleoperationClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return scam;
	}

	private synchronized MountTeleoperationInterface getMountService() {
		if (mount == null) {
			try {
				mount = GSClientProvider.getMountTeleoperationClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return mount;
	}

	private synchronized CCDTeleoperationInterface getCCDService() {
		if (ccd == null) {
			try {
				ccd = GSClientProvider.getCCDTeleoperationClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ccd;
	}

	public String getStreamImage(String rt, String scamName)
			throws ServiceException {

		this.getSCamService();

		try {
			return scam.getImageURL(rt, scamName);
		} catch (SCamTeleoperationException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void setTrackingRate(String rt, String mountName, String tracking)
			throws ServiceException {

		this.getMountService();

		try {
			mount.setTrackingRate(rt, mountName,
					TrackingRate.valueOf("DRIVE_" + tracking.toUpperCase()));
		} catch (MountTeleoperationException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void enableMountTracking(String rt, String mountName)
			throws ServiceException {

		this.getMountService();

		try {
			mount.setTracking(rt, mountName, true);
		} catch (MountTeleoperationException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void disableMountTracking(String rt, String mountName)
			throws ServiceException {

		this.getMountService();

		try {
			mount.setTracking(rt, mountName, false);
		} catch (MountTeleoperationException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void parkMount(String rt, String mountName) throws ServiceException {

		this.getMountService();

		try {
			mount.park(rt, mountName);
		} catch (MountTeleoperationException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void startExposure(String rt, String ccdName)
			throws ServiceException {

		this.getCCDService();

		try {
			ccd.startExposure(rt, ccdName);
		} catch (CCDTeleoperationException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
