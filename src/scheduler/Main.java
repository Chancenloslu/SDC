package scheduler;

public class Main {

	public static void main(String[] args) {

		RC rc = null;
		String resourcesName = null;

		if (args.length>1){
			System.out.println("Reading resource constraints from "+args[1]+"\n");
			rc = new RC();
			rc.parse(args[1]);
			resourcesName = args[1];
		}
		
		ProblemReader dr = new ProblemReader(false);
		if (args.length < 1) {
			System.err.printf("Usage: scheduler dotfile%n");
			System.exit(-1);
		}else {
			System.out.println("Scheduling "+args[0]);
			System.out.println();
		}

		String problemName = args[0].substring(args[0].lastIndexOf("/")+1);
		
		Graph g = dr.parse(args[0]);
		System.out.printf("%s%n", g.diagnose());

		Scheduler s = new ASAP();
		Schedule sched1 = s.schedule(g);
		System.out.printf("%nASAP%n%s%n", sched1.diagnose());
		System.out.printf("cost = %s%n", sched1.cost());

		sched1.draw("scheduler-framework-master/schedules/ASAP_" + problemName, problemName, null);

		s = new ALAP();
		Schedule sched2 = s.schedule(g);
		System.out.printf("%nALAP%n%s%n", sched2.diagnose());
		System.out.printf("cost = %s%n", sched2.cost());

		sched2.draw("scheduler-framework-master/schedules/ALAP_" + problemName, problemName, null);

//		Reorder reord = new Reorder(rc);
//		Schedule sched3 = reord.schedule(sched1, sched2);
		SDC sdc = new SDC(sched1, sched2, rc);
		sdc.generateEq();


		/* exemplary validation of a schedule */
		/*
		Node conflictingNode = sched.validateDependencies();
		if (conflictingNode != null) {
			System.out.println("Schedule validation failed. First conflicting node: " + conflictingNode.id);
		} else {
			System.out.println("Dependency validation successful.");
		}

		Node overusingNode = sched.validateResources();
		if (overusingNode != null) {
			System.out.println("Resource usage validation failed. First overuse by node " + overusingNode.id);
		} else {
			System.out.println("Resource usage validation successful.");
		}*/

	}
}
