package ch.wepcom.nonogram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

public class OutputThread extends Thread {

	Instance iCallback;
	
	int rowIndex;
	
	public OutputThread(Instance iCallback, int rowIndex) {

		this.iCallback = iCallback;
		this.rowIndex = rowIndex;
	}
	
	public void run() {
		ConstraintCollection cc = iCallback.rows.get(rowIndex);
		ArrayList<Constraint> rowConstraints = cc.getConstraints();
		int cConstraints = 0;
		int cSupports = 0;

		System.out.println("Starting output thread for collection:"+cc.name);

		PrintWriter pwc = null;	
		PrintWriter pws = null;
		String sConstraintFile = "/Volumes/Raid0/_to_delete/nonocsp-constraint-"+cc.type +cc.index+".xml";
		String sSupportsFile = "/Volumes/Raid0/_to_delete/nonocsp-support-"+cc.type +cc.index+".xml";
		try {
			Writer fwc = new FileWriter(sConstraintFile);
			Writer bwc = new BufferedWriter(fwc);
			pwc = new PrintWriter(bwc);
			Writer fws = new FileWriter(sSupportsFile);
			Writer bws = new BufferedWriter(fws);
			pws = new PrintWriter(bws);
			
			for (Constraint cRow : rowConstraints) {
				for (ConstraintCollection col : iCallback.cols) {
					ArrayList<Constraint> colConstraints = col.getConstraints();
					for (Constraint cCol : colConstraints) {
						int conSup = cRow.checkConstraintSupportType(cCol);
						if (conSup==1 || conSup==3) {
							Constraint[] tuple = new Constraint[2];
							tuple[0] = cRow;
							tuple[1] = cCol;
							this.printConstraintsTuples(pwc, tuple, ++cConstraints);
						}
					}						
				}
			}
		for (Constraint c : iCallback.getConstraints()) {
			if (c.isSelected) {
				this.printSupportsTuples(pwc, c, ++cConstraints);		
			}
		}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pwc != null)
				pwc.close();
			if (pws != null)
				pws.close();
		}
		
		if (cSupports == 0) {
			File f = new File(sSupportsFile);
			if (f != null) {
				f.delete();
			}
		}

		this.iCallback.addToNumberOfConstraintTuples(cConstraints);
		this.iCallback.addToNumberOfSupportsTuples(cSupports);

		System.out.println("Finished output thread for collection:"+cc.name);
	}
	
	public void printConstraintsTuples(PrintWriter out, Constraint[] tuple, int index) {
	// <constraint name="C1" arity="2" scope="Z0101 S0102" reference="rel0" />	

		out.println("<constraint name=\"C" + String.format("%02d", rowIndex) + String.format("%04d", index) + "\" arity=\"2\" scope=\""+ tuple[0].name + " " + tuple[1].name + "\" reference=\"rel0\" />");			
	}
	public void printSupportsTuples(PrintWriter out, Constraint c, int index) {
	// <constraint name="C1" arity="2" scope="Z0101 S0102" reference="rel0" />	

		out.println("<constraint name=\"S" + String.format("%02d", rowIndex) + String.format("%04d", index) + "\" arity=\"1\" scope=\""+ c.name  + "\" reference=\"rel1\" />");			
	}

}
