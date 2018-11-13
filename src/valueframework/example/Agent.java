package valueframework.example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import normFramework.Group;
import normFramework.Norm;
import normFramework.NormativeAction;
import valueframework.Action;
import valueframework.DecisionMaker;
import valueframework.RandomTree;
import valueframework.ValuedAction;
import valueframework.common.FrameworkBuilder;
import valueframework.common.Log;

public class Agent {
	private Map<Integer, ArrayList<Norm>> norms = new HashMap<Integer, ArrayList<Norm>>();//<groupId, relatedNorms>
	private ArrayList<Group> groups = new ArrayList<Group>();
	private double normFollowerProbability; //[0,1] a percentage that shows how much this agent cares to follow social norms 
	//an agent can be a member of several groups
	//each group can have its own norms
	//TODO:an agent can have its own interpretation of a group norm. So, each agent need to have a copy of norm instances as well
	//TODO:there is a need to update personal interpretation of norms based on feedbacks from the environment.
	//for the sake of simplicity, agents don't keep a copy of norm instances for themselves. 
	
	
	private int id = -1;
	private DecisionMaker decisionMaker;
	
	public Agent(int id, double normFollowerProbability) {
		
		Log.printStars();
		this.id = id;
		this.normFollowerProbability = normFollowerProbability;
		Log.printLog("Agent " + this.id + " with DecisionMaker");
		decisionMaker = new DecisionMaker();		
	}
	
	public void becomeGroupMember(Group gr){
		if(!groups.contains(gr.getId())){
			addNormsFromGroup(gr);
			groups.add(gr);
		}else
			Log.printLog("agent " + id + " is already a member of group " + gr.getId()) ;
	}
	
	private void addNormsFromGroup(Group gr) {
		//TODO: each agent make a exact copy of norms of the group. But it needs to have it's own interpretation.
		//Therefore, later each agent should make an instance of a norm and fill the elements later based on his communication with the group members, observations and feedbacks
		//or even, add more norms later.
		ArrayList<Norm> groupNormArray = null;
		
		if(!norms.containsKey(gr.getId()))
			groupNormArray = new ArrayList<Norm>();
		else
			groupNormArray = norms.get(gr.getId());
		
		for (Norm gNorm : gr.getNorms()) {
			Norm newNrm = new Norm(gNorm);
			groupNormArray.add(newNrm);
		}
		this.norms.put(gr.getId(), groupNormArray);
		Log.printLog("Agent "+ id + " copied norms of group " + gr.getId());
		
	}
	
	

	public void stepAction() {
		
		ArrayList<String> possibleActions = getPossibleActions();
		Log.printLog("Agent " + id + " possible actions: " + possibleActions);
		ArrayList<ValuedAction> valueFilteredActions = decisionMaker.agentFilterActionsBasedOnValues(possibleActions);
		Log.printLog("Agent " + id + " valued actions: " + valueFilteredActions);
		ArrayList<NormativeAction> normativeFilteredActions = decisionMaker.agentFilterActionsBasedOnNorms(possibleActions,this.norms);
		
		ArrayList<ValuedAction> nrmValuedActs = getNormativeValuedActions(normativeFilteredActions, valueFilteredActions);
		//use the opposite of valueFilteredActions		
		ArrayList<String> nonValuedActions = complementValuedActions(possibleActions, valueFilteredActions);
		ArrayList<NormativeAction> nrmNonValuedActions =  getNormativeNonValuedActions(normativeFilteredActions, nonValuedActions); 
//		ArrayList<Action> abnormalNonValuedActions = //we don't need this as an action shouldn't be against value and norms at the same time.
		Random r = new Random(); //randomly do action that doens't support values.
		//TODO: put 0.2 in the constant class
		double performValuedActionProbability = r.nextDouble();
		int executeActionCaseNumber = -1;
		if(performValuedActionProbability < 1-this.normFollowerProbability){//Preferably take the action from valued list
			if (nrmValuedActs.size() > 0)
				executeActionCaseNumber = 0;//take action from valued list
			else if(nrmNonValuedActions.size() > 0)
				executeActionCaseNumber = 1; //take action from normative list
			
		}else //preferably take an action from normative list
			if (nrmValuedActs.size() > 0)
				executeActionCaseNumber = 2;//take action from valued list
			else if(nrmNonValuedActions.size() > 0)
				executeActionCaseNumber = 3;//take action from normative list 
		
		switch (executeActionCaseNumber) {
		case 0:
		case 2:
			ValuedAction selectedValuedAction = nrmValuedActs.get(r.nextInt(nrmValuedActs.size()));
			Log.printLog("Agent " + id + " executes action: " + selectedValuedAction.getTitle());
			decisionMaker.agentExecutesValuedAction(selectedValuedAction, 1);			
			break;
		case 1:
		case 3:
			NormativeAction selectedActionNrmNonValued = nrmNonValuedActions.get(r.nextInt(nrmNonValuedActions.size()));
			Log.printLog("Agent " + id + " executes action: " + selectedActionNrmNonValued.getNormTitle());
			Set<Norm> relatedNormsToSelectedAction = getNormByNormativeAction(selectedActionNrmNonValued);
			decisionMaker.agentExecutesNormativeAction(relatedNormsToSelectedAction, 1);
			break;
		default:
			//actions that are value related but not norm nor norm violation
			ArrayList<ValuedAction> valuedNonNormNonViolationActionsList = getValuedNonNormNonViolationActionsList(valueFilteredActions);
			if(valuedNonNormNonViolationActionsList.size() == 0){
				Log.printLog("Agent " + id + " doesn't have an action to perform; valueBasedAction  " + nrmValuedActs.size() + ", normativeActions " + nrmNonValuedActions.size());
			}
			else{
				ValuedAction selectedValuedNonNormativeAction = valuedNonNormNonViolationActionsList.get(r.nextInt(valuedNonNormNonViolationActionsList.size()));
				Log.printLog("Agent " + id + " executes action: " + selectedValuedNonNormativeAction.getTitle());
				decisionMaker.agentExecutesValuedAction(selectedValuedNonNormativeAction, 1);		
			}
			break;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<ValuedAction> getValuedNonNormNonViolationActionsList(
			ArrayList<ValuedAction> valueFilteredActions) {
		
		ArrayList<ValuedAction> valuedNonNormativeAction = new ArrayList<ValuedAction>();
		valuedNonNormativeAction = (ArrayList<ValuedAction>) valueFilteredActions.clone();
		ArrayList<String> violationActions = new ArrayList<String>();
		for (int groupID : this.norms.keySet()) {
			ArrayList<Norm> nrmList = (ArrayList<Norm>) norms.get(groupID).clone();
			for (Norm norm : nrmList) {
				violationActions.addAll((ArrayList<String>)norm.getViolationList().clone());
			}
		}
		
		for (String action : violationActions) {
			valuedNonNormativeAction.remove(action);
		}
		
		return valuedNonNormativeAction;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<String> complementValuedActions(
			ArrayList<String> possibleActions,
			ArrayList<ValuedAction> valueFilteredActions) {
		ArrayList<String> complementActions = new ArrayList<String>();
		complementActions = (ArrayList<String>) possibleActions.clone();
		for (ValuedAction action : valueFilteredActions) {
			complementActions.remove(action.getTitle());
		}
		
		return complementActions;
	}

	private Set<Norm> getNormByNormativeAction(
			NormativeAction selectedActionNrmNonValued) {
		Set<Norm> relatedNorms = new HashSet<Norm>();
		for (ArrayList<Norm> nrmList : norms.values()) {
			for (Norm nrm : nrmList) {
				if(nrm.getTitle().equals(selectedActionNrmNonValued.getNormTitle()))
					relatedNorms.add(nrm);
			}
		}
		return relatedNorms;
	}

	private ArrayList<NormativeAction> getNormativeNonValuedActions(
			ArrayList<NormativeAction> normativeFilteredActions,
			ArrayList<String> nonValuedActions) {
		//return list of actions that are normative and but not compatible with values.

				ArrayList<NormativeAction> actionNames = new ArrayList<NormativeAction>();
				for (NormativeAction nrmAct : normativeFilteredActions) {
					for (String nVlAct : nonValuedActions) {
						if(nrmAct.getNormTitle().equals(nVlAct))
							actionNames.add(nrmAct);
					}
				}
				return actionNames;
	}

	private ArrayList<ValuedAction> getNormativeValuedActions(
			ArrayList<NormativeAction> normativeFilteredActions,
			ArrayList<ValuedAction> valueFilteredActions) {		
		//return list of actions that are normative and value related.
		ArrayList<ValuedAction> actions = new ArrayList<ValuedAction>();
		for (NormativeAction nrmAct : normativeFilteredActions) {
			for (ValuedAction vlAct : valueFilteredActions) {
				if(nrmAct.getNormTitle().equals(vlAct.getTitle()))
					actions.add(vlAct);
			}
		}
//		return convertActionTitlesToActions(actionNames);
		return actions;
		
		
	}

	public void stepDrainTanks() {
		decisionMaker.drainTanks();
		Log.printLog("Agent " + id + " tanks after drain of " + decisionMaker.getTankDrainAmount() + " - " + decisionMaker.toString());
	}
	
	/**
	 * Retrieves the titles of all actions
	 * @return all action titles
	 */
	private ArrayList<String> getAllActions() {
		
		ArrayList<String> allActions = new ArrayList<String>();
		allActions.add("Job fisher");
		allActions.add("Job captain");
		allActions.add("Job teacher");
		allActions.add("Job factory worker");
		allActions.add("Job factory boss");
		allActions.add("Job elderly caretaker");
		allActions.add("Job work outside village");
		allActions.add("Job unemployed");
		allActions.add("Donate nothing");
		allActions.add("Donate to council");
		return allActions;
	}
	
	/**
	 * Retrieves a subset of all actions
	 * @return some action titles
	 */
	private ArrayList<String> getPossibleActions() {
		
		Random r = new Random();
		ArrayList<String> possibleActions = new ArrayList<String>();
		for (String actionTitle : getAllActions()) {
			if (r.nextDouble() < 0.3) {
				possibleActions.add(actionTitle);
			}
		}
		return possibleActions;
	}


	

}
