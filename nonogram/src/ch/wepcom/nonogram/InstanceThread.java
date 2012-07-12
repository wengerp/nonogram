package ch.wepcom.nonogram;

public class InstanceThread extends Thread {

	Instance iCallback;
	int index;
	int[] rules;
	int collectionLength;
	String type;
	
	public InstanceThread(Instance iCallback, int index, int[] rules, int collectionLength, String type) {

		this.iCallback = iCallback;
		this.index = index;
		this.rules = rules;
		this.collectionLength = collectionLength;
		this.type = type;
	}
	
	public void run() {
		System.out.println("Starting thread for collection:"+type+(index+1));
		ConstraintCollection cc = new ConstraintCollection(type+String.format("%02d", index+1),type, index+1, collectionLength, rules);
		iCallback.setCollectionThreaded(cc);
		System.out.println("Finished thread for collection:"+type+(index+1));
	}

}
