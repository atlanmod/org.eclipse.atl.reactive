package org.eclipse.m2m.atl.reactive.model;

import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

/**
 * Extension of the NotificationImpl class to make it able to notify changes of the
 * extended features of the LazyModelDynamicEObjects
 * @author salvador
 *
 */
public class LazyModelNotification extends NotificationImpl{
	protected InternalEObject notifier;
	protected int featureID = NO_FEATURE_ID;
	protected EStructuralFeature feature = null;
	protected Object value = null;
	
	public static final int FLAG_CHANGE = 11;
	 
	public static final int GET = 12;
	
	protected String featureName;

	public LazyModelNotification(InternalEObject notifier, int eventType, String featureName, boolean oldBooleanValue,
			boolean newBooleanValue) {
		super(eventType, oldBooleanValue, newBooleanValue);
		this.notifier = notifier;
		this.featureName = featureName;
	}
	
	
	public LazyModelNotification(InternalEObject notifier, int eventType, EStructuralFeature feature, Object value)
	  {
	    super(eventType, null, null, 0);
	    this.notifier = notifier;
	    this.feature = feature;
	    this.value = value;
	  }
	
	@Override
	public Object getFeature()
	  {
	    return feature;
	  }
	
	public EStructuralFeature getEstructuralFeature(){
		return feature;
	}
	
	public Object getValue(){
		return value;
	}
	
	public Object getNotifier(){
		return notifier;
	}
	
	public String getFeatureName(){
		return featureName;
	}
	
	

}
