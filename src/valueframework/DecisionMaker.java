package valueframework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import normFramework.Group;
import normFramework.Norm;
import normFramework.NormativeAction;
import valueframework.common.FrameworkBuilder;
import valueframework.common.Log;

public class DecisionMaker {

	//there is a need to consider group presence to follow/not follow a norm.
	//for simplicity, we don't consider this.
	
	private ArrayList<Action> allActions;
	private Map<String, RandomTree> valueTrees;

	public DecisionMaker() {

		valueTrees = new HashMap<String, RandomTree>();
		allActions = new ArrayList<Action>();

		assignAllActions(FrameworkBuilder.getAllPossibleActions());
		copyNewValueTrees();
		ValueAssignment.checkInitialConditions(valueTrees);
		updateValueTrees(ValueAssignment.getImportanceRange());
		Log.printLog("Decision maker " + toString());
	}

	private void updateValueTrees(ArrayList<String> importanceRange) {
		
		for (String valInfo : importanceRange) {
			
			//Log.printDebug(valInfo);
			// Copy WaterTank
			WaterTank wt = waterCopyWaterTank(valueTrees.get(ValueAssignment.getValueName(valInfo)).getWaterTank());

			// Update WaterTank
			wt.setThreshold(ValueAssignment.getThreshold(valInfo));
			wt.setFilledLevel(wt.getThreshold()); // Set the filled level to the threshold
			
			// Set WaterTank
			RandomTree rt = valueTrees.get(wt.getRelatedAbstractValue());
			rt.setWaterTank(wt);
			valueTrees.put(wt.getRelatedAbstractValue(), rt);
		}
	}

	public double getUniversalismImportanceDistribution() {
		return getWaterTankFromTree(AbstractValue.UNIVERSALISM.name()).getThreshold()
				/ (getWaterTankFromTree(AbstractValue.POWER.name()).getThreshold() + getWaterTankFromTree(AbstractValue.UNIVERSALISM.name()).getThreshold());
	}

	public double getWaterTankLevel(String abstractValue) {
		if (valueTrees.containsKey(abstractValue)) {
			return getWaterTankFromTree(abstractValue).getFilledLevel();
		}
		return -1;
	}
	
	public double getWaterTankThreshold(AbstractValue abstractValue) {
		if (valueTrees.containsKey(abstractValue.name())) {
			return getWaterTankFromTree(abstractValue.name()).getThreshold();
		}
		return -1;
	}

	public void setWaterTankThreshold(String valueName, double threshold) {
		getWaterTankFromTree(valueName).setThreshold(threshold);
	}

	public void adjustWaterTankThreshold(String valueName, double change, double min, double max) {
		getWaterTankFromTree(valueName).adjustThreshold(change, min, max);
	}

	public void drainTanks() {
		for (String key : valueTrees.keySet()) {
			getWaterTankFromTree(key).draining();
		}
	}

	/**
	 * Here we copy each value tree (make a new reference)
	 */
	private void copyNewValueTrees() {
		for (String rootName : FrameworkBuilder.getGlobalValueTrees().keySet()) {
			RandomTree rt = copyRandomTree(FrameworkBuilder.getGlobalValueTrees().get(rootName));
			valueTrees.put(rootName, rt);
		}
	}

	private RandomTree copyRandomTree(RandomTree rt) {
		
		Node newRoot = new Node(rt.getRoot().getValueName(), null);
		Node newTreeCrrNode, oldTreeCrrNode;
		newTreeCrrNode = newRoot;
		oldTreeCrrNode = rt.getRoot();

		copyChildren(newTreeCrrNode, oldTreeCrrNode);

		RandomTree randomTree = new RandomTree(newTreeCrrNode, waterCopyWaterTank(rt.getWaterTank()));
		return randomTree;
	}

	private void copyChildren(Node newTreeCrrNode, Node oldTreeCrrNode) {

		for (Node ch : oldTreeCrrNode.getChildren()) {
			Node newChild = new Node(ch.getValueName(), newTreeCrrNode);
			newTreeCrrNode.addChild(ch);
			copyChildren(newChild, ch);
		}
		if (oldTreeCrrNode.getChildren().size() == 0)
			return;
	}

	private WaterTank waterCopyWaterTank(WaterTank wt) {

		Random r = new Random();
		double filledLevel = Math.round(wt.getCapacity() * r.nextDouble());
		WaterTank newWaterTank = new WaterTank(wt.getCapacity(), filledLevel,
				wt.getThreshold(), wt.getDrainingAmount(),
				wt.getRelatedAbstractValue());
		return newWaterTank;
	}

	/**
	 * Filters the given actions based on the watertanks
	 * 
	 * @param possibleActionsIn
	 * @return
	 */
	public ArrayList<ValuedAction> agentFilterActionsBasedOnValues(
			ArrayList<String> possibleActionTitlesIn) {

		// Return if input is an array list of zero
		if (possibleActionTitlesIn.size() == 0) {
			return new ArrayList<ValuedAction>();
		}

		// Convert action titles to actions and get the trees that belong to the
		// trees
		ArrayList<Action> possibleActions = convertActionTitlesToActions(possibleActionTitlesIn);
		Map<String, Double> weightedValues = getWeightedAbstractValueList();
		if (weightedValues.size() == 0) {
			Log.printLog("No value is important, returning all possible actions");
			return convertActionsToValuedActions(possibleActions);
		}
		Log.printLog("Print weighted values" + weightedValues.toString());
		ArrayList<ValuedAction> possibleValuedActions = evaluateActionsAccordingToValues(possibleActions, weightedValues);
		Log.printValuedActions("Evaluated VA: ", possibleValuedActions);

		// Select actions
		ArrayList<ValuedAction> selectedValuedActions = filterValuedActions(possibleValuedActions);

		return selectedValuedActions;
	}

	/**
	 * First sorts the possibleValuedActions, then adds all of them that have a
	 * positive action goodness If there are none which have a goodness above
	 * zero then the ones with the highest goodness are returned e.g. a set of
	 * valuedActions with goodness 0 or maybe -0.1
	 * 
	 * @param possibleValuedActions
	 * @return
	 */
	private ArrayList<ValuedAction> filterValuedActions(
			ArrayList<ValuedAction> possibleValuedActions) {

		Collections.sort(possibleValuedActions);
		ArrayList<ValuedAction> choosenValuedActions = new ArrayList<ValuedAction>();
		double highestValue = -1000; // Double.MIN_NORMAL; doesn't work the
										// value is to low to compare
		for (ValuedAction valuedAction : possibleValuedActions) {

			if (valuedAction.getActionGoodness() > 0) {
				choosenValuedActions.add(valuedAction);
				highestValue = 1;
			} else {
				if (highestValue <= valuedAction.getActionGoodness()) {
					choosenValuedActions.add(valuedAction);
					highestValue = valuedAction.getActionGoodness();
				}
			}
		}
		return choosenValuedActions;
	}

	private ArrayList<ValuedAction> evaluateActionsAccordingToValues(
			ArrayList<Action> possibleActions,
			Map<String, Double> weightedValues) {

		ArrayList<ValuedAction> valuedActions = new ArrayList<ValuedAction>();
		for (Action action : possibleActions) {

			double actionGoodness = 0;
			HashMap<String, Integer> evaluatedValues = new HashMap<String, Integer>();
			ArrayList<String> positiveAbstractValues = findPositiveAbstractValues(action);
			ArrayList<String> negativeAbstractValues = findNegativeAbstractValues(action);
			Log.printDebug("Evaluate action -> " + action.getTitle() + ": + " + positiveAbstractValues.toString() + ", - " + negativeAbstractValues);
			for (String key : weightedValues.keySet()) {
				if (positiveAbstractValues.contains(key)) {
					evaluatedValues.put(key, 1);
					actionGoodness += weightedValues.get(key);
				} else if (negativeAbstractValues.contains(key)) {
					evaluatedValues.put(key, -1);
					actionGoodness -= weightedValues.get(key);
				}
			}
			valuedActions.add(new ValuedAction(action.getTitle(),
					evaluatedValues, actionGoodness));
		}
		return valuedActions;
	}

	private Map<String, Double> getWeightedAbstractValueList() {

		Map<String, Double> priorityOfValues = new HashMap<String, Double>();
		double sigma = 0;
		for (String wtKey : valueTrees.keySet()) {
			double priority = getWaterTankFromTree(wtKey).getPriorityPercentage();
			if (priority > 0) {
				priorityOfValues.put(wtKey, priority);
				sigma += priority;
			}
		}
		Map<String, Double> weightedValues = new HashMap<String, Double>();
		for (String prioKey : priorityOfValues.keySet()) {
			double weight = priorityOfValues.get(prioKey) / sigma;
			weightedValues.put(prioKey, weight);
		}
		return weightedValues;
	}

	/**
	 * Updates the watertanks of the corresponding values
	 * ....
	 * @param selectedActionTitle
	 */
	public void agentExecutesValuedAction(ValuedAction selectedActionTitle, double multiplier) {

		for (String positiveValue : selectedActionTitle.getValuesPositive()) {
			getWaterTankFromTree(positiveValue).increaseLevel(multiplier);
		}

		for (String negativeValue : selectedActionTitle.getValuesNegative()) {
			getWaterTankFromTree(negativeValue).decreaseLevel(multiplier);
		}
	}

	private ArrayList<Action> convertActionTitlesToActions(
			ArrayList<String> possibleActionTitlesIn) {

		ArrayList<Action> possibleActions = new ArrayList<Action>();
		for (Action action : allActions) {
			if (possibleActionTitlesIn.contains(action.getTitle())) {
				possibleActions.add(action);
			}
		}
		if (possibleActionTitlesIn.size() != possibleActions.size()) {
			Log.printError("ArrayLists do not match in size actionTitles.size(): "
					+ possibleActionTitlesIn.size()
					+ ", actions.size():"
					+ possibleActions.size());
		}
		return possibleActions;
	}

	private ArrayList<ValuedAction> convertActionsToValuedActions(ArrayList<Action> actions) {
		
		ArrayList<ValuedAction> valuedActions = new ArrayList<ValuedAction>();
		for (Action action : actions) {
			valuedActions.add(new ValuedAction(action.getTitle()));
		}
		return valuedActions;
	}

	private ArrayList<String> findPositiveAbstractValues(Action action) {
		ArrayList<String> absValues = new ArrayList<String>(); //TODO: I (Maarten) changed this to String instead of Node. To make sure they are recognized in line absValues.contains(
		ArrayList<String> rndTrees = new ArrayList<String>();
		// add all the related values including negative and positive related
		// ones.
		ArrayList<Node> concreteValues = action.getPositiveRelatedConcreteValues();
		for (int i = 0; i < concreteValues.size(); i++) {
			Node crrPrnt = concreteValues.get(i);
			Node prvPrnt = crrPrnt;
			while (crrPrnt != null) {
				prvPrnt = crrPrnt;
				crrPrnt = crrPrnt.getParent();
			}
			if (!absValues.contains(prvPrnt.getValueName())) {
				absValues.add(prvPrnt.getValueName());
			}
		}

		for (RandomTree value : valueTrees.values()) {
			if (absValues.contains(value.getRoot().getValueName())) {
				rndTrees.add(value.getRoot().getValueName());
			}
		}

		return rndTrees;
	}

	private ArrayList<String> findNegativeAbstractValues(Action action) {
		ArrayList<String> absValues = new ArrayList<String>(); //TODO: Same as in positive
		ArrayList<String> rndTrees = new ArrayList<String>();
		// add all the related values including negative and positive related
		// ones.
		ArrayList<Node> concreteValues = action.getNegativeRelatedConcreteValues();
		for (int i = 0; i < concreteValues.size(); i++) {
			Node crrPrnt = concreteValues.get(i);
			Node prvPrnt = crrPrnt;
			while (crrPrnt != null) {
				prvPrnt = crrPrnt;
				crrPrnt = crrPrnt.getParent();
			}
			if (!absValues.contains(prvPrnt.getValueName()))
				absValues.add(prvPrnt.getValueName());
		}

		for (RandomTree value : valueTrees.values()) {
			if (absValues.contains(value.getRoot().getValueName()))
				rndTrees.add(value.getRoot().getValueName());
		}

		return rndTrees;
	}

	public void assignAllActions(ArrayList<Action> alist) {
		allActions = alist;
	}

	public Map<String, RandomTree> getValueTrees() {
		return valueTrees;
	}

	public void setValueTrees(Map<String, RandomTree> valueTrees) {
		this.valueTrees = valueTrees;
	}

	public double getAbstractValueThreshold(AbstractValue abstractValue) {
		return getWaterTankFromTree(abstractValue.name()).getThreshold();
	}

	public int getSatisfiedValuesCount() {
		int satisfiedValues = 0;
		for (String key : valueTrees.keySet()) {
			WaterTank wt = getWaterTankFromTree(key);
			if (wt.getFilledLevel() >= wt.getThreshold()) {
				satisfiedValues++;
			}
		}
		return satisfiedValues;
	}

	public WaterTank getWaterTankFromTree(String key) {
		return valueTrees.get(key).getWaterTank();
	}
	
	public boolean getIsSatisfied() {

		if (getSatisfiedValuesCount() > valueTrees.size() / 2) {
			return true;
		}
		return false;
	}

	public boolean isSelfDirectionSatisfied() {

		if (getWaterTankFromTree(AbstractValue.SELFDIRECTION.name()).getFilledLevel() >= getWaterTankFromTree(AbstractValue.SELFDIRECTION.name()).getThreshold()) {
			return true;
		}
		return false;
	}

	public double getTankDrainAmount() {

		for (String key : valueTrees.keySet()) {
			return getWaterTankFromTree(key).getDrainingAmount();
		}
		return 0;
	}

	/**
	 * Set important data of a water tank.
	 * @param data, this consists of 0: abstract value name, 1: level, 2: threshold
	 */
	public void setImportantWaterTankFromData(List<String> data) {
		for (int i = 1; i < data.size(); i += 3) {
			getWaterTankFromTree(data.get(i)).setLevelAndThreshold(Double.parseDouble(data.get(i + 1)), Double.parseDouble(data.get(i + 2)));
		}
	}
	
	public String importantData() {
		String string = "";
		boolean first = true;
		for (String key : valueTrees.keySet()) {
			if (!first) {
				string += ",";
			}
			string += getWaterTankFromTree(key).getRelatedAbstractValue() + ","
					+ getWaterTankFromTree(key).getFilledLevel() + ","
					+ getWaterTankFromTree(key).getThreshold();
			first = false;
		}
		return string;
	}

	@Override
	public String toString() {
		String string = "";
		for (String key : valueTrees.keySet()) {
			WaterTank wt = getWaterTankFromTree(key);
			string += wt.getRelatedAbstractValue().charAt(0) + ": ";
			if (wt.getFilledLevel() < wt.getThreshold()) {
				string += wt.getFilledLevel() + " < ["
						+ wt.getThreshold() + "], ";
			}
			else {
				string += wt.getFilledLevel() + " >= ["
						+ wt.getThreshold() + "], ";
			}		
		}
		return string;
	}

	//TODO: increase socialStatus amount as well
	//TODO: increase Tradition and Conformity as well
	//all the norms in the input list will be satisfied by performing one action (titles of the norms should be the same)
	//One action might be related to several norms.
	public void agentExecutesNormativeAction(			
			Set<Norm> selectedNormsToFollow, int multiplier) {
//		ArrayList<Norm> uniquesNormsToFollow = removeRepeatedNorms(selectedNormsToFollow);
		for (Norm selectedNorm : selectedNormsToFollow) {
			
			for (String positiveValue : selectedNorm.getValuesPositive()) {
				getWaterTankFromTree(positiveValue).increaseLevel(multiplier);
			}
	
			for (String negativeValue : selectedNorm.getValuesNegative()) {
				getWaterTankFromTree(negativeValue).decreaseLevel(multiplier);
			}
		}
	}

	

	public ArrayList<NormativeAction> agentFilterActionsBasedOnNorms(
			ArrayList<String> possibleActionsTitlesIn, Map<Integer, ArrayList<Norm>> norms) {
		// Return if input is an array list of zero
				if (possibleActionsTitlesIn.size() == 0) {
					return new ArrayList<NormativeAction>();
				}

				// Convert action titles to actions and get the trees 
				ArrayList<Action> possibleActions = convertActionTitlesToActions(possibleActionsTitlesIn);
				ArrayList<NormativeAction> weightedNorms = getWeightedNormList(norms);
				if (weightedNorms.size() == 0) {
					Log.printLog("No Norm is important, returning all possible actions");
					return convertActionsToNormativeActions(possibleActions);
				}
				Log.printLog("Print weighted values" + weightedNorms.toString());
				ArrayList<NormativeAction> possibleNormativeActions = evaluateActionsAccordingToNorms(possibleActions, weightedNorms);
				Log.printNormativeActions("Evaluated Norm: ", possibleNormativeActions);

				// Select actions
//				ArrayList<NormativeAction> selectedNormativeActions = filterNormativeActions(possibleNormativeActions);

				return possibleNormativeActions;
	}

	/*private ArrayList<NormativeAction> filterNormativeActions(
			ArrayList<NormativeAction> possibleNormativeActions) {
		
		Collections.sort(possibleNormativeActions);
		ArrayList<NormativeAction> choosenValuedActions = new ArrayList<NormativeAction>();
		double highestPriority = -1000; // Double.MIN_NORMAL; doesn't work the
										// value is to low to compare
		for (NormativeAction nrmAction : possibleNormativeActions) {

			if (nrmAction.getActionGoodness() > 0) {
				choosenValuedActions.add(valuedAction);
				highestValue = 1;
			} else {
				if (highestValue <= valuedAction.getActionGoodness()) {
					choosenValuedActions.add(valuedAction);
					highestValue = valuedAction.getActionGoodness();
				}
			}
		}
		return choosenValuedActions;
	}*/

	private ArrayList<NormativeAction> evaluateActionsAccordingToNorms(
			ArrayList<Action> possibleActions,
			ArrayList<NormativeAction> weightedNorms) {		
 
		ArrayList<NormativeAction> normedActions = new ArrayList<NormativeAction>();
		double actionGoodness = 0;
		Log.printLog("Possible actions which are normative as well are");
		for (Action action : possibleActions) {
	
			//As we considered not doing an action in the action list, there is only positive effect on values.
			//i.e. action "not donating" as a norm have positive effect on power and action "donating" has positive effect on universalim.
	
			for (NormativeAction nrmAction : weightedNorms) {
				if(action.getTitle().equals(nrmAction.getNormTitle())){
					normedActions.add(nrmAction);
					Log.printLog("\t" + nrmAction.toString());
					
				}
			}
			
		}
		return normedActions;
	}

	private ArrayList<NormativeAction> convertActionsToNormativeActions(
			ArrayList<Action> possibleActions) {
		ArrayList<NormativeAction> normativeActions = new ArrayList<NormativeAction>();
		for (Action action : possibleActions) {
			normativeActions.add(new NormativeAction(action.getTitle()));
		}
		return normativeActions;
	}

	private ArrayList<NormativeAction> getWeightedNormList(Map<Integer, ArrayList<Norm>> norms) {
		//TODO: now all the norms has the same weight
		//TODO: check if these norms are already checked with the groups that the agent is a member.
		//TODO: priority of internalized = 3, have to = 2, should = 1;
		ArrayList<NormativeAction> weightedNormativeActions = new ArrayList<NormativeAction>();
		double sigma = 0;
		double priority = -1;
		for (int grID : norms.keySet()) {
			for (Norm nrm : norms.get(grID)) {
				switch (nrm.getNormType()) {
				case "internalized":
					priority = 3.0;
					break;
				case "have to":
					priority = 2.0;
					break;
				case "should":
					priority = 1.0;
					break;				
				default:
					Log.printLog("norm type is not defined!");
					priority = -1.0;
					break;
				}
				weightedNormativeActions.add(new NormativeAction(nrm.getTitle(), priority));
				sigma  += priority;				
			}
		}
		
		/*Map<Norm, Double> weightedNorms = new HashMap<Norm, Double>();
		for (Norm nrm : priorityOfNorms.keySet()) {
			double weight = priorityOfNorms.get(nrm) / sigma;
			weightedNorms.put(prioKey, weight);
		}*/
//		return weightedValues;
		return weightedNormativeActions;
	}

	public void agentExecutesValuedAction(
			ValuedAction valuedActs, int multiplier) {
		for (String positiveValue : valuedActs.getValuesPositive()) {
			getWaterTankFromTree(positiveValue).increaseLevel(multiplier);
		}

		for (String negativeValue : valuedActs.getValuesNegative()) {
			getWaterTankFromTree(negativeValue).decreaseLevel(multiplier);
		}
		
	}

	
}