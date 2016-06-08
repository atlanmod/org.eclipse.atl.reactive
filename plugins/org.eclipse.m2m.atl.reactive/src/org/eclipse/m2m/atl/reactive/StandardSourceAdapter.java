package org.eclipse.m2m.atl.reactive;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.m2m.atl.reactive.model.LazyModelNotification;

/**
 * Source Reactive Adapter handles notification events produced by an EMF model
 * element such as ADD, SET or REMOVE. Then, a given running reactive
 * transformation is used to react to such events, namely, invalidating the
 * features which depend of that source change
 * 
 * @author Salvador Martínez
 * 
 */
public class StandardSourceAdapter extends EContentAdapter {

	public StandardSourceAdapter(EMFVMLazyTransformation transformation) {
		super();
		this.transformation = transformation;
	}

	EMFVMLazyTransformation transformation;

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);
		int eventType = notification.getEventType();
		EStructuralFeature f = null;
		EObject notifier = null;
		switch (eventType) {
		case Notification.ADD:
			f = (EStructuralFeature) ((ENotificationImpl) notification)
					.getFeature();
			notifier = (EObject) ((ENotificationImpl) notification)
					.getNotifier();
			if (!transformation.isBusy())
				transformation.propertyChanged(notifier, f.getName());
			break;
		case Notification.SET:
			f = (EStructuralFeature) ((ENotificationImpl) notification)
					.getFeature();
			notifier = (EObject) ((ENotificationImpl) notification)
					.getNotifier();
			if (!transformation.isBusy())
				transformation.propertyChanged(notifier, f.getName());
			break;
		case Notification.REMOVE:
			f = (EStructuralFeature) ((ENotificationImpl) notification)
					.getFeature();
			notifier = (EObject) ((ENotificationImpl) notification)
					.getNotifier();
			Object newv = ((ENotificationImpl) notification).getNewValue();
			Object old = ((ENotificationImpl) notification).getOldValue();
			if (!transformation.isBusy())
				transformation.elementDeleted((EObject) old);
			if (!transformation.isBusy())
				transformation.propertyChanged(notifier, f.getName());
			break;
		default:
			break;
		}
	}

}
