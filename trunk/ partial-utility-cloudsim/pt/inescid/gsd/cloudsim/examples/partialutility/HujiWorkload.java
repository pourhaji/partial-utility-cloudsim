package pt.inescid.gsd.cloudsim.examples.partialutility;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.WorkloadFileReader;

import pt.inescid.gsd.cloudsim.partialutility.PUClass;
import pt.inescid.gsd.cloudsim.partialutility.PUDatacenterBroker;
import pt.inescid.gsd.cloudsim.partialutility.PUVm;

public class HujiWorkload {

	public static void runHuji(Datacenter datacenter,
			PUDatacenterBroker broker, int nVMs,
			PU_Experience_Configuation experience,
			PU_DC_Configuration dcConfig, 
			BufferedWriter vmData,
			BufferedWriter cloudletPlanetLabData) throws NumberFormatException, IOException 
	{
		List<Vm> vmList = createVmListForHuji(
				broker, 
				dcConfig, 
				nVMs, 
				experience.vmClassPercentage
		);
		
		// Create Cloudlets for PlanetLab
		List<Cloudlet> cloudletList = createCloudletsHuji(
				broker.getId(), 
				nVMs
		);
		
		broker.submitVmList(vmList);
		broker.submitCloudletList(cloudletList);
	
		CloudSim.startSimulation();
		CloudSim.stopSimulation();
		
		// 3. Print VM results
		// PUGenericRunnerStatisticsHelper.printVMInfo(datacenter, broker, vmList, nVMs, experience, dcConfig, vmData);	
		
		// 4. Print cloudlet results
		PUGenericRunnerStatisticsHelper.printCloudletInfo(cloudletList, nVMs, cloudletPlanetLabData);
	}

	private static List<Cloudlet> createCloudletsHuji(int brokerId, int nVMs) throws FileNotFoundException {
		WorkloadFileReader reader = new WorkloadFileReader(PURunnerGeneric.HUJIWorkload, 1, nVMs);
		List<Cloudlet> cloudlets = reader.generateWorkload();
		for (Cloudlet cl : cloudlets) {
			cl.setUserId(brokerId);
			cl.setNumberOfPes(1);
		}
		/*List<Cloudlet> to_return = new ArrayList<Cloudlet>();
		for (int i=0; i<nVMs*2; ++i) {
			to_return.add(cloudlets.get(i));
		}*/
		return cloudlets;//to_return;
	}

	private static List<Vm> createVmListForHuji(PUDatacenterBroker broker,
			PU_DC_Configuration dcConfig, int nVMs,
			HashMap<PUClass, Double> vmClassPercentage) {
		double highVms = nVMs*vmClassPercentage.get(PUClass.High);
		double medVms = nVMs*vmClassPercentage.get(PUClass.Medium);
		double lowVms = nVMs*vmClassPercentage.get(PUClass.Low);
		List<PUClass> vmsToCreate = new ArrayList<PUClass>();
		for (int i=0; i<highVms; ++i) {
			vmsToCreate.add(PUClass.High);
		}
		for (int i=0; i<medVms; ++i) {
			vmsToCreate.add(PUClass.Medium);
		}
		for (int i=0; i<lowVms; ++i) {
			vmsToCreate.add(PUClass.Low);
		}
		Random r = new Random(1);
		List<Vm> vms = new ArrayList<Vm>();
		for (int i = 0; i < nVMs; i++) {
			int vmType = i % PURunnerGeneric.VM_TYPES;
			PUVm vm = new PUVm(
						i,
						broker.getId(),
						PURunnerGeneric.VM_MIPS[vmType],
						PURunnerGeneric.VM_PES[vmType],
						PURunnerGeneric.VM_RAM[vmType],
						PURunnerGeneric.VM_BW,
						PURunnerGeneric.VM_SIZE,
						"Xen",
						new CloudletSchedulerSpaceShared(),
						PlanetLabWorkload.types[vmType]
					);
			int idx = r.nextInt(vmsToCreate.size());
			PUClass cls = vmsToCreate.remove(idx);
			vm.setVmClass(cls);
			vms.add(vm);
		}
		return vms;
	}

}
