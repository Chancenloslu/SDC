package scheduler;

public class Validator {

	public static void main(String[] args) {

		Schedule schedule = null;
		String problemName = null;
		String resourceName = null;

		if (args.length>0){
			System.out.println("Reading schedule from "+args[0]+"\n");
			ScheduleReader reader = new ScheduleReader();
			schedule = reader.parse(args[0]);
			problemName = reader.getProblemName();
			resourceName = reader.getResourceName();
		}

		System.out.println("Problem name: " + problemName);
		System.out.println("Resource file: " + resourceName);
		System.out.printf("\n" + schedule.diagnose());

		//schedule.draw("schedules/Validator.dot", problemName, resourceName);

		/* validate the schedule */

		Node conflictingNode = schedule.validateDependencies();
		if (conflictingNode != null)
			System.out.println("Schedule validation failed. First conflicting node: " + conflictingNode.id);

		Node overusingNode = schedule.validateResources();
		if (overusingNode != null)
			System.out.println("Resource usage validation failed. First overuse by node " + overusingNode.id);
	}
}
