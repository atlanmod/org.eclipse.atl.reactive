package org.eclipse.m2m.atl.reactive.test;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.m2m.atl.engine.compiler.AtlCompiler;
import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.ASMXMLReader;
import org.eclipse.m2m.atl.engine.emfvm.lib.ExecEnv;
import org.eclipse.m2m.atl.engine.parser.AtlParser;
import org.eclipse.m2m.atl.reactive.AtlSourceObserver;
import org.eclipse.m2m.atl.reactive.EMFVMLazyTransformation;
import org.eclipse.m2m.atl.reactive.ReactiveTransformationLauncher;
import org.eclipse.m2m.atl.reactive.comparison.ComparisonUtil;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;

public class Class2RelationalHOTTest {
	
	private static final String ENCODING_PROPERTY = "file.encoding"; //$NON-NLS-1$

	ReactiveTransformationLauncher class2relational;

	@SuppressWarnings("unchecked")
	public void run() {
		
	
		// Initial elements
		EObject classRoot = class2relational.getInitialSourceElement();
		LazyModelDynamicEObjectImpl relationalRoot = class2relational
				.getInitialTargetElement();

		// We get on-demand the schemas of a system
		System.out
				.println("The lazily-computed top-level elements of the Relational model are:");
		EList<EObject> el = (EList<EObject>) relationalRoot.eContents();
		//EList<EObject> el = (EList<EObject>) relationalRoot.eGet(relationalRoot
				//.eClass().getEStructuralFeature("schemas"));
		for (EObject e : el) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}

		// Now, we add a package to the class diagram. This invalidates the
		// corresponding features of the relational model so that
		// they will be read again in the next access
		EStructuralFeature packages = classRoot.eClass().getEStructuralFeature(
				"packages");
		EFactory f = classRoot.eClass().getEPackage().getEFactoryInstance();
		EClass packageClass = (EClass) classRoot.eClass().getEPackage()
				.getEClassifier("Package");

		EObject newPackage = f.create(packageClass);
		newPackage
				.eSet(newPackage.eClass().getEStructuralFeature("name"), "p2");
		((EList<EObject>) classRoot.eGet(packages)).add(newPackage);

		// We get again on-demand the schemas of a system
		System.out
				.println("After adding another package to the Class model, the top-level elements of the Relational model are:");
		el = (EList<EObject>) relationalRoot.eContents();
		//el = (EList<EObject>) relationalRoot.eGet(relationalRoot.eClass()
		//		.getEStructuralFeature("schemas"));
		for (EObject e : el) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}
		// We get again on-demand the schemas of a system
		System.out
			.println("We get the tables contained in the contained in the first schema:");
		EObject firstSchema = relationalRoot.eContents().get(0);
		EList<EObject> cl = (EList<EObject>) firstSchema.eGet(firstSchema.eClass().getEStructuralFeature("ownedElements"));
		for (EObject e : cl) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}
		
		AtlSourceObserver observer = new AtlSourceObserver("data/ClassDiagram2Relational", (EMFVMLazyTransformation) class2relational.getTransformation());
		//observer.observeSourceDir();
		observer.recompileTransformation("ClassDiagram2Relational2.atl", "ClassDiagram2Relational.atl");
		
		// We get again the classes contained in the second schema
		System.out
					.println("We get the tables contained in the contained in the first schema after recompilation:");
		firstSchema = relationalRoot.eContents().get(0);
		cl = (EList<EObject>) firstSchema.eGet(firstSchema.eClass().getEStructuralFeature("ownedElements"));
		for (EObject e : cl) {
			System.out
					.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}
		
		System.out.println("After modifiyind the transformation:");
		el = (EList<EObject>) relationalRoot.eContents();
		//el = (EList<EObject>) relationalRoot.eGet(relationalRoot.eClass()
		//		.getEStructuralFeature("schemas"));
		for (EObject e : el) {
			System.out.println(e.eGet(e.eClass().getEStructuralFeature("name")));
		}
		
		try {
			relationalRoot.eResource().save(null);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void save(EObject root, String path) throws IOException {
		final URI modelURI = URI.createURI(path);
		ResourceSet resourceSet = root.eResource().getResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		final Resource newModelResource = resourceSet.createResource(modelURI);
		newModelResource.getContents().add(root);
		final Map<String, Object> options = new HashMap<String, Object>();
		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
		newModelResource.save(options);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Class2RelationalHOTTest t = new Class2RelationalHOTTest();

			t.class2relational = new ReactiveTransformationLauncher();

			t.class2relational.initializeGood(
					"data/ClassDiagram/ClassDiagram.ecore", "ClassDiagram",
					"data/Relational/Relational.ecore", "Relational",
					"data/ClassDiagram/Sample-ClassDiagram.xmi",
					"data/Relational/Sample-Relational.xmi", null, null,
					"data/ClassDiagram2Relational/ClassDiagram2Relational.asm");
			
			//AtlSourceObserver observer = new AtlSourceObserver("data/ClassDiagram2Relational", (EMFVMLazyTransformation) t.class2relational.getTransformation());
			//observer.observeSourceDir();
			      

//			try {
//				WatchService watcher = myDir.getFileSystem().newWatchService();
//				myDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
//				//In theory this is not very inefficient as it relies in the underlying operating system
//				//to be notified of changes instead of continuously pooling the folder
//				WatchKey watchKey = null;
//				while(true){
//
//					watchKey = watcher.take();
//
//					List<WatchEvent<?>> events = watchKey.pollEvents();
//					for (WatchEvent event : events) {
//						if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
//							System.out.println("Created: " + event.context().toString());
//						}
//						if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
//							System.out.println("Delete: " + event.context().toString());
//						}
//						if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
//							System.out.println("Modify: " + event.context().toString());
//							
//							String text = new String(Files.readAllBytes(Paths.get("data/ClassDiagram2Relational/" + event.context().toString())), StandardCharsets.UTF_8);
//							text.getBytes();
//							EObject model = AtlParser.getDefault().parse(new ByteArrayInputStream(text.getBytes()));
//							
//							URI oldAtl = URI.createURI("data/ClassDiagram2Relational/" + event.context().toString()+ ".xmi.old");
//							Resource oldAtlModel = model.eResource().getResourceSet().createResource(oldAtl);
//						
//							oldAtlModel.load(null);   
//							EObject oldATLModule = oldAtlModel.getContents().get(0);
//							
//							List<Diff> differences = ComparisonUtil.calculateDifferences(oldATLModule, model);
//							List<Diff> filteredDifferences = ComparisonUtil.filterDiffs(differences);
//							ComparisonUtil.getTransformationChanges(filteredDifferences);
//							
//							//This part compiles an ATL transformation, parses the ASM and register the Operations 
//							AtlCompiler.compile(new ByteArrayInputStream(text.getBytes()), "data/ClassDiagram2Relational/Compiled/fileASM.asm");
//							String asmBytes = new String(Files.readAllBytes(Paths.get("data/ClassDiagram2Relational/Compiled/" +  "fileASM.asm")), StandardCharsets.UTF_8);
//							ASM asm = new ASMXMLReader().read(new ByteArrayInputStream(asmBytes.getBytes()));
//		
//							ExecEnv ex = ((EMFVMLazyTransformation)t.class2relational.getTransformation()).getExecEnv();
//							
//							asm.registerOperations(ex, asm.getOperations());
//							
//							save(model, "data/ClassDiagram2Relational/" + event.context().toString()+ ".xmi.old");
//							
//						}
//					}
//					watchKey.reset();
//				}
//
//			} catch (Exception e) {
//				System.out.println("Error: " + e.toString());
//			}

			t.run();
			//es.shutdown();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
