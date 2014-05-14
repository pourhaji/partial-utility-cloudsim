package pt.inescid.gsd.cloudsim.partialutility;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

public class ExtendedDatacenterBroker extends DatacenterBroker {

	public ExtendedDatacenterBroker(String name) throws Exception {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long totalAllocatedMIPS;
	protected void clearDatacenters() {
		// First collect statistics
		/**
		 * total allocated mips
		 */
		totalAllocatedMIPS = 0;
		for (Vm vm : vmList) {
			Host h = vm.getHost();
			if (h == null) continue;
			double allocatedMips = h.getTotalAllocatedMipsForVm(vm);
			totalAllocatedMIPS += allocatedMips;
		}
		
		// Now, destroy VMs
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}
}
