import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.Random;

public class SimulationOnePerPalet {

	public static double clock, totalTime, sumServiceTime, sumPackingTime, sumSuccessTime, sumFailTime, sumRepackTime,
			sumInspectionTime, sumShipTime, lastArrivalTimeBoxes, lastPaletDeparture, lastArrivalTimeMonitor, sumInterArrivalTimeMonitor, clockPerDay,
			sumInterArrivalTimeBoxes, packingTime, repackingTime, inspectionTime, totalBoxingTime, sumExtraTime;

	public static int monitorsCounter, boxesCounter, numArrivals, numDepartures, numPacked, numInspected, numShipped, packer, extraHoursDay,
			numFails, numSuccess, instectorStatus, toInspection, toPacking, palet, toShipping, workingDays, maxPaletQ, paletQueue, numPalet;

	public static PriorityQueue<Event> boxes;
	public static PriorityQueue<Event> monitors;
	public static PriorityQueue<Event> paletQ;
	public static PriorityQueue<Event> inspect;

	public static int boxesQ, maxBoxesQ, monitorsQ, maxMonitorQ;

	public final static int arrival = 1;
	public final static int departure = 2;

	// declare FEL, RNG
	public static EventList FutureEventList;
	public static Random stream;

	// main method
	public static void main(String argv[]) {

		// initialize RNG
		long seed = 12345678;
		stream = new Random(seed);

		// initialize the FEL
		FutureEventList = new EventList();

		// call Initialization method for remaining initializations
		Initialization();

		while (workingDays < 100) {
			
			clockPerDay = 0;
			// loop until meet stopping condition
			while (clockPerDay < 480) {

				Event evt = (Event) FutureEventList.getMin(); // get imminent
																// event
				FutureEventList.dequeue(); // be rid of it
				clock = evt.getTime(); // advance simulation time
				clockPerDay = clock - (workingDays * 480);
				totalTime += clock;
				

				// determine event type and call handler
				if (evt.getType() == arrival){
					ProcessArrival(evt);
				}
				
				if (evt.getType() == 3){
					inspection();
				}
				if (evt.getType() == 4){
					shipping();
				}
				if (evt.getType() == departure){
					ProcessDeparture(evt);
				}
				
				if (clockPerDay > 480){
					sumExtraTime = clockPerDay - 480;
					extraHoursDay ++;
				}
				
			}

		workingDays++;

		}

		// simulation has stopped call ReportGen method
		ReportGeneration();
	}

	// perform all initializations including FEL
	public static void Initialization() {
		clock = 0.0;
		totalTime = 0.0;
		sumPackingTime = 0.0;
		sumSuccessTime = 0.0;
		sumFailTime = 0.0;
		sumExtraTime = 0.0;
		sumRepackTime = 0.0;
		sumShipTime = 0.0;
		totalBoxingTime = 0.0;
		lastArrivalTimeBoxes = 0.0;
		lastArrivalTimeMonitor = 0.0;
		sumInterArrivalTimeMonitor = 0.0;
		sumInterArrivalTimeBoxes = 0.0;
		lastPaletDeparture = 0.0;

		monitorsCounter = 0;
		boxesCounter = 0;
		numArrivals = 0;
		numPalet = 0;
		palet = 0;
		numDepartures = 0;
		numPacked = 0;
		numInspected = 0;
		numShipped = 0;
		toPacking = 0;
		numFails = 0;

		workingDays = 0;
		clockPerDay = 0;
		extraHoursDay = 0;

		instectorStatus = 1;
		packer = 1;

		boxes = new PriorityQueue<Event>();
		monitors = new PriorityQueue<Event>();
		paletQ = new PriorityQueue<Event>();
		inspect = new PriorityQueue<Event>();

		boxesQ = 0;
		maxBoxesQ = 0;
		monitorsQ = 0;
		maxMonitorQ = 0;
		paletQueue = 0;
		maxPaletQ = 0;

		double boxesArrivalTime = exponential(stream, 13.5);
		Event evt = new Event(arrival, boxesArrivalTime);
		evt.setKind(1);
		FutureEventList.enqueue(evt);

		double monitorArrivalTime = exponential(stream, 15);
		Event evt2 = new Event(arrival, monitorArrivalTime);
		evt2.setKind(2);
		FutureEventList.enqueue(evt2);
	}

	// EVENT HANDLER METHODS

	public static void ProcessArrival(Event evt) {

		numArrivals++;

		if (evt.getKind() == 1) {

			boxesCounter++;
			boxes.add(evt);
			boxesQ++;

			if (boxes.size() >= 0 && monitors.size() >= 1) {

				if (packer == 1 && clockPerDay < 480) {

					ScheduleDeparture();
				}

				else {
					if (maxBoxesQ < boxesQ)
						maxBoxesQ = boxesQ;
				}

			} else {
				if (maxBoxesQ < boxesQ)
					maxBoxesQ = boxesQ;
			}

			Event next_arrival = new Event(arrival, (clock + exponential(stream, 13.5)));
			next_arrival.setKind(1);
			FutureEventList.enqueue(next_arrival);

			double timeSinceLastBoxArrival = clock - lastArrivalTimeBoxes;
			sumInterArrivalTimeBoxes += timeSinceLastBoxArrival;
			lastArrivalTimeBoxes = clock;

		}

		else {

			monitorsCounter++;
			monitors.add(evt);
			monitorsQ++;

			if (monitors.size() >= 0 && boxes.size() >= 1) {

				if (packer == 1 && clockPerDay < 480) {

					ScheduleDeparture();
				} else {

					if (maxMonitorQ < monitorsQ)
						maxMonitorQ = monitorsQ;
				}

			} else {

				if (maxMonitorQ < monitorsQ)
					maxMonitorQ = monitorsQ;
			}

			Event next_arrival2 = new Event(arrival, (clock + exponential(stream, 15)));
			next_arrival2.setKind(2);
			FutureEventList.enqueue(next_arrival2);

			double timeSinceLastMonitorArrival = clock - lastArrivalTimeMonitor;
			sumInterArrivalTimeMonitor += timeSinceLastMonitorArrival;
			lastArrivalTimeMonitor = clock;
		}
	}

	public static void ScheduleDeparture() {
		
		double serviceTime;

		toPacking++;
		
		packer = 0;

		Event evt = monitors.poll();
		Event evt2 = boxes.poll();

		monitorsQ--;
		boxesQ--;

			packingTime = normal(stream, 5, 1);
			sumPackingTime += packingTime;

			serviceTime = packingTime;
			sumServiceTime += serviceTime;
			

		Event e = new Event(3, (clock + serviceTime));
		inspect.add(e);
		FutureEventList.enqueue(e);
		
		

	}
	
	public static void inspection() {
		
		double serviceTime;
		
		toInspection++;
		packer = 1;
		instectorStatus = 0;
		
		Event evt = inspect.poll();
		
		if ((stream.nextDouble()) < 0.12) {
			
			numFails++;
			
			inspectionTime = normal(stream, 3, 1);
			sumInspectionTime += inspectionTime;
			
			repackingTime = normal(stream, 6, 2);
			sumRepackTime += repackingTime;
			
			serviceTime = inspectionTime + repackingTime;
			
			
		}
		
		else {
			
			inspectionTime = normal(stream, 4, 2);
			sumInspectionTime += inspectionTime;
			
			numSuccess++;
			
			serviceTime = inspectionTime;
			
			
		}
		
		Event e = new Event(4, (clock + serviceTime));
		paletQ.add(e);
		FutureEventList.enqueue(e);
	}
	
	public static void shipping() {

		double shippingTime;
		
		instectorStatus = 1;
		
		toShipping ++;
		
		palet++;
		
		if (clock >= lastPaletDeparture) {
			
			shippingTime = (uniform(stream, 3, 1) + 2 + 1);
				
				Event evt = paletQ.poll();
				
				sumShipTime += shippingTime;
				
				palet --;
			
			numPalet ++;
			lastPaletDeparture = clock + shippingTime;
			
			
			Event e = new Event(departure, (clock + shippingTime));
			FutureEventList.enqueue(e);
			
			if (maxPaletQ < palet)
				maxPaletQ = palet;
		}
	}

	public static void ProcessDeparture(Event e) {

		numDepartures++;

		if (boxes.size() > 0 && monitors.size() > 0) {
			if(packer == 1 && clockPerDay < 480){
				ScheduleDeparture();
			}
		}
	}

	public static void ReportGeneration() {

		DecimalFormat percent = new DecimalFormat("#0.0#%");
		DecimalFormat pretty = new DecimalFormat("#0.##");
		
		double totalPacking = sumPackingTime + sumRepackTime;
		totalBoxingTime = (totalPacking/toPacking) + (sumInspectionTime/toInspection) + ( sumShipTime/ toShipping);

		System.out.println("Report: Shipping Boxes Unlimited \n");
		
		System.out.println("\nThe simulation ran for " + pretty.format(workingDays) + " days which represents "
				+ pretty.format(clock / 60) + " hours of work.");
		
		System.out.println("\n1.");
		System.out.println("The average of boxes and monitor entering the packing area is equal to: " + pretty.format((double)(toPacking)/100) + " per day.");
		System.out.println("The average of inspected boxes and monitor are equal to: " + pretty.format((double)toInspection/100) + " per day.");
		System.out.println("The average of packed and shipped boxes and monitor are equal to: " + pretty.format((double)(numDepartures)/100) + " per day.");
		
		
		System.out.println("\n2.");
		System.out.println("The total packing time is " + pretty.format ((totalPacking /100)/60) + " hours per day, which represents " + percent.format ((totalPacking /100)/60/8));
		System.out.println("The total inspection time is " + pretty.format ((sumInspectionTime /100)/60) + " hours per day, which represents " + percent.format ((sumInspectionTime /100)/60/8));
		System.out.println("The total shipping time is " + pretty.format ((sumShipTime /100)/60) + " hours per day, which represents " + percent.format ((sumShipTime /100)/60/8));
		
		System.out.println("\n3.");
		System.out.println("\nThe average number of departing boxes and monitor packed is: " + (double)(numDepartures)/100);
		
		System.out.println("\n4.");
		System.out.println("Over 100, employees would have to work late " + pretty.format(extraHoursDay) + " days with an average of " + pretty.format(sumExtraTime) + " minutes per days.");
		System.out.println("The total average boxing time of a monitor is " + pretty.format(totalBoxingTime) + " minutes, so if we make the employees stop before arround this time they shouldn't have extra hours.");
		System.out.println("After simulation (SimulationNoLatework), if the employees do not start any task in the last 21 minutes they shouldn't have any extra hours.");
		
		
		
		
		System.out.println("\n\n\nVerification data");
		System.out.println("\nThe number of arriving boxes: " + boxesCounter);
		System.out.println("\nThe number of arriving monitors: " + monitorsCounter);

		System.out.println("\nNumber of monitors in queue at the end of the simulation is: " + monitorsQ);
		System.out.println("\nNumber of boxes in queue at the end of the simulation: " + boxesQ);

		System.out.println("\nNumber of max monitors in queue during the simulation is: " + maxMonitorQ);
		System.out.println("\nNumber of max boxes in queue during the simulation is: " + maxBoxesQ);

		System.out
				.println("\nPercent of fail inspection: " + percent.format((double) numFails / (double) toInspection));

		System.out.println("\nNumber of fail inspection: " + pretty.format(numFails));

		System.out.println(
				"\nPercent of success inspection: " + percent.format((double) numSuccess / (double) toInspection));

		System.out.println("\nNumber of success inspection: " + pretty.format(numSuccess));

		System.out.println("\nThe total service time is " + pretty.format(sumServiceTime / 60) + " hours");

		System.out.println("\nAverage interarrival time boxes: "
				+ pretty.format(sumInterArrivalTimeBoxes / boxesCounter) + " minutes");

		System.out.println("\nAverage interarrival time monitors: "
				+ pretty.format(sumInterArrivalTimeMonitor / monitorsCounter) + " minutes");

	}

	public static double uniform(Random rng, double m, double h) {
		double a = m - h;
		double b = m + h;

		return a + ((b - a) * rng.nextDouble());
	}

	public static double exponential(Random rng, double mean) {
		return -mean * Math.log(rng.nextDouble());
	}

	public static double SaveNormal;
	public static int NumNormals = 0;
	public static final double PI = 3.1415927;

	public static double normal(Random rng, double mean, double sigma) {
		double ReturnNormal;

		if (NumNormals == 0) {
			double r1 = rng.nextDouble();
			double r2 = rng.nextDouble();
			ReturnNormal = Math.sqrt(-2 * Math.log(r1)) * Math.cos(2 * PI * r2);
			SaveNormal = Math.sqrt(-2 * Math.log(r1)) * Math.sin(2 * PI * r2);
			NumNormals = 1;
		} else {
			NumNormals = 0;
			ReturnNormal = SaveNormal;
		}
		return ReturnNormal * sigma + mean;
	}

}
