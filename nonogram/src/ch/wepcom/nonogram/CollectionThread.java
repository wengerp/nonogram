package ch.wepcom.nonogram;


public class CollectionThread extends Thread {

	Instance iCallback;
	ConstraintCollection cc;
	int index;
	
	public CollectionThread(Instance iCallback, ConstraintCollection cc, int index) {

		this.iCallback = iCallback;
		this.cc = cc;
		this.index = index;
	}
	
	public void run() {
		
		System.out.println("Starting thread for collection:"+cc.index);
		cc.createConstraints();
		iCallback.setCollectionThreaded(index,cc);
		System.out.println("Finished thread for collection:"+cc.index+" (prio: "+cc.prio+")");
	}

}
