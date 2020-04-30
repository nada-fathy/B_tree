package eg.edu.alexu.csd.filestructure.btree;

import java.util.ArrayList;
import java.util.List;


public class BTreeNode <K extends Comparable<K>, V> implements IBTreeNode<K, V> {
	
	private int t;     //minimum degree
	private  List<IBTreeNode<K, V>> children;
	private List<K> keys;
	private List<V> values;
	private int numKeys=0;
	private boolean leaf;
	BTreeNode<K,V> parent;
	
	public BTreeNode(int t,boolean isLeaf) {
		leaf=isLeaf;
		this.t=t;
		keys=new ArrayList<>();
		values=new ArrayList<>();
		children=new ArrayList<>();
		parent=null;
		numKeys=0;
	}
	
	@Override
	public int getNumOfKeys() {
		return numKeys;
	}

	@Override
	public void setNumOfKeys(int numOfKeys) {
		numKeys=numOfKeys;
	}

	@Override
	public boolean isLeaf() {
		return leaf;
	}

	@Override
	public void setLeaf(boolean isLeaf) {
		leaf=isLeaf;
		
	}

	@Override
	public List<K> getKeys() {
		return keys;
	}

	@Override
	public void setKeys(List<K> keys) {
		this.keys=new ArrayList<>();
		this.keys.addAll(keys);
	}

	@Override
	public List<V> getValues() {
		return values;
	}

	@Override
	public void setValues(List<V> values) {
		this.values=new ArrayList<>();
		this.values.addAll(values);
		
	}

	@Override
	public List<IBTreeNode<K, V>> getChildren() {
		return children;
	}

	@Override
	public void setChildren(List<IBTreeNode<K, V>> children) {
		this.children=new ArrayList<>();
		this.children.addAll(children);
		
	}
	public boolean remove(K key) {
		int pos = findKey(key);
		if (pos < getNumOfKeys() && keys.get(pos).equals(key))
		{
			if (isLeaf())
			{
				removeFromLeaf(pos);
			}
			else
			{
				removeFromNonLeaf(pos);
			}
		}
		else {
			if (isLeaf())
			{
				return false;
			}
			boolean flag = ((pos == getNumOfKeys())? true : false);
			
			if (children.get(pos).getNumOfKeys() < t)
			{
				fill(pos);
			}
			
			if (flag && pos > getNumOfKeys())
			{
				return ((BTreeNode<K, V>) children.get(pos- 1)).remove(key);
			}
			else
			{
				return ((BTreeNode<K, V>) children.get(pos)).remove(key);
			}
		}
		return true;
	}
	private int findKey( K key)
	{
		int idorder = 0;
		while ( idorder < getNumOfKeys() && (keys.get( idorder).compareTo(key) < 0))
		{
			++ idorder;
		}
		return  idorder;
	}
	private void removeFromLeaf(int  idorder)
	{
	for (int i =  idorder + 1; i < getNumOfKeys(); ++i)
	{
		keys.set(i - 1, keys.get(i));
		values.set(i - 1, values.get(i));
	}
	numKeys--;
	return;
}
	private void removeFromNonLeaf(int  idorder)
	{
	K k = keys.get( idorder);
	 if (children.get( idorder).getNumOfKeys() >= t)
	{
		IBTreeNode<K, V> pred = getPred( idorder);
		keys.set( idorder, pred.getKeys().get(pred.getNumOfKeys() - 1));
		values.set( idorder, pred.getValues().get(pred.getNumOfKeys() - 1));
		((BTreeNode<K, V>) children.get( idorder)).remove(pred.getKeys().get(pred.getNumOfKeys() - 1));
	}
	 else if (children.get( idorder + 1).getNumOfKeys() >= t)
	{
		 IBTreeNode<K, V> succ = getSucc( idorder);
		 keys.set( idorder, succ.getKeys().get(0));
		 values.set( idorder, succ.getValues().get(0));
		((BTreeNode<K, V>) children.get( idorder + 1)).remove(succ.getKeys().get(0));
	} 
	else
	{
		merge( idorder);
		((BTreeNode<K, V>) children.get( idorder)).remove(k);
	}
	return;
}
	private IBTreeNode<K, V> getPred(int  idorder)
	{
	IBTreeNode<K, V> cur = children.get( idorder);
	while (!cur.isLeaf())
	{
		cur = cur.getChildren().get(cur.getNumOfKeys());
	}
	return cur;
}
	
	private IBTreeNode<K, V> getSucc(int  idorder)
	{
		IBTreeNode<K, V> cur = children.get( idorder + 1);
		while (!cur.isLeaf())
		{
			cur = cur.getChildren().get(0);
		}
		return cur;
	}

	private void fill(int  idorder)
	{
		if ( idorder != 0 && children.get( idorder- 1).getNumOfKeys() >= t)
		{
			borrowFromPrev( idorder);
		}
		else if ( idorder != numKeys && children.get( idorder+ 1).getNumOfKeys() >= t)
		{
			borrowFromNext( idorder);
		}
		else
		{
			if ( idorder != numKeys )
			{
				merge( idorder);
			}
			else
			{
				merge( idorder - 1);
			}
		}
		return;
	}
	
	private void borrowFromPrev(int  idorder)
	{
		IBTreeNode<K, V> child = children.get( idorder);
		IBTreeNode<K, V> sibling = children.get( idorder- 1);
		child.getKeys().add(child.getNumOfKeys(), child.getKeys().get(child.getNumOfKeys() - 1));
		child.getValues().add(child.getNumOfKeys(), child.getValues().get(child.getNumOfKeys() - 1));
		
		for (int i = child.getNumOfKeys() - 2; i >= 0; --i)
		{
			child.getKeys().set(i+1, child.getKeys().get(i));
			child.getValues().set(i+1, child.getValues().get(i));

		}
		if (!child.isLeaf())
		{
			child.getChildren().add(child.getNumOfKeys() + 1, child.getChildren().get(child.getNumOfKeys()));
			
			for (int i = child.getNumOfKeys() - 1; i >= 0; --i)
			{
				child.getChildren().set(i+1, child.getChildren().get(i));
			}
		}
		child.getKeys().set(0, keys.get( idorder - 1));
		child.getValues().set(0, values.get( idorder - 1));

		if (!child.isLeaf())
		{
			child.getChildren().set(0, sibling.getChildren().get(sibling.getNumOfKeys()));
		}
		keys.set( idorder- 1, sibling.getKeys().get(sibling.getNumOfKeys() - 1));
		values.set( idorder- 1, sibling.getValues().get(sibling.getNumOfKeys() - 1));
		
		child.setNumOfKeys(child.getNumOfKeys() + 1);
		sibling.setNumOfKeys(sibling.getNumOfKeys() - 1);
		return;
	}

	private void borrowFromNext(int  idorder)
	{
		IBTreeNode<K, V> child = children.get( idorder);
		IBTreeNode<K, V> sibling = children.get( idorder + 1);
		
		child.getKeys().add(child.getNumOfKeys(), keys.get( idorder));
		child.getValues().add(child.getNumOfKeys(), values.get( idorder));

		if (!child.isLeaf())
		{
			child.getChildren().add(child.getNumOfKeys() + 1, sibling.getChildren().get(0));
		}
		keys.set( idorder, sibling.getKeys().get(0));
		values.set( idorder, sibling.getValues().get(0));

		for (int i = 1; i < sibling.getNumOfKeys(); ++i)
		{
			sibling.getKeys().set(i - 1, sibling.getKeys().get(i));
			sibling.getValues().set(i - 1, sibling.getValues().get(i));
		}
		
		if (!sibling.isLeaf())
		{
			for (int i = 1; i <= sibling.getNumOfKeys(); ++i)
			{
				sibling.getChildren().set(i - 1, sibling.getChildren().get(i));
			}
		}
		child.setNumOfKeys(child.getNumOfKeys() + 1);
		sibling.setNumOfKeys(sibling.getNumOfKeys() - 1);
		return;
	}

	public void merge(int pos)
	{
		IBTreeNode<K, V> child = children.get(pos);
		IBTreeNode<K, V> sibling = children.get(pos+ 1);
		child.getKeys().add(t - 1, keys.get(pos));
		child.getValues().add(t - 1, values.get(pos));

		for (int i = 0; i < sibling.getNumOfKeys(); ++i)
		{ 
			if ((t + i) >= child.getNumOfKeys()) {
				child.getKeys().add(t + i, sibling.getKeys().get(i));
				child.getValues().add(t + i, sibling.getValues().get(i));
			} else {
				child.getKeys().set(t + i, sibling.getKeys().get(i));
				child.getValues().set(t + i, sibling.getValues().get(i));
			}
		}
		if (!child.isLeaf())
		{
			for (int i = 0; i <= sibling.getNumOfKeys(); ++i)
			{
				if (t + i > child.getNumOfKeys()) {
					child.getChildren().add(i + t, sibling.getChildren().get(i));
				}else {
					child.getChildren().set(i + t, sibling.getChildren().get(i));
				}
			}
		}
		for (int i = pos + 1; i < numKeys; ++i)
		{
			keys.set(i - 1, keys.get(i));
			values.set(i - 1, values.get(i));
		}
		for (int i = pos + 2; i <= numKeys; ++i)
		{
			children.set(i - 1, children.get(i));
		}
		child.setNumOfKeys(child.getNumOfKeys() + sibling.getNumOfKeys() + 1);

		numKeys--;		
		return;
	}

}