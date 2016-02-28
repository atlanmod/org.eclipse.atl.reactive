package org.eclipse.m2m.atl.reactive.test;

import java.io.FileNotFoundException;

import org.eclipse.m2m.atl.reactive.ReactiveTransformationLauncher;

public class relationalQuery {
	
	public void run(){
		
	}
	
	public static void main(String[] args) {

		try {
			Class2RelationalTest t = new Class2RelationalTest();

			t.class2relational = new ReactiveTransformationLauncher();

			t.class2relational.initializeGood(
					"data/ClassDiagram/ClassDiagram.ecore", "ClassDiagram",
					"data/Relational/Relational.ecore", "Relational",
					"data/ClassDiagram/Sample-ClassDiagram.xmi",
					"data/Relational/Sample-Relational.xmi", null, null,
					"data/ClassDiagram2Relational/ClassDiagram2Relational.asm");

			t.run();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
