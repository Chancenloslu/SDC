package scheduler;

import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;

public class Main {

	public static void main(String[] args) {

		/* this lpwizard solver has no meaning, just for testing the function of SCP solver at the beginning.
		* It would be better to keep it. See README.md for details. */
		LPWizard lpw = new LPWizard();
		lpw.plus("t_total",1.0);
		//lpw.addConstraint("c1",1,"=").plus("d1",1);

		lpw.addConstraint("dd1",-1,">=").plus("n1",1.0).plus("n3",-1.0);
		lpw.addConstraint("dd2",-1,">=").plus("n2",1.0).plus("n3",-1.0);
		lpw.addConstraint("dd3",-1,">=").plus("n4",1.0).plus("n6",-1.0);
		lpw.addConstraint("dd4",-1,">=").plus("n7",1.0).plus("n10",-1.0);
		lpw.addConstraint("dd5",-1,">=").plus("n8",1.0).plus("n11",-1.0);
		lpw.addConstraint("dd6",-1,">=").plus("n3",1.0).plus("n5",-1.0);
		lpw.addConstraint("dd7",-1,">=").plus("n5",1.0).plus("n9",-1.0);
		lpw.addConstraint("dd8",-1,">=").plus("n6",1.0).plus("n9",-1.0);

		lpw.addConstraint("rc1",-1,">=").plus("n1",1.0).plus("n2",-1.0);
		lpw.addConstraint("rc2",-1,">=").plus("n2",1.0).plus("n3",-1.0);
		lpw.addConstraint("rc3",-1,">=").plus("n3",1.0).plus("n4",-1.0);
		lpw.addConstraint("rc4",-1,">=").plus("n4",1.0).plus("n6",-1.0);
		lpw.addConstraint("rc5",-1,">=").plus("n6",1.0).plus("n7",-1.0);
		lpw.addConstraint("rc6",-1,">=").plus("n5",1.0).plus("n8",-1.0);
		lpw.addConstraint("rc7",-1,">=").plus("n8",1.0).plus("n9",-1.0);
		lpw.addConstraint("rc8",-1,">=").plus("n9",1.0).plus("n10",-1.0);
		lpw.addConstraint("rc9",-1,">=").plus("n10",1.0).plus("n11",-1.0);

		lpw.setMinProblem(true);
		LPSolution lps = lpw.solve();
		//System.out.println(lps);

		/* start parse the graph and resource constraints */
		RC rc = null;

		if (args.length>1){
			System.out.println("Reading resource constraints from "+args[1]+"\n");
			rc = new RC();
			rc.parse(args[1]);
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

		//sched1.draw("scheduler-framework-master/schedules/ASAP_" + problemName, problemName, null);

		s = new ALAP();
		Schedule sched2 = s.schedule(g);
		System.out.printf("%nALAP%n%s%n", sched2.diagnose());
		System.out.printf("cost = %s%n", sched2.cost());

		//sched2.draw("scheduler-framework-master/schedules/ALAP_" + problemName, problemName, null);
		System.out.println("\n\n**************************************");
		System.out.println("****              SDC             ****");
		long startTime=System.currentTimeMillis();
		SDC sdc = new SDC(sched1, sched2, rc);
		Schedule sched3 = sdc.runSolver(0);
		long endTime=System.currentTimeMillis();
		System.out.println("run time of SDC: " + (endTime - startTime) + "ms");
		sched3.draw("scheduler-framework-master/schedules/SDCScheduler_" + problemName, problemName,null);

		System.out.println("\n\n**************************************");
		System.out.println("****       list scheduler         ****");
		startTime = System.currentTimeMillis();
		s = new ListScheduler(rc);
		Schedule sched4 = s.schedule(g);
		endTime = System.currentTimeMillis();
		System.out.printf("%nlist scheduler%n%s%n", sched4.diagnose());
		System.out.printf("cost  = %s%n", sched4.cost());
		System.out.println("run time of list scheduler: " + (endTime - startTime) + "ms");
		sched4.draw("scheduler-framework-master/schedules/ListScheduler_" + problemName, problemName,null);

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
