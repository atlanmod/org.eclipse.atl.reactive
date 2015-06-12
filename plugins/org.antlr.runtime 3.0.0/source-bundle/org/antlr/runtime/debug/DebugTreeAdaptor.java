package org.antlr.runtime.debug;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.TreeAdaptor;

/** A TreeAdaptor proxy that fires debugging events to a DebugEventListener
 *  delegate and uses the TreeAdaptor delegate to do the actual work.  All
 *  AST events are triggered by this adaptor; no code gen changes are needed
 *  in generated rules.  Debugging events are triggered *after* invoking
 *  tree adaptor routines.
 *
 *  Trees created with actions in rewrite actions like "-> ^(ADD {foo} {bar})"
 *  cannot be tracked as they might not use the adaptor to create foo, bar.
 *  The debug listener has to deal with tree node IDs for which it did
 *  not see a createNode event.  A single <unknown> node is sufficient even
 *  if it represents a whole tree.
 */
public class DebugTreeAdaptor implements TreeAdaptor {
	protected DebugEventListener dbg;
	protected TreeAdaptor adaptor;

	public DebugTreeAdaptor(DebugEventListener dbg, TreeAdaptor adaptor) {
		this.dbg = dbg;
		this.adaptor = adaptor;
	}

	public Object create(Token payload) {
		Object n = adaptor.create(payload);
		dbg.createNode(adaptor.getUniqueID(n), payload.getTokenIndex());
		return n;
	}

	public Object dupTree(Object tree) {
		// TODO: do these need to be sent to dbg?
		return adaptor.dupTree(tree);
	}

	public Object dupNode(Object treeNode) {
		// TODO: do these need to be sent to dbg?
		return adaptor.dupNode(treeNode);
	}

	public Object nil() {
		Object n = adaptor.nil();
		dbg.nilNode(adaptor.getUniqueID(n));
		return n;
	}

	public boolean isNil(Object tree) {
		return adaptor.isNil(tree);
	}

	public void addChild(Object t, Object child) {
		adaptor.addChild(t,child);
		dbg.addChild(adaptor.getUniqueID(t), adaptor.getUniqueID(child));
	}

	public Object becomeRoot(Object newRoot, Object oldRoot) {
		Object n = adaptor.becomeRoot(newRoot, oldRoot);
		dbg.becomeRoot(adaptor.getUniqueID(n), adaptor.getUniqueID(oldRoot));
		return n;
	}

	public Object rulePostProcessing(Object root) {
		return adaptor.rulePostProcessing(root);
	}

	public void addChild(Object t, Token child) {
		Object n = this.create(child);
		this.addChild(t, n);
	}

	public Object becomeRoot(Token newRoot, Object oldRoot) {
		Object n = this.create(newRoot);
		adaptor.becomeRoot(n, oldRoot);
		dbg.becomeRoot(adaptor.getUniqueID(n), adaptor.getUniqueID(oldRoot));
		return n;
	}

	public Object create(int tokenType, Token fromToken) {
		Object n = adaptor.create(tokenType, fromToken);
		dbg.createNode(adaptor.getUniqueID(n), fromToken.getText(), tokenType);
		return n;
	}

	public Object create(int tokenType, Token fromToken, String text) {
		Object n = adaptor.create(tokenType, fromToken, text);
		dbg.createNode(adaptor.getUniqueID(n), text, tokenType);
		return n;
	}

	public Object create(int tokenType, String text) {
		Object n = adaptor.create(tokenType, text);
		dbg.createNode(adaptor.getUniqueID(n), text, tokenType);
		return n;
	}

	public int getType(Object t) {
		return adaptor.getType(t);
	}

	public void setType(Object t, int type) {
		adaptor.setType(t, type);
	}

	public String getText(Object t) {
		return adaptor.getText(t);
	}

	public void setText(Object t, String text) {
		adaptor.setText(t, text);
	}

	public Token getToken(Object t) {
		return adaptor.getToken(t);
	}

	public void setTokenBoundaries(Object t, Token startToken, Token stopToken) {
		adaptor.setTokenBoundaries(t, startToken, stopToken);
		if ( t!=null && startToken!=null && stopToken!=null ) {
			dbg.setTokenBoundaries(adaptor.getUniqueID(t),
								   startToken.getTokenIndex(),
								   stopToken.getTokenIndex());
		}
	}

	public int getTokenStartIndex(Object t) {
		return adaptor.getTokenStartIndex(t);
	}

	public int getTokenStopIndex(Object t) {
		return adaptor.getTokenStopIndex(t);
	}

	public Object getChild(Object t, int i) {
		return adaptor.getChild(t, i);
	}

	public int getChildCount(Object t) {
		return adaptor.getChildCount(t);
	}

	public int getUniqueID(Object node) {
		return adaptor.getUniqueID(node);
	}

	
	// support

	public DebugEventListener getDebugEventListener() {
		return dbg;
	}

	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}

}
