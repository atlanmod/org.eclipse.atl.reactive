package org.eclipse.m2m.atl.reactive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreEList;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.m2m.atl.common.ATLLogger;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;
import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.ASMOperation;
import org.eclipse.m2m.atl.engine.emfvm.StackFrame;
import org.eclipse.m2m.atl.engine.emfvm.VMException;
import org.eclipse.m2m.atl.engine.emfvm.adapter.EMFModelAdapter;
import org.eclipse.m2m.atl.engine.emfvm.launch.ITool;
import org.eclipse.m2m.atl.engine.emfvm.lib.ASMModule;
import org.eclipse.m2m.atl.engine.emfvm.lib.AbstractStackFrame;
import org.eclipse.m2m.atl.engine.emfvm.lib.ExecEnv;
import org.eclipse.m2m.atl.engine.emfvm.lib.HasFields;
import org.eclipse.m2m.atl.engine.emfvm.lib.OclUndefined;
import org.eclipse.m2m.atl.engine.emfvm.lib.Operation;
import org.eclipse.m2m.atl.engine.emfvm.lib.TransientLink;
import org.eclipse.m2m.atl.engine.emfvm.lib.TransientLinkSet;
import org.eclipse.m2m.atl.reactive.model.EMFLazyModel;
import org.eclipse.m2m.atl.reactive.model.EMFLazyModelFactory;
import org.eclipse.m2m.atl.reactive.model.LazyModelDynamicEObjectImpl;

public class EMFVMLazyTransformation implements ILazyTransformation{
	
	private Map<String, IModel> models = new HashMap<String, IModel>();
	public Map<String, IModel> getModels() {
		return models;
	}

	public void setModels(Map<String, IModel> models) {
		this.models = models;
	}

	private ExecEnv execEnv;
	private ASMModule asmModule;
	private EMFLazyModelFactory lazyModelFactory = new EMFLazyModelFactory();
	private EMFModelFactory modelFactory = new EMFModelFactory();
	private EditingDomain editingDomain = null;
	public TransactionalEditingDomain domain;
	private EObject startingElement;
	private String URIFrag;
	long startTime = 0;
	long endTime = 0;
	double totalTime = 0.0;
	TransientLinkSet traceabilityLinks;
	
	public TransientLinkSet getTraceabilityLinks() {
		return traceabilityLinks;
	}

	LazyModelDynamicEObjectImpl notifyingObj;
	String notifyingFeature;
	Boolean toNotify = false;
	
	public void setToNotify(Boolean value){
		toNotify = value;
	}
	
	public double getTotalTime() {
		return totalTime;
	}


	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}


	public String getURIFrag() {
		return URIFrag;
	}


	public void setURIFrag(String uRIFrag) {
		URIFrag = uRIFrag;
	}


	public EObject getStartingElement() {
		return startingElement;
	}


	public void setStartingElement(EObject startingElement) {
		this.startingElement = startingElement;
	}

	private Boolean busy = false;
	
	public Boolean isBusy() {
		return busy;
	}


	public void setBusy(Boolean busy) {
		this.busy = busy;
	}


	public EMFVMLazyTransformation() {
	}
	 
//	   /**
//	    * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
//	    * or the first access to SingletonHolder.INSTANCE, not before.
//	    */
//	private static class EMFVMLazyTransformationHolder { 
//		public static final EMFVMLazyTransformation INSTANCE = new EMFVMLazyTransformation();
//	}
//	public static EMFVMLazyTransformation getInstance() {
//		return EMFVMLazyTransformationHolder.INSTANCE;
//	}
//  Singleton removed since with chains we can have several transformations running in the same thread


	public void addReferenceModel(String name, Resource resource) {
		EMFReferenceModel sourceReferenceModel = (EMFReferenceModel)modelFactory.newReferenceModel();
		new EMFInjector().inject(sourceReferenceModel, resource);
		models.put(name, sourceReferenceModel);
	}

	private EMFLazyModel getLazyModel(String referenceModelName, Resource resource) {
		EMFLazyModel ret = (EMFLazyModel)lazyModelFactory.newModel((EMFReferenceModel)models.get(referenceModelName));
		new EMFInjector().inject(ret, resource);
		return ret;
	}
	
	private EMFModel getModel(String referenceModelName, Resource resource) {
		EMFModel ret = (EMFModel) modelFactory.newModel((EMFReferenceModel)models.get(referenceModelName));
		new EMFInjector().inject(ret, resource);
		return ret;
	}
	
	public void addSourceModel(String name, String referenceModelName,
			Resource resource) {
		models.put(name, getModel(referenceModelName, resource));
	}

	
	public void addTargetModel(String name, String referenceModelName, Resource resource) {
		EMFLazyModel model = getLazyModel(referenceModelName, resource);
		model.setIsTarget(true);
		models.put(name, model);
	}
	
	public EMFModel getTargetModel(String name){
		return (EMFModel) models.get(name);
	}
	
	public EMFReferenceModel getReferenceModel(String name){
		return (EMFReferenceModel) models.get(name);
	}
	
	public void addLoadedReferenceModel(String name, EMFReferenceModel model){
		models.put(name, model);
	}
	
	public void addSourceLoadedModel(String name, EMFModel model) {
		models.put(name, model);
	}

	public void init(ASM asm, boolean step) {		
		execEnv = new ExecEnv(models, new ITool[0]);
		execEnv.setStep(step);
		final EMFModelAdapter modelAdapter = new EMFModelAdapter();
		// by default (if options.get("checkSameModel") == null) interModelReferences are not allowed
		modelAdapter.setAllowInterModelReferences(false);
		execEnv.init(modelAdapter);
		execEnv.registerOperation(HashMap.class, new Operation(3, "__put") {
			@SuppressWarnings("unchecked")
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				((Map<Object, Object>)localVars[0]).put(localVars[1], localVars[2]);
				return localVars[0];
			}
		});
		execEnv.registerOperation(ArrayList.class, new Operation(2, "__add") { //$NON-NLS-1$ 
			@SuppressWarnings("unchecked")
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				((Collection<Object>)localVars[0]).add(localVars[1]);
				return localVars[0];
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(3, "refUnsetValue") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				if (localVars[0] instanceof EObject) {
					modelAdapter.unSet(frame, (EObject)localVars[0], (String)localVars[1]);
				} else {
					((HasFields)localVars[0]).unset(frame, localVars[1]);
				}
				return null;
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(3, "refInvokeOperation") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				Operation op = execEnv.getOperation(modelAdapter.getType(localVars[0]), localVars[1]);
				StackFrame calleeFrame = (StackFrame)frame.newFrame(op);
				Object[] arguments = calleeFrame.getLocalVars();
				int i = 0;
				arguments[i++] = localVars[0];
				for(Object o : ((Collection<?>)localVars[2])) {
					arguments[i++] = o;
				}
				Object ret = op.exec(calleeFrame);
				if(ret == null) {
					ret = OclUndefined.SINGLETON;
				}
				return ret;
			}
		});
		
		
		/**
		 * Here we process the changing of properties. This can be by cahce invalidation
		 * or by directly forwarding the changes. Using a lib operation allows us to
		 * modify the behaviour without changing the compiler
		 * 
		 * Here we are also going to record information for the observers, that will be for the moment
		 * just this transformatiom to avoid threads
		 */
		execEnv.registerOperation(Object.class, new Operation(3, "processChanged") { //$NON-NLS-1$
			
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				LazyModelDynamicEObjectImpl target = (LazyModelDynamicEObjectImpl) localVars[0];
				target.setFeaturesFlagMapElement((String)localVars[1], false);
				//FIXME
				//This only works when the source change affects just a target feature
				//if not we will just process the last one. 
				notifyingObj = target;
				notifyingFeature = (String) localVars[1];
				return OclUndefined.SINGLETON; 
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(1, "invalidateTarget") { //$NON-NLS-1$
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				LazyModelDynamicEObjectImpl target = (LazyModelDynamicEObjectImpl) localVars[0];
				invalidateTarget(target);
				return OclUndefined.SINGLETON; 
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(2, "removeLink") { //$NON-NLS-1$
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				TransientLinkSet tsl = (TransientLinkSet) localVars[0];
				tsl.removeLink((TransientLink) localVars[1]);
				return OclUndefined.SINGLETON; 
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(2, "invalidateParentFeature") { //$NON-NLS-1$
			@Override
			public Object exec(AbstractStackFrame frame) {
				//Object[] localVars = frame.getLocalVars();
				//LazyModelDynamicEObjectImpl target = (LazyModelDynamicEObjectImpl) localVars[0];
				return OclUndefined.SINGLETON; 
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(1, "getTargetElements") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				Object ret = ((TransientLink)localVars[0]).getTargetElements().values();
				if (ret == null) {
					ret = OclUndefined.SINGLETON;
				}
				return ret;
			}
		});
		
		
		execEnv.registerOperation(Object.class, new Operation(1, "getContainmentFeature") { //$NON-NLS-1$
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				EObject obj = (EObject) localVars[0];
				return obj.eContainmentFeature().getName(); 
			}
		});
		
		//New operation to get a trace link from a target element
		execEnv.registerOperation(Object.class, new Operation(2, "getLinkByTargetElement") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				traceabilityLinks = (TransientLinkSet) localVars[0];
				TransientLink ret = ((TransientLinkSet)localVars[0])
						.getLinkByTargetElement(localVars[1]);
				if (ret == null) {
					return OclUndefined.SINGLETON;
				} else {
					return ret;
				}
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(2, "getFirstSourceElement") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				return ((TransientLink)localVars[0]).getSourceElements().values().iterator().next();
			}
		});
		
		execEnv.registerOperation(Object.class, new Operation(1, "isModelElement") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				
				if (localVars[0] instanceof EObject){
					EMFModel model = (EMFModel) execEnv.getModelOf(localVars[0]);
					if (model != null && !model.isTarget()){ //&& !model.isTarget()){
							return true;
					}
				}
				return false;
			}
		});
		
		execEnv.registerOperation(TransientLink.class, new Operation(1, "getRule") { //$NON-NLS-1$ 
			@Override
			public Object exec(AbstractStackFrame frame) {
				Object[] localVars = frame.getLocalVars();
				return ((TransientLink)localVars[0]).getRule();
			}
		});
		
//		execEnv.registerOperation(TransientLink.class, new Operation(1, "setRuleObject") { //$NON-NLS-1$ 
//			@Override
//			public Object exec(AbstractStackFrame frame) {
//				Object[] localVars = frame.getLocalVars();
//				//frame.getCaller().getLocalVariables()
//				StackSequence s = frame.getStack();
//				Map<String,Object> x = frame.getLocalVariables();
//				Object rule = frame.getLocalVariables().get("self");
//				((TransientLink)localVars[0]).setRuleObject(rule);
//				return null;
//			}
//		});
		
		


		asmModule = new ASMModule();
		ASMOperation mainOperation = asm.getMainOperation();

		// register module operations after libraries to avoid overriding
		// "main" in execEnv (avoid superimposition problems)
		List<ASMOperation> operations = new ArrayList<ASMOperation>();
		for(Iterator<ASMOperation> i = asm.getOperations() ; i .hasNext() ; ) {
			operations.add(i.next());
		}
		
		ASM.registerOperations(execEnv, operations.iterator());
		
		long startTime = System.currentTimeMillis();
		mainOperation.exec(new StackFrame(execEnv, asmModule, mainOperation));
		long endTime = System.currentTimeMillis();
			
		totalTime = totalTime + new Double((endTime - startTime) / 1000.);
	}
	
	public void call(String operationName, EObject source) {
		call(operationName, source, new Object[0]);
	}
	
	
	public void call(final String operationName, final EObject source, final Object arguments[]) {
		if(this.editingDomain == null) {
			this.actualCall(operationName, source, arguments);
		} else {
			this.editingDomain.getCommandStack().execute(new AbstractCommand() {

				public boolean prepare() {
					return true;
				}

				public void execute() {
					EMFVMLazyTransformation.this.actualCall(operationName, source, arguments);
				}

				public void redo() {
					
				}
				
			});
		}
	}
	
	public void moduleOperationCall(String operationName, Object arguments[]) {
		long startTime = System.currentTimeMillis();
		Operation operation = execEnv.getOperation(asmModule.getClass(), operationName);
		
		
		StackFrame stackFrame = new StackFrame(execEnv, asmModule, operation);
		stackFrame.getLocalVars()[0] = arguments[0];
		for(int i = 0 ; i < arguments.length ; i++) {
			stackFrame.getLocalVars()[i + 1] =  arguments[i];
		}
		try {
		operation.exec(stackFrame);
		} catch(VMException e) {
			ATLLogger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		long endTime = System.currentTimeMillis();
		totalTime = totalTime + new Double((endTime - startTime) / 1000.);
	}
	
	public void actualCall(String operationName, EObject source, Object arguments[]) {
		long startTime = System.currentTimeMillis();
		
		
		
		Operation operation = execEnv.getOperation(source.eClass(), operationName);
		StackFrame stackFrame = new StackFrame(execEnv, asmModule, operation);
		stackFrame.getLocalVars()[0] = source;
		for(int i = 0 ; i < arguments.length ; i++) {
			stackFrame.getLocalVars()[i + 1] =  arguments[i];
		}
		try {
		operation.exec(stackFrame);
		} catch(VMException e) {
			ATLLogger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		long endTime = System.currentTimeMillis();
		
		totalTime = totalTime + new Double((endTime - startTime) / 1000.);
		//System.out.println(new Double((endTime - startTime) / 1000.));
		//System.out.println("operation " + operation + " executed");
	}
	
	public ASMModule getASMModule(){
		return asmModule;
	}
	
	
	public void propertyChanged(EObject source, String propertyName){
		long startTime = System.currentTimeMillis();
		final EMFVMLazyTransformation transformation = this;
		transformation.setBusy(true);
		call("propertyChanged", source, new Object[]{propertyName});
		transformation.setBusy(false);
		//The change in the invalidation flags have to be notified
		//or directly process here
		//if (toNotify){
		//	notifyChanged(notifyingObj, notifyingFeature);
		//}
		long endTime = System.currentTimeMillis();
		totalTime = totalTime + new Double((endTime - startTime) / 1000.);
	}
	
	/*
	 * The transformation acts as observer of flag changing. The process of the notification
	 * Supposes reading the binding
	 */
	public void notifyChanged(LazyModelDynamicEObjectImpl target, String feature){
		getBinding(null, target, target.eClass().getEStructuralFeature(feature));		
	}
	
	public void elementDeleted(EObject source){
		final EMFVMLazyTransformation transformation = this;
		transformation.setBusy(true);
		call("elementDeleted", source);
		transformation.setBusy(false);	
	}
	
	/**
	 * Mark an element and all its contents as invalid
	 * @param obj
	 */
	public void invalidateTarget(LazyModelDynamicEObjectImpl obj){
		obj.setValid(false);
		TreeIterator<EObject> it = obj.eAllContents();
		while (it.hasNext()){
			LazyModelDynamicEObjectImpl o = (LazyModelDynamicEObjectImpl) it.next();
			o.setValid(false);
		}
	}
	
	public void getBinding(Object value, final LazyModelDynamicEObjectImpl lazyTargetElement, final EStructuralFeature feature){
		if ((!lazyTargetElement.getFeaturesFlagMapElement(feature.getName()) || 
				value == null || (value instanceof EcoreEList.Dynamic && ((EcoreEList.Dynamic<?>)value).isEmpty()))  
				&& !this.isBusy()){
			
			
			
			final EMFVMLazyTransformation transformation = this;
			
			transformation.setBusy(true); 
			String featureName = lazyTargetElement.eClass().getEStructuralFeature(feature.getFeatureID()).getName();
			transformation.moduleOperationCall("__initBinding__", new Object[] {lazyTargetElement, featureName});
			transformation.setBusy(false);
			
			//We dont need this if we are not using editors
			/*AbstractEMFOperation command = new
			AbstractEMFOperation(((EMFVMLazyTransformation)transformation).domain, "setFeatures") {

				@Override
				protected IStatus doExecute(IProgressMonitor arg0,
						IAdaptable arg1) throws ExecutionException {
						
						transformation.setBusy(true); 
						String featureName = lazyTargetElement.eClass().getEStructuralFeature(feature.getFeatureID()).getName();
						transformation.moduleOperationCall("__initBinding__", new Object[] {lazyTargetElement, featureName});
						transformation.setBusy(false);
					
					return Status.OK_STATUS;
				}
			}; 
			
			command.canExecute();
			
			try {
				command.execute(new NullProgressMonitor(), null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}*/
		}
	}
}
