package org.eclipse.m2m.atl.reactive.comparison;

public class DiffObject implements Comparable{
	
	public static final int RULE_CHANGE = 1;
	public static final int BINDING_CHANGE = 2;
	public static final int HELPER_CHANGE = 3;
	public static final int MODULE_CHANGE = 4;
	
	private String ruleName;
	
	private String bindingName;
	
	private String helperName;
	
	private String moduleName;
	
	
	private int changeKind;
	
	public DiffObject(String elementName, int changeKind) {
		this.changeKind = changeKind;
		switch (changeKind){
			case 1 : this.ruleName = elementName;
					 this.bindingName = "";
					break;
			case 2 : this.bindingName = elementName;
					break;
			case 3 : this.helperName = elementName;
					break;
			case 4 : this.moduleName = elementName;
					break;
			default : throw new IllegalArgumentException("The change kind is not correct"); 
		}
	}
	
	public DiffObject(String ruleName, String bindingName, int changeKind) {
		super();
		this.ruleName = ruleName;
		this.bindingName = bindingName;
		this.changeKind = changeKind;
	}
	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getBindingName() {
		return bindingName;
	}

	public void setBindingName(String bindingName) {
		this.bindingName = bindingName;
	}

	public int getChangeKind() {
		return changeKind;
	}

	public void setChangeKind(int changeKind) {
		this.changeKind = changeKind;
	}
	
	public String getHelperName() {
		return helperName;
	}

	public void setHelperName(String helperName) {
		this.helperName = helperName;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	@Override
	public int compareTo(Object o) {
		DiffObject d = (DiffObject) o;
		if ((this.getBindingName().equals(d.getBindingName())) && (this.getRuleName().equals(d.getRuleName()))){
			return 0;
		}else{
			return 1;
		}
	}
}
