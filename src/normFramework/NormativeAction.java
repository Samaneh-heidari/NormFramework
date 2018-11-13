package normFramework;

import java.util.ArrayList;
import java.util.HashMap;

public class NormativeAction implements Comparable<NormativeAction> {

	private String normTitle;
	private double actionGoodness;
	
	public NormativeAction(String title) {
		this.normTitle = title;
		actionGoodness = 0;
	}
	
	public NormativeAction(String title, double actionGoodness) {
		this.normTitle = title;
		this.actionGoodness = actionGoodness;
	}
	
	/*public ArrayList<String> getNormsPositive() {
		ArrayList<String> normsPositive = new ArrayList<String>();
		for (String key : evaluatedNorms.keySet()) {
			if (evaluatedNorms.get(key) == 1) {//TODO: check number assignment
				normsPositive.add(key);
			}
		}
		return normsPositive;
	}
	*/
	/*public ArrayList<String> getNormsNegative() {
		ArrayList<String> normsNegative = new ArrayList<String>();
		for (String key : evaluatedNorms.keySet()) {
			if (evaluatedNorms.get(key) == -1) {//TODO: check number assignment
				normsNegative.add(key);
			}
		}
		return normsNegative;
	}*/
	
	public double getActionGoodness() {
		return actionGoodness;
	}
	
	public String getNormTitle() {
		return normTitle;
	}
	
	public String toString() {
		String returnString = "NormAct [" + normTitle + "]:";
		/*for (String key : evaluatedNorms.keySet()) {
			if (evaluatedNorms.get(key) == 1) {
				returnString += " +" + key.charAt(0);
			}
			else if (evaluatedNorms.get(key) == -1) {
				returnString += " -" + key.charAt(0);
			}
		}*/

		returnString += ", Good: " + actionGoodness;
		return returnString;
	}

	@Override
	public int compareTo(NormativeAction other) {

		if (this.getActionGoodness() > other.getActionGoodness()) return -1;
		if (this.getActionGoodness() < other.getActionGoodness()) return 1;
		return 0;
	}
}
