package pt.inescid.gsd.cloudsim.partialutility;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;

public class PUVm extends Vm {
	public static final boolean RECORD_HISTORY = false;
	
	/** The Constant HISTORY_LENGTH. */
	public static final int HISTORY_LENGTH = 2<<12;

	/** The utilization history. */
	private final List<Double> utilizationHistory = new LinkedList<Double>();

	/** The previous time. */
	private double previousTime;


	private VMType type;
	private PUClass vmClass;
	private double totalDeprecition;
	
	public PUVm(int id, int userId, double mips, int numberOfPes,
			int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler, VMType type) {
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
		this.type = type;
		totalDeprecition = 0;
	}

	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		if (RECORD_HISTORY) {
			double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
			if (CloudSim.clock() != 0 || utilization != 0) {
				addUtilizationHistoryValue(utilization);
			}
			setPreviousTime(currentTime);
		}
		double time = super.updateVmProcessing(currentTime, mipsShare);
//		if (currentTime > getPreviousTime() && (currentTime - 0.1) % getSchedulingInterval() == 0) {
//			double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
//			if (CloudSim.clock() != 0 || utilization != 0) {
//				addUtilizationHistoryValue(utilization);
//			}
//			setPreviousTime(currentTime);
//		}
		return time;
	}

	/**
	 * Adds the utilization history value.
	 * 
	 * @param utilization the utilization
	 */
	public void addUtilizationHistoryValue(double utilization) {
		getUtilizationHistory().add(0, utilization);
		if (getUtilizationHistory().size() > HISTORY_LENGTH) {
			getUtilizationHistory().remove(HISTORY_LENGTH);
		}
	}
	
	protected List<Double> getUtilizationHistory() {
		return utilizationHistory;
	}
	
	/**
	 * Gets the utilization mean in percents.
	 * 
	 * @return the utilization mean //in MIPS
	 */
	public double getUtilizationMean() {
		double mean = 0;
		if (!getUtilizationHistory().isEmpty()) {
			int n = HISTORY_LENGTH;
			if (HISTORY_LENGTH > getUtilizationHistory().size()) {
				n = getUtilizationHistory().size();
			}
			for (int i = 0; i < n; i++) {
				mean += getUtilizationHistory().get(i);
			}
			mean /= n;
		}
		return mean; // * getMips();
	}
	
	/**
	 * Sets the previous time.
	 * 
	 * @param previousTime the new previous time
	 */
	public void setPreviousTime(double previousTime) {
		this.previousTime = previousTime;
	}
	
	private double getSchedulingInterval() {
		return Constants.SCHEDULING_INTERVAL;
	}

	/**
	 * Gets the previous time.
	 * 
	 * @return the previous time
	 */
	public double getPreviousTime() {
		return previousTime;
	}
	
	public VMType getType() {
		return type;
	}

	public void setType(VMType type) {
		this.type = type;
	}

	public PUClass getVmClass() {
		return vmClass;
	}

	public void setVmClass(PUClass vmClass) {
		this.vmClass = vmClass;
	}

	public void depreciate(double percentage) {
		// commented at 23-01-2014 => infinite loop with VM 40 @ Host#18
		setMips(getMips()-getMips()*percentage);
		totalDeprecition += percentage;
	}

	public void undoDepreciation(double percentage) {
		setMips(getMips()+getMips()*percentage);
	}
	
}
