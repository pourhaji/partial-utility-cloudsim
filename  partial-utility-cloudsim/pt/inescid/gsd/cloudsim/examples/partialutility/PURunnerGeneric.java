package pt.inescid.gsd.cloudsim.examples.partialutility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import pt.inescid.gsd.cloudsim.partialutility.PUConfig;
import pt.inescid.gsd.cloudsim.partialutility.PUDatacenterBroker;
import pt.inescid.gsd.cloudsim.partialutility.PUHost;
import pt.inescid.gsd.cloudsim.partialutility.VmAllocationPartialUtility_BestFit;
import pt.inescid.gsd.cloudsim.partialutility.VmAllocationPartialUtility_SI_MAX_VIRT_POWER;
import pt.inescid.gsd.cloudsim.partialutility.VmAllocationPolicySimple_PU_DoublePhase;
import pt.inescid.gsd.cloudsim.partialutility.VmSchedulerTimeSharedOverSubscription_SI_MaxVirtPwr_MinClass;
import pt.inescid.gsd.cloudsim.partialutility.VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass;

public class PURunnerGeneric {
	
	public static final boolean RUN_PLANETLAB = true;
	public static final boolean RUN_HUJI = false;
	
	public static final int NUMBER_OF_HOSTS_SMALL_DC = 20;
	public static final int NUMBER_OF_HOSTS_MEDIUM_DC = 40;
	public static final int NUMBER_OF_HOSTS_MEDIUM2_DC = 80;

	// VM types
	public final static int VM_TYPES	= 4;
	public final static int[] VM_MIPS	= { 2500, 2000, 1000, 500 };
	public final static int[] VM_PES	= { 1, 1, 1, 1 };
	
	public final static int[] VM_RAM	= { 3500, 2400, 1700, 1000 };
	public final static int VM_BW		= 100000;
	public final static int VM_SIZE		= 2500; 
	
	public static final String BASE_RESULTS_DIR = ""; // base dir to store results
	public static final File folder = new File(BASE_RESULTS_DIR+"/vm-allocation");

	public static String planetLabBase = ""; // directory with planetlab vms
	public static String planetLabDir1 = ""; // directory with planetlab vms

	public static String hujiIntelWorkload = ""; // directoty with intel's workloads
	public static String hujiRICCWorkload = ""; // directoty with ricc's workloads

	public static final String HUJIWorkload = hujiIntelWorkload;
	
	public static PU_Experience_Configuation currentExperience;

	
	public static void main(String[] args) throws Exception {
		
		PU_DC_Configuration[] DCsConfigurations = {
			new PU_DC_Configuration(
				NUMBER_OF_HOSTS_SMALL_DC,
				2,
				new int[] { 2, 2 },
				new int[] { 1860, 2660 },
				new int[] { 2<<12, 2<<12 } // 4GB
			),
			new PU_DC_Configuration(
				NUMBER_OF_HOSTS_MEDIUM_DC,
				2,
				new int[] { 4, 4 },
				new int[] { 1860, 2660 },
				new int[] { 2 << 13, 2 << 13} // 8GB
			),
			new PU_DC_Configuration(
				NUMBER_OF_HOSTS_MEDIUM2_DC,
				2,
				new int[] { 4, 4 },
				new int[] { 1860, 2660 },
				new int[] { 2 << 13, 2 << 13} // 8GB
			)
			
		};
		
		                                           // low, medium, high
		double[] classesDistribution 	= new double[] {0.3, 0.5, 0.2};
		double[] typesDistribution 		= new double[] {0.1, 0.4, 0.4, 0.1};
		
		PU_Experience_Configuation[] configurations = {

			new PU_Experience_Configuation(
					"FFI-Time",
					VmAllocationPolicySimple.class,
					VmSchedulerTimeShared.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),
				
			new PU_Experience_Configuation(
					"MaxMIPS-MinPwrMinClass-PU",
					VmAllocationPolicySimple_PU_DoublePhase.class,
					VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution,
					true),
					
			// NoShare
			new PU_Experience_Configuation(
					"FFD-Space",
					VmAllocationPartialUtility_BestFit.class,
					VmSchedulerSpaceShared.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),
					
			new PU_Experience_Configuation(
					"FFI-Space",
					VmAllocationPolicySimple.class,
					VmSchedulerSpaceShared.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),		
		
			// Shared
			new PU_Experience_Configuation(
					"FFD-Time",
					VmAllocationPartialUtility_BestFit.class,
					VmSchedulerTimeShared.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),

			new PU_Experience_Configuation(
					"FFI-Time",
					VmAllocationPolicySimple.class,
					VmSchedulerTimeShared.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),
					

			// OverSub
			new PU_Experience_Configuation(
					"FFD-OverSub",
					VmAllocationPartialUtility_BestFit.class,
					VmSchedulerTimeSharedOverSubscription.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),

			new PU_Experience_Configuation(
					"FFI-OverSub",
					VmAllocationPolicySimple.class,
					VmSchedulerTimeSharedOverSubscription.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution),

					
			new PU_Experience_Configuation(
					"MaxMIPS-MaxPwrMinClass-PU",
					VmAllocationPartialUtility_SI_MAX_VIRT_POWER.class,
					VmSchedulerTimeSharedOverSubscription_SI_MaxVirtPwr_MinClass.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution,
					true),	

			new PU_Experience_Configuation(
					"MaxMIPS-MinPwrMinClass-PU",
					VmAllocationPartialUtility_SI_MAX_VIRT_POWER.class,
					VmSchedulerTimeSharedOverSubscription_SI_MinVirtPwr_MinClass.class,
					RamProvisionerSimple.class,
					classesDistribution,
					typesDistribution,
					true),

		};
		
	
		/* Init config */
		PUConfig.RAM_LIMIT =  true;
		
		Log.enable();
		
		
		// small DC
		System.out.println("Small DC");
		for (PU_Experience_Configuation experience : configurations)
			run(experience, DCsConfigurations[0], 20, 76, 2);// 66 host = 300, 404, 2);
		

		// medium DC
		System.out.println("Medium DC");
		for (PU_Experience_Configuation experience : configurations)
			run(experience, DCsConfigurations[1], 174, 274, 4); //174, 274, 4);
			
		
		System.out.println("Medium2 DC");
		for (PU_Experience_Configuation experience : configurations) {
			System.out.println("Running experience " + experience.name );
			run(experience, DCsConfigurations[2], 402, 554, 4); //80 hosts = 690, 858, 8
		}
		
		System.out.println("\n\n+++ All simulations are complete +++\n");
	}

	public static List<Double> allocationsDataAverage;
	public static List<Double> allocationsDataMedian;
	
	private static void run(
			PU_Experience_Configuation experience, 
			PU_DC_Configuration dcConfig,
			int startVMCounter,
			int endVMCounter, 
			int vmIncrement) throws Exception 
	{
		// set globall current experience
		currentExperience = experience;
		
		File experienceFolder = new File(folder.getAbsolutePath()+"/"+dcConfig.numberOfHosts+"hosts");
		if (!experienceFolder.exists())
			experienceFolder.mkdir();
		// VMs-#<num hosts>-<allocPolicy>.cvs
		BufferedWriter vmData = new BufferedWriter(
			new FileWriter(experienceFolder.getAbsolutePath()+
					"/VMs"+"-"+
					dcConfig.numberOfHosts+"-"+
					experience.name+".csv"));
		
		BufferedWriter allocationData = new BufferedWriter(
			new FileWriter(experienceFolder.getAbsolutePath()+
					"/AllocVMs"+"-"+
					dcConfig.numberOfHosts+"-"+
					experience.name+".csv"));
		
		BufferedWriter cloudletPlanetLabData = new BufferedWriter(
			new FileWriter(experienceFolder.getAbsolutePath()+
					"/Cloudlets-PL-"+
					"20-"+//dcConfig.numberOfHosts+"-"+
					experience.name+".csv"));

		BufferedWriter cloudletHujiData = new BufferedWriter(
			new FileWriter(experienceFolder.getAbsolutePath()+
					"/Cloudlets-HJ-"+
					dcConfig.numberOfHosts+"-"+
					experience.name+".csv"));

		vmData.write(
			"nhosts;nVMs;revenue;averageMIPS;medianMIPS;"+
			"requestedRatio;allocatedMIPSRatio;allocatedRAMRatio;"+
			"totalMIPS;allocatedMIPS;failedMIPS;"+
			"LostRevenue;NetRevenue;"+
			"failedByMIPS;failedByRAM;" + 
			"extra;regular;small;micro;" + 
			"optimalRevenue;optimalRevenueGlobal;"+
			"totalFailed\n");
				
		cloudletPlanetLabData.write(
			"num_cloudlets;num_vms;" +
			"wait_time_average;exec_time_average;total_average\n");
		
		cloudletHujiData.write(
			"num_cloudlets;num_vms;" +
			"wait_time_average;exec_time_average;total_average\n");
		
		// create vm and cloudlet submit list
		for (int nVMs = startVMCounter; nVMs <= endVMCounter; nVMs+=vmIncrement) {
			System.out.println("#VM = " + nVMs);
			// PLANETLAB
			if (RUN_PLANETLAB)
			{
				// Initialize the CloudSim package. It should be called
				// before creating any entities.
				int num_user = 1; // number of cloud users
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = true; // mean trace events

				// Initialize the CloudSim library
				CloudSim.init(num_user, calendar, trace_flag);
				
				Datacenter datacenter 		= createDatacenter(experience, dcConfig);
				PUDatacenterBroker broker 	= createBroker(experience);
				broker.RUN_OPTIMAL			=  experience.runOptimal;
	
				allocationsDataAverage = new ArrayList<Double>();
				allocationsDataMedian  = new ArrayList<Double>();
				
				PlanetLabWorkload.runPlanetLab(
						datacenter, 
						broker, 
						nVMs, 
						experience, 
						dcConfig, 
						vmData, 
						cloudletPlanetLabData
				);
				
				allocationData.write(nVMs+";");
				for (Double d : allocationsDataAverage)
					allocationData.write(d+";");				
				allocationData.write("\n");

				allocationData.write(nVMs+";");
				for (Double d : allocationsDataMedian)
					allocationData.write(d+";");				
				allocationData.write("\n");
			}
			
			// HUJI
			if (RUN_HUJI)
			{
				// Initialize the CloudSim package. It should be called
				// before creating any entities.
				int num_user = 1; // number of cloud users
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = true; // mean trace events

				// Initialize the CloudSim library
				CloudSim.init(num_user, calendar, trace_flag);
				
				Datacenter datacenter 		= createDatacenter(experience, dcConfig);
				PUDatacenterBroker broker 	= createBroker(experience);
	
				HujiWorkload.runHuji(
						datacenter, 
						broker, 
						nVMs, 
						experience, 
						dcConfig, 
						vmData, 
						cloudletHujiData
				);
			}
			
		}
		
		vmData.close();
		allocationData.close();
		cloudletPlanetLabData.close();
		cloudletHujiData.close();
	}


	private static PUDatacenterBroker createBroker(PU_Experience_Configuation experience) throws Exception {
		return new PUDatacenterBroker("SI-Phase1-Broker");
	}
	
	public static long getTotalRequestedMIPS(List<Vm> vmList) {
		int requestedMIPS = 0;
		for (Vm vm : vmList) {
			requestedMIPS += vm.getMips();
		}
		return requestedMIPS;
	}

	public static long getDatacenterAvailableMIPS(Datacenter datacenter) {
		long totalMIPS = 0;
		List<Host> hostList = datacenter.getHostList();
		for (Host h : hostList) {
			totalMIPS += h.getTotalMips();
		}
		return totalMIPS;
	}
	
	public static List<Double> getDatacenterListAvailableMIPS(Datacenter datacenter) {
		List<Double> availableMIPS = new ArrayList<>();
		List<Host> hostList = datacenter.getHostList();
		for (Host h : hostList) {
			availableMIPS.add((double)h.getTotalMips());
		}
		return availableMIPS;
	}
	
	
	public static long getDatacenterAvailableRAM(Datacenter datacenter) {
		long totalRAM = 0;
		List<Host> hostList = datacenter.getHostList();
		for (Host h : hostList) {
			totalRAM += h.getRam();
		}
		return totalRAM;
	}

	public static long getTotalRAM(List<Vm> vmList) {
		int requestedRAM = 0;
		for (Vm vm : vmList) {
			requestedRAM += vm.getRam();
		}
		return requestedRAM;
	}
	
	public static Datacenter createDatacenter(
			PU_Experience_Configuation experience,
			PU_DC_Configuration dcConfig) throws Exception 
	{
		// 1. We need to create a list to store our machine
		List<PUHost> hostList = createHostList(
				dcConfig, 
				experience.vmScheduler, 
				experience.ramProvisioner
		);

		// dummy values
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// Finally, we need to create a Datacenter object.
		Datacenter datacenter = null;
		Constructor<?> ctr = experience.allocationToHost.getConstructor(List.class);
		VmAllocationPolicySimple hostAllocator = (VmAllocationPolicySimple) ctr.newInstance(hostList);
		
		datacenter = new Datacenter(
				experience.name, 
				characteristics, 
				hostAllocator, 
				storageList, 
				/* !! Constants.SCHEDULING_INTERVAL*/
				Constants.SCHEDULING_INTERVAL);

		return datacenter;
	}

	public static <T, W> List<PUHost> createHostList(
			PU_DC_Configuration dcConfig, 
			Class<T> vmmScheduler,
			Class<W> ramProvisioner) 
					throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException 
	{
		List<PUHost> hostList = new ArrayList<PUHost>();
		for (int i = 0; i < dcConfig.numberOfHosts; i++) {
			int hostType = i % dcConfig.hostTypes;

			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < dcConfig.hostPES[hostType]; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(dcConfig.hostMIPS[hostType])));
			}
			
			Constructor<T> ctr = vmmScheduler.getConstructor(List.class);
			VmScheduler vmScheduler = (VmScheduler) ctr.newInstance(peList);

			Constructor<W> ramCtr = ramProvisioner.getConstructor(int.class);
			
			hostList.add(new PUHost(
					i,
					(RamProvisioner) ramCtr.newInstance(dcConfig.hostRAM[hostType]),
					new BwProvisionerSimple(org.cloudbus.cloudsim.examples.power.Constants.HOST_BW),
					org.cloudbus.cloudsim.examples.power.Constants.HOST_STORAGE,
					peList,
					vmScheduler));
		}
		return hostList;
	}

}
