package org.eclipse.m2m.atl.reactive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.engine.compiler.AtlCompiler;
import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.ASMXMLReader;
import org.eclipse.m2m.atl.engine.parser.AtlParser;
import org.eclipse.m2m.atl.engine.emfvm.lib.ExecEnv;
import org.eclipse.m2m.atl.reactive.comparison.ComparisonUtil;
import org.eclipse.m2m.atl.reactive.comparison.DiffObject;


public class AtlSourceObserver {
	
	private static final String ENCODING_PROPERTY = "file.encoding"; //$NON-NLS-1$
	
	private String sourceFolderPath;
	private EMFVMLazyTransformation transformation;
	private List<DiffObject> changes;
	
	public AtlSourceObserver(String sourceFolderPath,
			EMFVMLazyTransformation transformation) {
		super();
		this.sourceFolderPath = sourceFolderPath;
		this.transformation = transformation;
	}
	
	public List<DiffObject> getChanges() {
		return changes;
	}

	public void observeSourceDir(){
		ExecutorService es = Executors.newSingleThreadExecutor();
		es.execute(new Runnable() {
			@Override 
			public void run() {
				//define a folder root
				Path myDir = Paths.get(sourceFolderPath);       

				try {
					WatchService watcher = myDir.getFileSystem().newWatchService();
					myDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					//In theory this is not very inefficient as it relies in the underlying operating system
					//to be notified of changes instead of continuously pooling the folder
					WatchKey watchKey = null;
					while(true){

						watchKey = watcher.take();

						List<WatchEvent<?>> events = watchKey.pollEvents();
						for (WatchEvent event : events) {
							if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
								System.out.println("Created: " + event.context().toString());
							}
							if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
								System.out.println("Delete: " + event.context().toString());
							}
							if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
								recompileTransformation(event.context().toString(), "");
							}
						}
						watchKey.reset();
					}

				} catch (Exception e) {
					System.out.println("Error: " + e.toString());
				}

			}
		});		
	}
	
	public EObject getTransformationModel(String transformationPath) {
		try {
			EObject model = AtlParser.getDefault().parse(new ByteArrayInputStream(getTransformationBytes(transformationPath)));
			return model;
		} catch (ATLCoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] getTransformationBytes(String transformationPath){
		try {
			String text = new String(Files.readAllBytes(Paths.get(transformationPath)), StandardCharsets.UTF_8);
			return text.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void recompileTransformation(String transformationName, String oldTransformationName){
		try {
			//A model of the new transformation
			EObject model = getTransformationModel(sourceFolderPath + "/" + transformationName);
			
			//A model of the old transformation
			EObject oldAtlModel = getTransformationModel(sourceFolderPath + "/" + oldTransformationName);
			
			//Here we calculate the changes the modification of the transformation code produced
			List<Diff> differences = ComparisonUtil.calculateDifferences(oldAtlModel, model);
			List<Diff> filteredDifferences = ComparisonUtil.filterDiffs(differences);
			changes = ComparisonUtil.getTransformationChanges(filteredDifferences);

			//This part compiles an ATL transformation, parses the ASM and register the Operations 
			AtlCompiler.compile(new ByteArrayInputStream(getTransformationBytes(sourceFolderPath + "/" 
			+ transformationName)), sourceFolderPath + "/Compiled/fileASM.asm");
			String asmBytes = new String(Files.readAllBytes(Paths.get(sourceFolderPath +  "/Compiled/fileASM.asm")), StandardCharsets.UTF_8);
			ASM asm = new ASMXMLReader().read(new ByteArrayInputStream(asmBytes.getBytes()));
			ExecEnv ex = transformation.getExecEnv();
			ASM.registerOperations(ex, asm.getOperations());
			
			for (DiffObject diff : changes ){
				if (diff.getChangeKind() == DiffObject.BINDING_CHANGE){
					transformation.invalidateAllRuleBindings(diff.getRuleName(), diff.getBindingName());
				}
			}

			//save(model, sourceFolderPath + "/old/" + transformationName + ".xmi.old");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

}
