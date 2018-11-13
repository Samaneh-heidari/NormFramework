package normFramework;

import java.io.Serializable;
import java.util.ArrayList;

import valueframework.Node;
import valueframework.common.Log;

public class Norm implements Serializable{
	private String condition;
	private String title;
	private String postCondition;
	private ArrayList<String> violationList;
	private ArrayList<String> relatedValues;
	private ArrayList<Integer> relationOfValues; //1 if it's positively related and -1 if it's negatively related.
	//TODO: access to this arraylist should be syn with access to relatedValues list.
	private String normType; //"should", "have to", "internalized"
	//relatedValues is only title of the abstract value, as we don't want to make lots of copies of 
	//values for each agents and for each norm that an agent create.
	//TODO: check if it's good to keep the node, or it's better to keep a string.
	
	public Norm(String nCondition, String nActionTitle, String nPostcondition, String normType, ArrayList<String> nRelatedValues, ArrayList<Integer> nRelationOfValues, ArrayList<String> violationList) {
		this.condition = nCondition;
		this.title = nActionTitle;
		this.postCondition = nPostcondition;
		this.normType = normType;
		if(nRelatedValues.size() != nRelationOfValues.size()){
			Log.printError("the size of two lists : relatedValues and relationOfValues are not equal");
			try {
				throw new Exception("Samaneh comment: Size doesn't match");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.relatedValues = new ArrayList<String>();
		this.relatedValues.addAll(nRelatedValues);
		this.relationOfValues = new ArrayList<Integer>();
		this.relationOfValues.addAll(nRelationOfValues);
		this.violationList = new ArrayList<String>();
		this.violationList.addAll(violationList);
	}
	
	@SuppressWarnings("unchecked")
	public Norm(Norm normCopy) {
//		this(normCopy.getCondition(),normCopy.getTitle(),normCopy.getPostCondition(), (ArrayList<String>) normCopy.getRelatedValues().clone(),(ArrayList<Integer>) normCopy.getRelationOfValues().clone());
		this.condition = normCopy.getCondition();
		this.title = normCopy.getTitle();
		this.relatedValues = (ArrayList<String>) normCopy.getRelatedValues().clone();
		this.relationOfValues = (ArrayList<Integer>) normCopy.getRelationOfValues().clone();
		this.postCondition = normCopy.getPostCondition();
		this.normType = normCopy.getNormType();
		this.violationList = (ArrayList<String>) normCopy.getViolationList().clone();
	}
	
	
	private int groupId; //if type is "internalized", then groupId is an agentID.
	
	public void applyPostConditions(){
//		TODO: it might need to have access to the agent to change his social status or ...
		
	}
	
	public boolean checkConditions(){
		//		TODO: it might need to have access to the environment to check all the conditions. Maybe, it's better to put this method somewhere else
		return false;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String actionTitle) {
		this.title = actionTitle;
	}

	public String getPostCondition() {
		return postCondition;
	}

	public void setPostCondition(String postCondition) {
		this.postCondition = postCondition;
	}

	public String getType() {
		return normType;
	}

	public void setType(String type) {
		this.normType = type;
	}

	public int getGroupId() {
		return groupId;
	}

	public ArrayList<String> getRelatedValues() {
		return relatedValues;
	}

	public void setRelatedValues(ArrayList<String> nrelatedValues) {
		this.relatedValues = nrelatedValues;
	}

	public String getNormType() {
		return normType;
	}

	public void setNormType(String normType) {
		this.normType = normType;
	}

	public ArrayList<Integer> getRelationOfValues() {
		return relationOfValues;
	}

	public ArrayList<String> getValuesPositive() {
		ArrayList<String> valuesPositive = new ArrayList<String>();
		for (int idx = 0; idx < relatedValues.size(); idx++) {
			if (relationOfValues.get(idx) > 0) {
				valuesPositive.add(relatedValues.get(idx));
			}
		}
		return valuesPositive;
	}

	public ArrayList<String>  getValuesNegative() {
		ArrayList<String> valuesNegative = new ArrayList<String>();
		for (int idx = 0; idx < relatedValues.size(); idx++) {
			if (relationOfValues.get(idx) < 0) {
				valuesNegative.add(relatedValues.get(idx));
			}
		}
		return valuesNegative;
	}

	public ArrayList<String> getViolationList() {
		return violationList;
	}

	public void setViolationList(ArrayList<String> violationList) {
		this.violationList = violationList;
	}

	

}
