package valueframework.common;


import java.util.ArrayList;

import normFramework.Group;
import normFramework.Norm;

public class GroupAgentConnector {
		
	public ArrayList<Norm> getGroupNorm(int agentId, int groupId){
		for (Group group : FrameworkBuilder.getGroups()) {
			if(groupId == group.getId())
				break;
			if (!group.getMemberAgents().contains(agentId)) {
				Log.printError("agent " + agentId + " is not a member of group " + groupId);
				return null;
			}
			return group.getNorms();
		}
		Log.printError("agent " + agentId + "cannot find norms of group " + groupId);
		return null;
	}
}
