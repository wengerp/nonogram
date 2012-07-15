package ch.wepcom.nonogram;

import java.util.ArrayList;

public class ConstraintCollection {

	String name = "";
	String type = "";
	int index = -1;
	int length = 0;		// external length (i.e. 5)
	ArrayList<Integer> rules = null;
	ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	ArrayList<Constraint> candidates = new ArrayList<Constraint>();
	boolean busy = false;
	Instance instance;
	
	public ConstraintCollection(Instance instance, String name, String type, int index, int length, int[] rules) {
		this.instance = instance;
		this.name = name;
		this.type = type;
		this.index = index;
		this.length = length;
		this.rules = new ArrayList<Integer>();
		for (int i : rules) {
			this.rules.add(new Integer(i));
		}
		createConstraints();
	}
	
	public String toString() {
		return "["+this.name+";"+length+";"+rules+";"+getConstraintsSize()+";"+constraints;
	}

	public void addConstraint(Constraint c) {
//		int nConstr = getConstraintsSize();
//		if (nConstr==0) {
//			c.setSelected(true);
//		} else if (nConstr==1) {
//			c.setSelected(false);
//			this.constraints.get(0).setSelected(false);
//		} else {
//			c.setSelected(false);
//		}
		this.constraints.add(c);
		
		// @@@
//		this.instance.updateConstraintsState(c);
	}
	
	private void createConstraints() {

		int sumProcessedRules = 0;
		int sumAllRules = getSumRemainingRules(-1)+1; 
		if (sumAllRules==this.length+2 || sumAllRules == 2) {
			// only one possibility --> construct directly
			Constraint selectedConstraint = new Constraint(this, this.name+String.format("%02d",(this.constraints.size()+1)),this.type,this.index,this.rules,this.length);
			selectedConstraint.setSelected(true);
			System.out.println("--- Created Selected Constraint:"+selectedConstraint);
			this.addConstraint(selectedConstraint);
//			this.instance.updateConstraintsState(selectedConstraint);
		} else {
			ArrayList<ArrayList<Constraint>> valueCandidates = new ArrayList<ArrayList<Constraint>>();
			for (int i=0; i<rules.size(); i++) {
				int rule = rules.get(i);
				if (rule==0) break;
				int sumRemainingRules = getSumRemainingRules(i);
				ArrayList<Constraint> candidates = createCandidates(rule,sumProcessedRules, sumRemainingRules);
				// @
				this.candidates.addAll(candidates);
				valueCandidates.add(candidates);
				sumProcessedRules += rule+1;
			}
			
			ArrayList<Constraint> it = new ArrayList<Constraint>();
			generateConstraints(0, it, valueCandidates);
		}
		
		return;
	}

//	private void createCandidates(int rule,int sumProcessedRules, int sumRemainingRules) {
//	
//		for (int p=sumProcessedRules; p<=length+2-(rule+2+sumRemainingRules); p++) {
//			Constraint candidate = new Constraint(this, this.name+String.format("%02d",(this.constraints.size()+1)),this.type,this.index,this.rules,this.length);
//			
//			// first 
//			candidate.setBoolVal(p,new Boolean(false));
//			
//			// middle
//			for (int j=0; j<rule; j++) {
//				candidate.setBoolVal(p+j+1,new Boolean(true));				}
//	
//			// last
//			candidate.setBoolVal(p+rule+1,new Boolean(false));
//			this.constraints.add(candidate);
//		}
//		
//	}

	private ArrayList<Constraint> createCandidates(int rule,int sumProcessedRules, int sumRemainingRules) {
		ArrayList<Constraint> result = new ArrayList<Constraint>();
	
		for (int p=sumProcessedRules; p<=length+2-(rule+2+sumRemainingRules); p++) {
			Constraint candidate = new Constraint(this, this.name+String.format("%02d",(this.constraints.size()+1)),this.type,this.index,this.rules,this.length);
			
			// first 
			candidate.setBoolVal(p,new Boolean(false));
			
			// middle
			for (int j=0; j<rule; j++) {
				candidate.setBoolVal(p+j+1,new Boolean(true));				}
	
			// last
			candidate.setBoolVal(p+rule+1,new Boolean(false));
			
			result.add(candidate);
		}
		
		return result;
	}

	public void generateConstraints(int d, ArrayList<Constraint> candidateKomb, ArrayList<ArrayList<Constraint>> candidatesList) {
	if (d == candidatesList.size()) {
		Constraint c = new Constraint(this,candidateKomb);
//	@@@
//		if (this.instance.checkConstraintsState(c) < 3) {
			if (!exists(c) && c.isValid()) {
				this.addConstraint(c);
//				this.instance.updateConstraintsState(c);
			} 
//		} else {
//			System.out.println("generateConstraints(I):HEURISTIC RULE:FORBIDDEN CONSTRAINT:"+c);
//		}
		return;
	  }
		for (int k = 0; k < candidatesList.get(d).size(); k++) {
			ArrayList<Constraint> copyCandidateKomb = new ArrayList<Constraint>(candidateKomb);
			Constraint nextCandidate = candidatesList.get(d).get(k);
			if (d>0) {
				Constraint previousCandidate = candidateKomb.get(d-1);
				int firstfree =  previousCandidate.getFirstFreePosition();
				int lastfree = nextCandidate.getLastFreePosition();
				if (lastfree-firstfree>=0) {
					copyCandidateKomb.add(nextCandidate);
					generateConstraints(d + 1, copyCandidateKomb, candidatesList);
				} else {
//						this.candidates.remove(nextCandidate);
				}
			} else {
				copyCandidateKomb.add(nextCandidate);
				generateConstraints(d + 1, copyCandidateKomb, candidatesList);
			}
	  }
	  return;
	}

	public synchronized void updateConstraintsState(Constraint c) {
		for (Constraint constraint: this.constraints){
			if(constraint.isSelected()){
				for (Constraint c2 : this.constraints) {
					if(c2!=constraint) {
						System.out.println("----- updating cc for CONSTRAINT:"+c2);
						while (busy)	{
							System.out.println("---cc:updateConstraintsState---");
						}
						busy = true;
						c2.setForbidden(true);
						busy = false;
					}
				}
				break;
			}
		}
		for (Constraint candidate : this.candidates) {
			int conSup = candidate.checkConstraintSupportType(c);
			if (conSup == 3) {
//				System.out.println("----- updating cc for candidate:"+candidate);
				while (busy)	{
					System.out.println("---cc:updateConstraintsState---");
				}
				busy = true;
				candidate.setForbidden(true);
				busy = false;
			}
		}
	}

	public int checkConstraintsState(Constraint c) {
		int result = 0;
		for (Constraint constraint: this.constraints) {
			int conSup = constraint.checkConstraintSupportType(c);
			if (conSup > result) {
				result = conSup;
			}
		}
		return result;
	}

	private int getSumRemainingRules(int currentRule) {
		int result = 0;
		for (int i=currentRule+1; i<this.rules.size(); i++) {
			result += this.rules.get(i)+1;
		}
		return result;
	}

	
	public boolean exists(Constraint c)	{
		for (Constraint c2 : constraints) {
			if (c.isEqual(c2)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Constraint> getConstraints() {
		return this.constraints;
	}
	
	public int getConstraintsSize() {
		return this.constraints.size();
	}
}
