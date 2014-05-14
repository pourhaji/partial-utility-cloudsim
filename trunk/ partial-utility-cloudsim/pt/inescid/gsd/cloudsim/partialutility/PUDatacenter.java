package pt.inescid.gsd.cloudsim.partialutility;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;

public class PUDatacenter extends Datacenter {

	/**
	 * Matrix of partial utility for each class and depreciation percentage (M) 
	 */
	private PUMatrix M;
	
	/**
	 * Matrix of base prices for each VM type and PU class (P)
	 */
	private PricesMatrix P;

	
	public PUDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		
		M = new PUMatrix();
		
		P = new PricesMatrix();
	}

}
