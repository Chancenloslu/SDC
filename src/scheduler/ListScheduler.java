package scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * This class implements List Scheduling
 *
 */
public class ListScheduler extends Scheduler {

	private RC resources;	

	Schedule sched = null;

	public ListScheduler(RC resources){
		this.resources = resources;		
	}

	

	@Override
	public Schedule schedule(Graph sg) {

		ASAP asap = new ASAP();
		Schedule asapSchedule = asap.schedule(sg);

		ALAP alap = new ALAP();
		Schedule alapSchedule = alap.schedule(sg);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		Iterator<Node> it = sg.iterator();
		while(it.hasNext()){
			nodes.add(it.next());
		}
		
		HashMap<Node, Integer> mobilityMap = new HashMap<Node, Integer>();
	
		int maxMobility=0;
		for(Node n: nodes){
			int mobility = alapSchedule.slot(n).ubound - asapSchedule.slot(n).ubound;
			maxMobility = mobility > maxMobility ? mobility : maxMobility;
			mobilityMap.put(n, mobility);
		}

		ArrayList<Node> priorityList = new ArrayList<Node>();

		int priority = 0; // 0 is highest priority ( minimum mobility )
		while(priority <= maxMobility){
			List<Node> removeNodes = new ArrayList<Node>();
			for(Map.Entry<Node, Integer> e : mobilityMap.entrySet()){
				Node node = e.getKey();
				int tempPriority = e.getValue();
				if(tempPriority == priority){
					priorityList.add(node);
					removeNodes.add(node);
				}
			}
			for(Node n : removeNodes){
				mobilityMap.remove(n);
			}
			priority++;
		}
		for(Node n : priorityList){
			System.out.println(n);
		}

		sched = new Schedule();
		HashMap<String, Integer> usedResources = new HashMap<String, Integer>();

		int timeSlot = 0;
		while(!priorityList.isEmpty()){
			freeResources(usedResources, timeSlot);
			ArrayList<Node> nodesToRemove = new ArrayList<Node>();
			for(Node n : candidates(priorityList)){
				Set<String> pes = resources.getRes(n.getRT());
				if(pes.isEmpty()){
					System.out.println("Can not schedule " + n.getRT().name + ". No PE can execute this instruction.");
					System.exit(-1);
				}
				Iterator<String> peit = pes.iterator();
				while(peit.hasNext()){
					String res = peit.next();
					if(!usedResources.containsKey(res)){
						int end = timeSlot + n.getDelay() - 1;
						sched.add(n, new Interval(timeSlot, end),res);
						usedResources.put(res, end);
						nodesToRemove.add(n);
						break;
					}
				}
			}
			priorityList.removeAll(nodesToRemove);
			handleNodes(sched, timeSlot);
			timeSlot++;
		}
		return sched;
	}

	

	private void freeResources(Map<String, Integer> resources, int timeSlot){		
		ArrayList<String> toRemove = new ArrayList<String>();
		for(Map.Entry<String, Integer> e : resources.entrySet()){
			if(e.getValue() < timeSlot){
				toRemove.add(e.getKey());
			}
		}
		for(String s: toRemove){
			resources.remove(s);
		}
	}

	

	private void handleNodes(Schedule sched, int timeSlot){
		Set<Node> nodes = sched.nodes(timeSlot);
		if(nodes == null){
			return;
		}
		for(Node n : nodes){
			if(sched.nodes( timeSlot + 1 ) == null || !sched.nodes(timeSlot+1).contains(n)){
				for(Node suc : n.successors()){
					suc.handle(n);
				}
			}
		}
	}


	private List<Node> candidates(List<Node> nodes){
		ArrayList<Node> candidates = new ArrayList<Node>();
		for(Node n: nodes){
			if(n.top()){
				candidates.add(n);
			}
		}
		return candidates;
	}
}

