package pt.inescid.gsd.cloudsim.partialutility;

import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.util.MathUtil;

public class PUHost extends Host {

	public static enum FailureCause {MIPS, RAM, BW};
	
	private HashMap<Vm,FailureCause> allocationFailedVM = new HashMap<Vm,FailureCause>();
	
	public HashMap<Vm, FailureCause> getAllocationFailedVM() {
		return allocationFailedVM;
	}

	public PUHost(
			int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);	
	}
	
	/**
	 * Gets the host utilization history.
	 * 
	 * @return the host utilization history
	 */
	public double[] getUtilizationHistory() {
		double[] utilizationHistory = new double[PUVm.HISTORY_LENGTH];
		double hostMips = getTotalMips();
		for (PUVm vm : this.<PUVm> getVmList()) {
			for (int i = 0; i < vm.getUtilizationHistory().size(); i++) {
				utilizationHistory[i] += 
						vm.getUtilizationHistory().get(i) * getTotalAllocatedMipsForVm(vm) 
						/ hostMips;
			}
		}
		return MathUtil.trimZeroTail(utilizationHistory);
	}
	
	
	/**
	 * Allocates PEs and memory to a new VM in the Host.
	 * Keep reason of allocation failure
	 * 
	 * @param vm Vm being started
	 * @return $true if the VM could be started in the host; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean vmCreate(Vm vm) {
		if (PUConfig.STORAGE_LIMIT)
			if (getStorage() < vm.getSize()) {
				Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId()
						+ " failed by storage");
				return false;
			}
	
		if (PUConfig.RAM_LIMIT)
			if (!getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam())) {
				Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId()
						+ " failed by RAM");
				allocationFailedVM.put(vm, FailureCause.RAM);
				return false;
			}
	
		if (PUConfig.BW_LIMIT)
			if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
				Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId()
						+ " failed by BW");
				allocationFailedVM.put(vm, FailureCause.BW);
				getRamProvisioner().deallocateRamForVm(vm);
				return false;
			}
		
		
		// 23-01-2014
		Log.printLine("[VmScheduler.vmCreate] Try allocate VM # " + vm.getId() + " to Host #" + getId());
		// ----------
		if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			Log.printLine("[VmScheduler.vmCreate] Allocation of VM #" + vm.getId() + " to Host #" + getId()
					+ " failed by MIPS");
			allocationFailedVM.put(vm, FailureCause.MIPS);
			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}

		setStorage(getStorage() - vm.getSize());
		getVmList().add(vm);
		vm.setHost(this);
		
		/* Hack to determine overcommit of memory */
		for (Vm vmInHost : getVmList()) {
			double configured = vmInHost.getMips();
			double allocated = getTotalAllocatedMipsForVm(vmInHost);
			if (allocated == configured) {
				getRamProvisioner().allocateRamForVm(vmInHost, vmInHost.getRam());
				continue;
			}
			double dep = allocated/configured;
			double ramAllocated = vmInHost.getRam();//getRamProvisioner().getAllocatedRamForVm(vmInHost);
			int newRamAllocated = (int) Math.ceil(ramAllocated * dep);
			getRamProvisioner().deallocateRamForVm(vmInHost);
			getRamProvisioner().allocateRamForVm(vmInHost, newRamAllocated);
		}
		
		return true;
	}
	


}
