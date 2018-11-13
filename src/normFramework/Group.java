package normFramework;

import java.util.ArrayList;

import valueframework.common.Log;

public class Group {
	private int id;
	private ArrayList<Norm> norms;
	//here we assign norms to a group as it is what the simulator think as a norm of a group. 
	//each agent has its own norms which are his interpretation of group norms. 
	private ArrayList<Integer> memberAgents;
	
	public Group(int gid){
		id = gid;
		norms = new ArrayList<Norm>();
		memberAgents = new ArrayList<Integer>();
	}
	public void addNorm(Norm newNorm, String normType){
		Log.printLog("new norm : " + newNorm.getTitle() + "is added for group " + id + ", it's type is " + newNorm.getNormType());
		newNorm.setNormType(normType);
		norms.add(newNorm);
	}
	
	public ArrayList<Norm> getNorms(){
		return norms;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<Integer> getMemberAgents() {
		return memberAgents;
	}
	public void setMemberAgents(ArrayList<Integer> memberAgents) {
		this.memberAgents = memberAgents;
	}
	public void setNorms(ArrayList<Norm> norms) {
		this.norms = norms;
	}
	public void addMember(int agentID){
		this.memberAgents.add(agentID);
	}
	
}
