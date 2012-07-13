package ch.wepcom.nonogram;

import java.util.ArrayList;

public class ConstraintCollection {

	String name = "";
	String type = "";
	int index = -1;
	int length = 0;		// external length (i.e. 5)
	ArrayList<Integer> rules = null;
	ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	public ConstraintCollection(String name, String type, int index, int length, int[] rules) {
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
		return "["+this.name+";"+length+";"+rules+";"+constraints;
	}

	public void addConstraint(Constraint c) {
		int nConstr = getConstraintsSize();
		if (nConstr==0) {
			c.setSelected(true);
		} else if (nConstr==1) {
			c.setSelected(false);
			this.constraints.get(0).setSelected(false);
		} else {
			c.setSelected(false);
		}
		this.constraints.add(c);
	}
	
	private void createConstraints() {

		int sumProcessedRules = 0;
		int sumAllRules = getSumRemainingRules(-1)+1; 
		if (sumAllRules==this.length+2) {
			// only one possibility --> construct directly
			this.addConstraint(new Constraint(this.name+String.format("%02d",(this.constraints.size()+1)),this.type,this.index,this.rules,this.length));
		} else {
			ArrayList<ArrayList<Constraint>> valueCandidates = new ArrayList<ArrayList<Constraint>>();
			for (int i=0; i<rules.size(); i++) {
				int rule = rules.get(i);
				if (rule==0) break;
				int sumRemainingRules = getSumRemainingRules(i);
				ArrayList<Constraint> candidates = createCandidates(rule,sumProcessedRules, sumRemainingRules);
				valueCandidates.add(candidates);
				sumProcessedRules += rule+1;
			}
			
			ArrayList<Constraint> it = new ArrayList<Constraint>();
			generateConstraints(0, it, valueCandidates);
		}
		
		return;
	}
	
	private int getSumRemainingRules(int currentRule) {
		int result = 0;
		for (int i=currentRule+1; i<this.rules.size(); i++) {
			result += this.rules.get(i)+1;
		}
		return result;
	}

	
	private ArrayList<Constraint> createCandidates(int rule,int sumProcessedRules, int sumRemainingRules) {
		ArrayList<Constraint> result = new ArrayList<Constraint>();

		for (int p=sumProcessedRules; p<=length+2-(rule+2+sumRemainingRules); p++) {
			Constraint candidate = new Constraint(this.name+String.format("%02d",(this.constraints.size()+1)),this.type,this.index,this.rules,this.length);
			
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
			Constraint c = new Constraint(candidateKomb);
			if (!exists(c) && c.isValid()) {
				this.addConstraint(c);
			}
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
					}
				} else {
					copyCandidateKomb.add(nextCandidate);
					generateConstraints(d + 1, copyCandidateKomb, candidatesList);
				}
		  }
		  return;
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
