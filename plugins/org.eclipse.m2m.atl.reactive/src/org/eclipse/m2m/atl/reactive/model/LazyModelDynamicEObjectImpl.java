package org.eclipse.m2m.atl.reactive.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;


public class LazyModelDynamicEObjectImpl extends DynamicEObjectImpl{
	
	public Map<String, Boolean> featuresFlagMap = new HashMap<String, Boolean>();
	private Boolean valid = true;
	
	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public LazyModelDynamicEObjectImpl(EClass eClass) {
	    super();
	    eSetClass(eClass);
	    Iterator<EStructuralFeature> it = eClass.getEAllStructuralFeatures().iterator();
	    
	    while (it.hasNext()){   	
	    	featuresFlagMap.put(it.next().getName(), false);
	    }
	}
	
	public boolean getFeaturesFlagMapElement(String featureName) {
		return featuresFlagMap.get(featureName);
	}

	public void setFeaturesFlagMapElement(String featureName, Boolean value) {
		LazyModelNotification notification = new LazyModelNotification(this, LazyModelNotification.FLAG_CHANGE, featureName, featuresFlagMap.get(featureName), value);
		featuresFlagMap.put(featureName, value);
		this.eNotify(notification);
	}
	
	public void setFeaturesFlagMapElementNotNotify(String featureName, Boolean value) {
		featuresFlagMap.put(featureName, value);
	}

	@Override
	public Object eGet(EStructuralFeature eFeature){
		if (!valid){
			throw new InvalidElementAccessException("Can Not perform Get operation: Element invalid");
		}
		
		Object value = super.eGet(eFeature);
		/*final LazyTransformation transformation = EMFVMLazyTransformation.getInstance();
		final EStructuralFeature feature = eFeature; 
		final LazyModelDynamicEObjectImpl lazyTargetElement = this;
		Object value = super.eGet(eFeature);
		
		if ((!featuresFlagMap.get(eFeature.getName()) ||  value == null 
				|| (value instanceof EcoreEList.Dynamic && ((EcoreEList.Dynamic)value).isEmpty()) )  
				&& !((EMFVMLazyTransformation)transformation).isBusy() ){
			featuresFlagMap.put(eFeature.getName(), true);
			((EMFVMLazyTransformation)transformation).getBinding(value, lazyTargetElement, feature);
			
			value = super.eGet(eFeature);
		}*/
		
		//We notify a get is being performed
		LazyModelNotification notification = new LazyModelNotification(this, LazyModelNotification.GET, eFeature, value);
		this.eNotify(notification);
		
		value = super.eGet(eFeature);
		
		
	    return value;
	}
	
	@Override
	public void eSet(EStructuralFeature eFeature, Object newValue){
		if (!valid){
			throw new InvalidElementAccessException("Can Not perform Set operation: Element invalid");
		}
		super.eSet(eFeature, newValue);	
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public EList<EObject> eContents(){
		EList<EObject> contentList = new BasicEList<EObject>();
		
		Iterator<EStructuralFeature> it = this.eClass.getEAllStructuralFeatures().iterator();
	   
	    while (it.hasNext()){ 
	    	EStructuralFeature est = it.next();
	    	if (est instanceof EReferenceImpl){
	    		if ( ((EReferenceImpl)est).isContainment() ){
	    			EList<EObject> partialList = (EList<EObject>) this.eGet(est);
	    			contentList.addAll(partialList);
	    		}
	    	}
	    }
	    return contentList;		
	}
}
