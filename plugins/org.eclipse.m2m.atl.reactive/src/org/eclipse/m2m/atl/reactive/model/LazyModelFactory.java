package org.eclipse.m2m.atl.reactive.model;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EFactoryImpl;

public class LazyModelFactory extends EFactoryImpl{
	
	
	
	/*public EObject create(EClass eClass){
		LazyModelDynamicEObjectImpl newObject = (LazyModelDynamicEObjectImpl) super.create(eClass);
		return newObject;
	}*/
	
	/**
	 * @generated modifiable
	 */
	  public EObject create(EClass eClass) 
	  {
	    if (getEPackage() != eClass.getEPackage() || eClass.isAbstract())
	    {
	      throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
	    }

	    for (List<EClass> eSuperTypes = eClass.getESuperTypes(); !eSuperTypes.isEmpty(); )
	    {
	      EClass eSuperType = eSuperTypes.get(0);
	      if (eSuperType.getInstanceClass() != null)
	      {
	        EObject result = eSuperType.getEPackage().getEFactoryInstance().create(eSuperType);
	        ((InternalEObject)result).eSetClass(eClass);
	        return result;
	      }
	      eSuperTypes = eSuperType.getESuperTypes();
	    }

	    return basicCreate(eClass);
	  }

	  protected EObject basicCreate(EClass eClass) 
	  {
	    return
	       eClass.getInstanceClassName() == "java.util.Map$Entry" ?
	         new LazyModelDynamicEObjectImpl.BasicEMapEntry<String, String>(eClass) :
	         new LazyModelDynamicEObjectImpl(eClass);
	  }

}
