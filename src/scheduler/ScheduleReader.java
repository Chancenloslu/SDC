package scheduler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads DOT-ish files. (see graphviz)
 * Used here for reading schedule files according to the output format of the framework.
 * <p>
 * The reader reads the file line-wise expecting at most one expression per line.
 * It distinguishes between the following types of expressions:
 * 1. Node definition containing a label in the format "id|{resource type|resource|start timestep|end timestep}"
 * 2. Link between two nodes
 * 3. graph label (containing the scheduling problem name)
 * 4. graph comment (containing the resource file name, if resource constrained)
 */
public class ScheduleReader {
	private Schedule schedule;
	private String problemName;
	private String resourceName;

	public ScheduleReader() {
		schedule = new Schedule();
	}
	
	private void lex(BufferedReader input) {

		/* regex definitions */
		Pattern pat_def = Pattern.compile("(\\w\\w*)(\\[.*label=\")(.+?(?=\"))(.*]);.*");
		Pattern pat_nodeLabel = Pattern.compile("(\\w\\w*)(\\|\\{)(\\w*)(\\|)(\\w*)(\\|)(\\d+)(\\|)(\\d+)(})");
		Pattern pat_use = Pattern.compile("(\\w\\w*) -> (\\w\\w*);.*");
		Pattern pat_graphLabel = Pattern.compile("(label=\")(.*?(?=\"))(\")");
		Pattern pat_graphComment = Pattern.compile("(comment=\")(.*?(?=\"))(\")");

		String line;
		Matcher m;

		/* maps for storing parsing results */
		Map<String, Node> parsedNodes = new HashMap<>();
		Map<Node, Interval> parsedIntervals = new HashMap<>();
		Map<Node, String> parsedResources = new HashMap<>();
		Map<String, Set<String>> parsedLinks = new HashMap<>();

		try {
			line = input.readLine();
			while (line != null) {

				/* parse node definition */
				m = pat_def.matcher(line);

				if (m.matches()) {

					String id = m.group(1);
					String label = m.group(3);

					/* parse node label */
					m = pat_nodeLabel.matcher(label);

					if (m.matches()) {

						String type = m.group(3);
						String resource = m.group(5);

						int start = Integer.parseInt(m.group(7));
						int end = Integer.parseInt(m.group(9));

						Node n = new Node(id, RT.valueOf(type));

						parsedNodes.put(id, n);
						parsedIntervals.put(n, new Interval(start, end));

						if (!resource.equals("-")) {
							parsedResources.put(n, resource);
						}
					}
				}

				/* parse links */
				m = pat_use.matcher(line);
				if (m.matches()){
					parsedLinks.putIfAbsent(m.group(1), new HashSet<>());
					parsedLinks.get(m.group(1)).add(m.group(2));
				}

				/* parse graph label (problem name) */
				m = pat_graphLabel.matcher(line);
				if (m.matches()){
					problemName = m.group(2);
				}

				/* parse graph comment (resource file name) */
				m = pat_graphComment.matcher(line);
				if (m.matches()){
					resourceName = m.group(2);
				}

				line = input.readLine();
			}
		} catch (Throwable e) {
			System.err.printf("FATAL: Could not read from input%n");
			e.printStackTrace(System.err);
			System.exit(-1);
		}

		/* build the schedule node-wise */
		for (String nodeId : parsedNodes.keySet()) {

			Node n = parsedNodes.get(nodeId);

			for (String successor : parsedLinks.getOrDefault(nodeId, Collections.emptySet())) {
				n.append(parsedNodes.get(successor), 0);
			}
			schedule.add(n, parsedIntervals.get(n), parsedResources.get(n));
		}
	}

	/**
	 * @return the name of the scheduling problem as read from the graph label
	 */
	public String getProblemName() {
		return problemName;
	}

	/**
	 * @return the name of the resource file as read from the graph comment
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * Parses the open stream.
	 */
	public Schedule parse(String fn) {
		BufferedReader file_reader;
		
		schedule = new Schedule();
		
		try {
			file_reader = new BufferedReader(new FileReader(fn));
			lex(file_reader);
		} catch (FileNotFoundException e) {
			System.err.printf("FATAL: File not found: %s%n", fn);
			System.exit(-1);
		}
		
		return schedule;
	}
}
