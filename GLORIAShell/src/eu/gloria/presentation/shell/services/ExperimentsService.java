package eu.gloria.presentation.shell.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.WebServiceException;

import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.experiment.online.OnlineExperimentException;
import eu.gloria.gs.services.experiment.online.OnlineExperimentInterface;
import eu.gloria.gs.services.experiment.online.data.ExperimentInformation;
import eu.gloria.gs.services.experiment.online.data.FeatureCompliantInformation;
import eu.gloria.gs.services.experiment.online.data.NoSuchExperimentException;
import eu.gloria.gs.services.experiment.online.data.OperationInformation;
import eu.gloria.gs.services.experiment.online.data.ParameterInformation;
import eu.gloria.gs.services.experiment.online.data.ReservationInformation;
import eu.gloria.gs.services.experiment.online.data.FeatureInformation;
import eu.gloria.gs.services.experiment.online.data.TimeSlot;
import eu.gloria.gs.services.experiment.online.models.DuplicateExperimentException;
import eu.gloria.gs.services.experiment.online.models.ExperimentFeature;
import eu.gloria.gs.services.experiment.online.operations.ExperimentOperation;
import eu.gloria.gs.services.experiment.online.operations.ExperimentOperationException;
import eu.gloria.gs.services.experiment.online.operations.NoSuchOperationException;
import eu.gloria.gs.services.experiment.online.parameters.ExperimentParameter;
import eu.gloria.gs.services.experiment.online.parameters.ExperimentParameterException;
import eu.gloria.gs.services.experiment.online.parameters.ParameterType;
import eu.gloria.gs.services.experiment.online.reservation.ExperimentNotInstantiatedException;
import eu.gloria.gs.services.experiment.online.reservation.ExperimentReservationArgumentException;
import eu.gloria.gs.services.experiment.online.reservation.NoSuchReservationException;
import eu.gloria.gs.services.experiment.online.reservation.MaxReservationTimeException;
import eu.gloria.gs.services.experiment.online.reservation.NoReservationsAvailableException;
import eu.gloria.presentation.shell.request.ServiceException;

public class ExperimentsService extends Service {

	private OnlineExperimentInterface experiment = null;

	public ExperimentsService() {		
	}

	private synchronized OnlineExperimentInterface getService() {
		if (experiment == null) {
			try {
				experiment = GSClientProvider.getOnlineExperimentClient();
			} catch (WebServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return experiment;
	}

	public void createExperiment(
			@Parameter(name = "experiment") String experimentName)
			throws ServiceException {

		this.getService();

		try {
			experiment.createExperiment(experimentName);
		} catch (OnlineExperimentException | DuplicateExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void removeExperiment(
			@Parameter(name = "experiment") String experimentName)
			throws ServiceException {

		this.getService();

		try {
			experiment.removeExperiment(experimentName);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAllOnlineExperiments() throws ServiceException {

		this.getService();
		try {
			List<String> experiments = experiment.getAllOnlineExperiments();

			if (experiments == null) {
				return new String[0];
			}

			return experiments.toArray(new String[0]);

		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	private String timeSlotToString(TimeSlot timeSlot) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");

		return "{" + dateFormat.format(timeSlot.getBegin()) + ", "
				+ dateFormat.format(timeSlot.getEnd()) + "}";
	}

	private String reservationToString(ReservationInformation resInfo) {
		return "{rid=" + resInfo.getReservationId() + ", exp="
				+ resInfo.getExperiment() + ", when="
				+ this.timeSlotToString(resInfo.getTimeSlot()) + ", on="
				+ resInfo.getTelescopes().toString() + "}";
	}

	public String[] getMyActiveReservations() throws ServiceException {

		this.getService();
		try {
			List<ReservationInformation> reservations = experiment
					.getMyCurrentReservations();

			List<String> reservationsStr = new ArrayList<String>();

			for (ReservationInformation reservation : reservations) {
				reservationsStr.add(this.reservationToString(reservation));
			}

			return reservationsStr.toArray(new String[0]);

		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		} catch (NoReservationsAvailableException e) {
			return new String[0];
		}
	}

	private Date parseStringDate(String stringDate) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		return format.parse(stringDate);
	}

	public void makeReservation(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "telescopes") String[] telescopes,
			@Parameter(name = "fromDate") String begin,
			@Parameter(name = "toDate") String end) throws ServiceException {

		this.getService();

		Date beginDate = null;
		Date endDate = null;
		try {
			beginDate = this.parseStringDate(begin);
			endDate = this.parseStringDate(end);
		} catch (ParseException e) {
			throw new ServiceException(e.getMessage());
		}

		TimeSlot timeSlot = new TimeSlot();
		timeSlot.setBegin(beginDate);
		timeSlot.setEnd(endDate);

		try {

			experiment.reserveExperiment(experimentName, new ArrayList<String>(
					Arrays.asList(telescopes)), timeSlot);
		} catch (OnlineExperimentException | MaxReservationTimeException
				| ExperimentReservationArgumentException
				| NoReservationsAvailableException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void cancelReservation(@Parameter(name = "reservationId") int rid)
			throws ServiceException {
		this.getService();

		try {
			experiment.cancelExperimentReservation(rid);
		} catch (OnlineExperimentException | NoSuchReservationException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAvailableReservations(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "telescopes") String[] telescopes,
			@Parameter(name = "maxResults") int maxEntries)
			throws ServiceException {

		this.getService();

		try {
			List<TimeSlot> timeSlots = experiment.getAvailableReservations(
					experimentName,
					new ArrayList<String>(Arrays.asList(telescopes)));

			if (timeSlots == null)
				return new String[0];

			String[] timeSlotsStr = new String[Math.min(timeSlots.size(),
					maxEntries)];

			int i = 0;
			while (i < maxEntries) {
				timeSlotsStr[i] = this.timeSlotToString(timeSlots.get(i));
				i++;
			}

			return timeSlotsStr;

		} catch (OnlineExperimentException
				| ExperimentReservationArgumentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getExperimentOperations(
			@Parameter(name = "experiment") String experimentName)
			throws ServiceException {
		this.getService();

		try {
			ExperimentInformation expInfo = experiment
					.getExperimentInformation(experimentName);

			List<OperationInformation> operations = expInfo.getOperations();

			if (operations == null)
				return new String[0];

			String[] operationNames = new String[operations.size()];
			int i = 0;
			for (OperationInformation operation : operations) {
				operationNames[i] = operation.getModelName();
				i++;
			}

			return operationNames;

		} catch (NoSuchExperimentException | OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getExperimentParameters(
			@Parameter(name = "experiment") String experimentName)
			throws ServiceException {
		this.getService();

		try {
			ExperimentInformation expInfo = experiment
					.getExperimentInformation(experimentName);

			List<ParameterInformation> parameters = expInfo.getParameters();

			if (parameters == null)
				return new String[0];

			String[] parameterNames = new String[parameters.size()];
			int i = 0;
			for (ParameterInformation parameter : parameters) {
				parameterNames[i] = parameter.getModelName();
				i++;
			}

			return parameterNames;

		} catch (NoSuchExperimentException | OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void addExperimentParameter(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "parameterName") String parameterName,
			@Parameter(name = "parameterType") String type,
			@Parameter(name = "arguments") String[] arguments)
			throws ServiceException {

		this.getService();
		
		ParameterInformation paramInfo = new ParameterInformation();
		paramInfo.setModelName(parameterName);
		paramInfo.setParameterName(type);
		paramInfo.setArguments(arguments);

		try {
			experiment.addExperimentParameter(experimentName, paramInfo);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void addExperimentOperation(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "operationName") String operationName,
			@Parameter(name = "operationType") String type,
			@Parameter(name = "arguments") String[] arguments)
			throws ServiceException {

		this.getService();
		
		OperationInformation operationInfo = new OperationInformation();
		operationInfo.setModelName(operationName);
		operationInfo.setOperationName(type);
		operationInfo.setArguments(arguments);

		try {
			experiment.addExperimentOperation(experimentName, operationInfo);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void addExperimentFeature(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "featureName") String featureName,
			@Parameter(name = "arguments") String[] arguments)
			throws ServiceException {

		this.getService();
		
		FeatureInformation featureInfo = new FeatureInformation();

		featureInfo.setName(featureName);
		featureInfo.setArguments(arguments);

		try {
			experiment.addExperimentFeature(experimentName, featureInfo);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public boolean testExperimentFeature(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "featureName") String featureName,
			@Parameter(name = "arguments") String[] arguments)
			throws ServiceException {

		this.getService();
		
		FeatureInformation featureInfo = new FeatureInformation();

		featureInfo.setName(featureName);
		featureInfo.setArguments(arguments);

		try {
			return experiment
					.testExperimentFeature(experimentName, featureInfo);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getFeatureCompliantParameter(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "featureName") String featureName,
			@Parameter(name = "arguments") String[] arguments,
			@Parameter(name = "queryParameterName") String queryParameterName)
			throws ServiceException {

		this.getService();
		
		FeatureInformation featureInfo = new FeatureInformation();

		featureInfo.setName(featureName);
		featureInfo.setArguments(arguments);

		try {
			FeatureCompliantInformation featureCompliantInfo = experiment
					.getFeatureCompliantInformation(experimentName, featureInfo);

			String parameterName = featureCompliantInfo.getParameters().get(
					queryParameterName);

			return parameterName;

		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getFeatureCompliantOperation(
			@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "featureName") String featureName,
			@Parameter(name = "arguments") String[] arguments,
			@Parameter(name = "queryOperationName") String queryOperationName)
			throws ServiceException {

		this.getService();
		
		FeatureInformation featureInfo = new FeatureInformation();

		featureInfo.setName(featureName);
		featureInfo.setArguments(arguments);

		try {
			FeatureCompliantInformation featureCompliantInfo = experiment
					.getFeatureCompliantInformation(experimentName, featureInfo);

			return featureCompliantInfo.getOperations().get(queryOperationName);

		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void executeExperimentOperation(
			@Parameter(name = "rid") int reservationId,
			@Parameter(name = "operationName") String operationName)
			throws ServiceException {

		this.getService();
		
		try {
			experiment.executeExperimentOperation(reservationId, operationName);
		} catch (OnlineExperimentException | NoSuchReservationException
				| ExperimentOperationException | NoSuchOperationException
				| ExperimentParameterException
				| ExperimentNotInstantiatedException
				| NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public Object getExperimentParameterValue(
			@Parameter(name = "rid") int reservationId,
			@Parameter(name = "parameterName") String parameterName)
			throws ServiceException {

		this.getService();
		
		try {
			return experiment.getExperimentParameterValue(reservationId,
					parameterName);
		} catch (OnlineExperimentException | NoSuchReservationException
				| ExperimentNotInstantiatedException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void setExperimentParameterValue(
			@Parameter(name = "rid") int reservationId,
			@Parameter(name = "parameterName") String parameterName,
			@Parameter(name = "value") Object value) throws ServiceException {

		this.getService();
		
		try {

			ReservationInformation resInfo = experiment
					.getReservationInformation(reservationId);

			ExperimentInformation expInfo = experiment
					.getExperimentInformation(resInfo.getExperiment());

			List<ParameterInformation> parameterInfos = expInfo.getParameters();

			Object castedValue = value;

			for (ParameterInformation paramInfo : parameterInfos) {
				if (paramInfo.getModelName().equals(parameterName)) {
					ParameterType type = paramInfo.getParameter().getType();

					Class<?> valueType = type.getValueType();

					try {
						if (valueType.equals(Integer.class)) {
							castedValue = Integer.parseInt((String) value);
						} else if (valueType.equals(Double.class)) {
							castedValue = Double.parseDouble((String) value);
						}
					} catch (NumberFormatException e) {
					}
				}
			}

			experiment.setExperimentParameterValue(reservationId,
					parameterName, castedValue);
		} catch (OnlineExperimentException | NoSuchReservationException
				| ExperimentNotInstantiatedException
				| NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAllParameterNames() throws ServiceException {

		this.getService();
		
		try {
			Set<String> parameters = experiment.getAllExperimentParameters();

			return parameters.toArray(new String[0]);
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAllOperationNames() throws ServiceException {

		this.getService();
		
		try {
			Set<String> operations = experiment.getAllExperimentOperations();

			return operations.toArray(new String[0]);
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getAllFeatureNames() throws ServiceException {

		this.getService();
		
		try {
			Set<String> operations = experiment.getAllExperimentFeatures();

			return operations.toArray(new String[0]);
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}
	
	public String getFeatureSignature(@Parameter(name = "feature") String featureName)
			throws ServiceException {

		this.getService();
		
		try {
			ExperimentFeature requestedFeature = experiment
					.getExperimentFeature(featureName);

			String[] paramSignature = new String[requestedFeature.getOperations().size()];

			for (int i = 0; i < paramSignature.length; i++) {
				paramSignature[i] = "(arg" + i + ") ";

//				if (requestedParameter.argumentIsParameter(i)) {
//					ExperimentParameter parameter = requestedParameter
//							.getParameterDependencies().get(i);
//
//					paramSignature[i] += "Parameter: " + parameter.getName();
//				} else {
//					Class<?> valueType = requestedParameter
//							.getValueArgumentType(i);
//
//					paramSignature[i] += "Value: " + valueType.getSimpleName();
//				}
			}

			return Arrays.toString(paramSignature);
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getParameterSignature(@Parameter(name = "parameter") String paramName)
			throws ServiceException {

		this.getService();
		
		try {
			ExperimentParameter requestedParameter = experiment
					.getExperimentParameter(paramName);

			String[] signature = new String[requestedParameter
					.getArgumentsNumber()];

			for (int i = 0; i < signature.length; i++) {
				signature[i] = "(arg" + i + ") ";

				if (requestedParameter.argumentIsParameter(i)) {
					ExperimentParameter parameter = requestedParameter
							.getParameterDependencies().get(i);

					signature[i] += "Parameter: " + parameter.getName();
				} else {
					Class<?> valueType = requestedParameter
							.getValueArgumentType(i);

					signature[i] += "Value: " + valueType.getSimpleName();
				}
			}

			String baseAppend = requestedParameter.getType().getValueType()
					.getSimpleName();

			return Arrays.toString(signature) + " -> " + baseAppend;
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getParameterBehaviour(@Parameter(name = "parameter") String paramName)
			throws ServiceException {

		this.getService();
		
		try {
			ExperimentParameter requestedParameter = experiment
					.getExperimentParameter(paramName);

			Map<String, String[]> initOperations = requestedParameter
					.getInitOperations();

			if (initOperations == null) {
				return new String[0];
			}

			String[] behaviour = new String[initOperations.size()];

			int order = 0;
			for (String action : initOperations.keySet()) {

				behaviour[order] = action + "(";
				String[] actionArgs = initOperations.get(action);

				int argOrder = 0;
				for (Object arg : actionArgs) {

					behaviour[order] += String.valueOf(arg);

					if (argOrder < actionArgs.length - 1)
						behaviour[order] += " ,";

					argOrder++;
				}

				behaviour[order] += ")";

				order++;
			}

			return behaviour;

		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getOperationSignature(@Parameter(name = "operation") String operationName)
			throws ServiceException {

		this.getService();
		
		try {
			ExperimentOperation requestedOperation = experiment
					.getExperimentOperation(operationName);

			String[] signature = new String[requestedOperation
					.getParametersNumber()];
			ExperimentParameter[] parameters = requestedOperation
					.getParameterTypes();

			for (int i = 0; i < signature.length; i++) {

				signature[i] = "(arg" + i + ") ";

				signature[i] += parameters[i].getName();
			}

			return operationName + " " + Arrays.toString(signature);
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getOperationBehaviour(@Parameter(name = "operation") String operationName)
			throws ServiceException {

		this.getService();
		
		try {
			ExperimentOperation requestedOperation = experiment
					.getExperimentOperation(operationName);

			Map<String, String[]> completeBehaviour = requestedOperation
					.getBehaviour();

			if (completeBehaviour == null) {
				return new String[0];
			}

			String[] behaviourArray = new String[completeBehaviour.size()];

			int order = 0;
			for (String action : completeBehaviour.keySet()) {

				behaviourArray[order] = action + "(";
				String[] actionArgs = completeBehaviour.get(action);

				int argOrder = 0;
				for (Object arg : actionArgs) {

					behaviourArray[order] += String.valueOf(arg);

					if (argOrder < actionArgs.length - 1)
						behaviourArray[order] += " ,";

					argOrder++;
				}

				behaviourArray[order] += ")";

				order++;
			}

			return behaviourArray;

		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getParameterProperties(@Parameter(name = "parameter") String paramName)
			throws ServiceException {

		this.getService();
		
		try {
			ExperimentParameter requestedParameter = experiment
					.getExperimentParameter(paramName);

			Map<String, Object> propertiesMap = requestedParameter
					.getProperties();

			if (propertiesMap != null) {
				String[] properties = new String[propertiesMap.size()];

				int i = 0;

				for (String propertyName : propertiesMap.keySet()) {
					Object propertyValue = propertiesMap.get(propertyName);
					properties[i] = propertyName + ": " + propertyValue;

					i++;
				}
				return properties;
			}

			return new String[0];
		} catch (OnlineExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getExperimentOperationsByType(@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "type") String type) throws ServiceException {
		
		this.getService();
		
		try {

			ExperimentInformation expInfo = experiment
					.getExperimentInformation(experimentName);
			List<OperationInformation> operations = expInfo.getOperations();

			ArrayList<String> operationNames = new ArrayList<>();

			for (OperationInformation operation : operations) {
				if (operation.getOperationName().equals(type)) {
					operationNames.add(operation.getModelName());
				}
			}
			return operationNames.toArray(new String[0]);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getExperimentParametersByType(@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "type") String type) throws ServiceException {
		
		this.getService();
		
		try {

			ExperimentInformation expInfo = experiment
					.getExperimentInformation(experimentName);
			List<ParameterInformation> parameters = expInfo.getParameters();

			ArrayList<String> paramNames = new ArrayList<>();

			for (ParameterInformation parameter : parameters) {
				if (parameter.getParameterName().equals(type)) {
					paramNames.add(parameter.getModelName());
				}
			}
			return paramNames.toArray(new String[0]);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String[] getExperimentParametersByProperty(@Parameter(name = "experiment") String experimentName,
			@Parameter(name = "property") String property) throws ServiceException {
		
		this.getService();
		
		try {

			ExperimentInformation expInfo = experiment
					.getExperimentInformation(experimentName);
			List<ParameterInformation> parameters = expInfo.getParameters();

			ArrayList<String> paramNames = new ArrayList<>();

			for (ParameterInformation parameter : parameters) {

				Map<String, Object> properties = parameter.getParameter()
						.getProperties();

				if (properties != null && properties.containsKey(property)) {
					paramNames.add(parameter.getModelName());
				}
			}
			return paramNames.toArray(new String[0]);
		} catch (OnlineExperimentException | NoSuchExperimentException e) {
			throw new ServiceException(e.getMessage());
		}
	}
}
