package valueframework.example;

import java.util.ArrayList;

import normFramework.Group;
import valueframework.common.FrameworkBuilder;
import valueframework.common.Log;

public class Test {
	
	public static void main(String [] args) {

		//first create value files
		FrameworkBuilder.initialize();
		Log.printStars();
		ArrayList<Agent> agents = new ArrayList<Agent>();
		ArrayList<Group> groups = new ArrayList<Group>();
		
		Log.printLog("Create agents");
		agents.add(new Agent(0, 0.80));
		agents.add(new Agent(1, 0.80));
		agents.add(new Agent(2, 0.80));
		agents.add(new Agent(3, 0.80));
		
		Log.printLog("Create groups");
		//new groups
		//assign norms to groups
		//assign groups to agents
		//groups are not in the input list as we want them to be dynamic
		Group g1 = new Group(100);
		g1.addNorm(FrameworkBuilder.getNormByName("Donate nothing"), "have to");
		groups.add(g1);
		Group g2 = new Group(101);
		g2.addNorm(FrameworkBuilder.getNormByName("Donate to council"), "have to");
		groups.add(g2);
		
		agents.get(0).becomeGroupMember(g1);
		agents.get(1).becomeGroupMember(g1);
		agents.get(2).becomeGroupMember(g2);
		agents.get(3).becomeGroupMember(g2);
		
		
		FrameworkBuilder.setAllAgents(agents);
		FrameworkBuilder.setAllGroups(groups);
		theoryTest(100, agents);
	}

	private static void theoryTest(int steps, ArrayList<Agent> agents) {
		
		for (int i = 1; i <= steps; i++) {
			
			Log.printLog("********************** Step " + i + " ************************");
			Log.printLog("Drain step");
			for (Agent agent : agents) {
				agent.stepDrainTanks();
			}
			
			Log.printStars();
			Log.printLog("Take action");
			for (Agent agent : agents) {
				agent.stepAction();
			}
		}
	}
}