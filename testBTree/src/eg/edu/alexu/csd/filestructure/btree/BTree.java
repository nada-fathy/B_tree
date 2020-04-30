package eg.edu.alexu.csd.filestructure.btree;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import javax.management.RuntimeErrorException;

import org.junit.Assert;

import com.sun.jdi.InvalidTypeException;




public class BTree <K extends Comparable<K>, V> implements IBTree<K, V>{
	
	IBTreeNode<K,V> root;
	int t;
	int height;
	
	BTree(int t){
		if(t==0 ||t==1) {
			throw new RuntimeErrorException(null);
		}
		this.t=t;
		root=null;
		height=0;
	}

	@Override
	public int getMinimumDegree() {
		return t;
	}

	@Override
	public IBTreeNode<K, V> getRoot() {
		return root;
	}

	@Override
	public void insert(K key, V value) {
		if(key==null || value==null) {
			throw new RuntimeErrorException(null);
		}
		if(root==null) {
			IBTreeNode<K, V>node=new BTreeNode<>(t,true);
			add(node,key,value);
			root=node;
			return;
		}
		if(exists(key,root)) {
			return;
		}
		boolean flag=false;
		IBTreeNode<K, V>node=root;
		while(!node.isLeaf()) {
			flag=false;
			if(node.getNumOfKeys()==2*t-1) {
				node=split(node,key);        // unchecked cast
				continue;
			}
			
			if(key.compareTo(node.getKeys().get(0))<0) {
				node=node.getChildren().get(0);
				continue;
			}
			else if(key.compareTo(node.getKeys().get(node.getNumOfKeys()-1))>0) {
				node=node.getChildren().get(node.getNumOfKeys());
				continue;
			}
			else {
				for(int i=1;(i<node.getNumOfKeys())&&flag==false;i++) {    //modification here
					if(key.compareTo(node.getKeys().get(i))<0) {
						node=node.getChildren().get(i);
						flag=true;
					}
				}
			}
		}
		if(node.getNumOfKeys()==2*t-1) {
			node=splitleaf(node,key);
		}
		add(node,key,value);
		
	}
	
	private void add(IBTreeNode<K,V> node,K key,V value) {
		int start=node.getNumOfKeys();
		List<K> keys=new ArrayList<>();
		keys.addAll(node.getKeys());
		
		List<V> values=new ArrayList<>();
		values.addAll(node.getValues());
		
		for(int i=start-1;i>=0;i--) {
			if(key.compareTo(node.getKeys().get(i))<0) {
				if(keys.size()>i+1) {
					keys.remove(i+1);
				}
				keys.add(i+1,keys.get(i));
				if(values.size()>i+1) {
					values.remove(i+1);
				}
				values.add(i+1,values.get(i));
				start=i;
			}
			else {
				break;
			}
		}
		if(keys.size()>start) {
			keys.remove(start);
			values.remove(start);
		}
		keys.add(start, key);
		values.add(start, value);
		node.setKeys(keys);
		node.setValues(values);
		node.setNumOfKeys(node.getNumOfKeys()+1);
	}
	
	private IBTreeNode<K,V> splitleaf(IBTreeNode<K,V> node,K key){
		int mid=node.getNumOfKeys()/2;
		IBTreeNode<K,V> n1=new BTreeNode<>(t,node.isLeaf());
		IBTreeNode<K,V> n2=new BTreeNode<>(t,node.isLeaf());
		List<K> keys1=new ArrayList<>();
		List<K> keys2=new ArrayList<>();
		List<V> values1=new ArrayList<>();
		List<V> values2=new ArrayList<>();
		
		for(int i=0;i<mid;i++) {
			keys1.add(i, node.getKeys().get(i));
			values1.add(i, node.getValues().get(i));
			n1.setNumOfKeys(n1.getNumOfKeys()+1);
		}
		n1.setKeys(keys1);
		n1.setValues(values1);
		
		int j=0;
		for(int i=mid+1;i<node.getNumOfKeys();i++) {
			keys2.add(j, node.getKeys().get(i));
			values2.add(j, node.getValues().get(i));
			n2.setNumOfKeys(n2.getNumOfKeys()+1);
			j++;
		}
		n2.setKeys(keys2);
		n2.setValues(values2);
		
		if(search4node(node.getKeys().get(0))==null) {
			K midkey=node.getKeys().get(mid);
			V midvalue=node.getValues().get(mid);
			
			List<K> newkey=new ArrayList<>();
			List<V> newvalue=new ArrayList<>();
			List<IBTreeNode<K, V>> newchildren=new ArrayList<>();
			newkey.add(0,midkey);
			newvalue.add(0,midvalue);
			node.setNumOfKeys(1);
			node.setLeaf(false);
			node.setKeys(newkey);
			node.setValues(newvalue);
			newchildren.add(0,n1);
			newchildren.add(1,n2);
			node.setChildren(newchildren);
			if(key.compareTo(midkey)>0) {
				return n2;
			}
			return n1;
		}
		List<K> parentkeys=new ArrayList<>();
		List<V> parentvalues=new ArrayList<>();
		List<IBTreeNode<K, V>> parentchildren=new ArrayList<>();
		
		IBTreeNode<K,V> parent=search4node(node.getKeys().get(0));
		parentkeys.addAll(parent.getKeys());
		parentvalues.addAll(parent.getValues());
		parentchildren.addAll(parent.getChildren());
		int index=parent.getNumOfKeys();
		K midkey=node.getKeys().get(mid);
		V midvalue=node.getValues().get(mid);
		for(int i=parent.getNumOfKeys()-1;i>=0;i--) {
			if(midkey.compareTo(parent.getKeys().get(i))<0) {   //modification
				if(parentkeys.size()>i+1) {
					parentkeys.remove(i+1);
					parentvalues.remove(i+1);
				}
				parentkeys.add(i+1,parent.getKeys().get(i));
				parentvalues.add(i+1,parent.getValues().get(i));
				if(parentchildren.size()>i+2) {
					parentchildren.remove(i+2);
				}
				parentchildren.add(i+2,parent.getChildren().get(i+1));   //shift by one
				index=i;
			}
			else {
				break;
			}
		}
		if(parentkeys.size()>index) {
			parentkeys.remove(index);
			parentvalues.remove(index);
		}
		parentkeys.add(index,midkey);
		parentvalues.add(index,midvalue);
		if(parentchildren.size()>index) {
			parentchildren.remove(index);
		}
		parentchildren.add(index,n1);
		if(parentchildren.size()>index+1) {
			parentchildren.remove(index+1);
		}
		parentchildren.add(index+1, n2);
		parent.setNumOfKeys(parent.getNumOfKeys()+1);
		parent.setKeys(parentkeys);
		parent.setValues(parentvalues);
		parent.setChildren(parentchildren);
		if(key.compareTo(midkey)>0) {
			return n2;
		}
		return n1;
	}
	
	
	private IBTreeNode<K,V> split(IBTreeNode<K,V> node,K key){
		int mid=node.getNumOfKeys()/2;
		IBTreeNode<K,V> n1=new BTreeNode<>(t,node.isLeaf());
		IBTreeNode<K,V> n2=new BTreeNode<>(t,node.isLeaf());
		List<K> keys1=new ArrayList<>();
		List<K> keys2=new ArrayList<>();
		List<V> values1=new ArrayList<>();
		List<V> values2=new ArrayList<>();
		List<IBTreeNode<K, V>> children1=new ArrayList<>();
		List<IBTreeNode<K, V>> children2=new ArrayList<>();
		
		for(int i=0;i<mid;i++) {
			keys1.add(i, node.getKeys().get(i));
			values1.add(i, node.getValues().get(i));
			children1.add(i, node.getChildren().get(i));
			n1.setNumOfKeys(n1.getNumOfKeys()+1);
		}
		children1.add(mid, node.getChildren().get(mid));
		n1.setKeys(keys1);
		n1.setValues(values1);
		n1.setChildren(children1);
		
		int j=0;
		for(int i=mid+1;i<node.getNumOfKeys();i++) {
			keys2.add(j, node.getKeys().get(i));
			values2.add(j, node.getValues().get(i));
			children2.add(j, node.getChildren().get(i));
			n2.setNumOfKeys(n2.getNumOfKeys()+1);
			j++;
		}
		children2.add(mid, node.getChildren().get(node.getNumOfKeys()));
		n2.setKeys(keys2);
		n2.setValues(values2);
		n2.setChildren(children2);
		
		if(search4node(node.getKeys().get(0))==null) {
			K midkey=node.getKeys().get(mid);
			V midvalue=node.getValues().get(mid);
			
			List<K> newkey=new ArrayList<>();
			List<V> newvalue=new ArrayList<>();
			List<IBTreeNode<K, V>> newchildren=new ArrayList<>();
			newkey.add(0,midkey);
			newvalue.add(0,midvalue);
			node.setNumOfKeys(1);
			node.setLeaf(false);
			node.setKeys(newkey);
			node.setValues(newvalue);
			newchildren.add(0,n1);
			newchildren.add(1,n2);
			node.setChildren(newchildren);
			if(key.compareTo(midkey)>0) {
				return n2;
			}
			return n1;
		}
		List<K> parentkeys=new ArrayList<>();
		List<V> parentvalues=new ArrayList<>();
		List<IBTreeNode<K, V>> parentchildren=new ArrayList<>();
		
		IBTreeNode<K,V> parent=search4node(node.getKeys().get(0));
		parentkeys.addAll(parent.getKeys());
		parentvalues.addAll(parent.getValues());
		parentchildren.addAll(parent.getChildren());
		int index=parent.getNumOfKeys();
		K midkey=node.getKeys().get(mid);
		V midvalue=node.getValues().get(mid);
		for(int i=parent.getNumOfKeys()-1;i>=0;i--) {
			if(midkey.compareTo(parent.getKeys().get(i))<0) {
				if(parentkeys.size()>i+1) {
					parentkeys.remove(i+1);
					parentvalues.remove(i+1);
				}
				parentkeys.add(i+1,parent.getKeys().get(i));
				parentvalues.add(i+1,parent.getValues().get(i));
				if(parentchildren.size()>i+2) {
					parentchildren.remove(i+2);
				}
				parentchildren.add(i+2,parent.getChildren().get(i+1));
				index=i;
			}
			else {
				break;
			}
		}
		if(parentkeys.size()>index) {
			parentkeys.remove(index);
			parentvalues.remove(index);
		}
		parentkeys.add(index,midkey);
		parentvalues.add(index,midvalue);
		
		if(parentchildren.size()>index) {
			parentchildren.remove(index);
		}
		parentchildren.add(index,n1);
		
		if(parentchildren.size()>index+1) {
			parentchildren.remove(index+1);
		}
		parentchildren.add(index+1, n2);
		parent.setNumOfKeys(parent.getNumOfKeys()+1);
		parent.setKeys(parentkeys);
		parent.setValues(parentvalues);
		parent.setChildren(parentchildren);
		if(key.compareTo(midkey)>0) {
			return n2;
		}
		return n1;
		
	}
	
	private IBTreeNode<K, V> search4node(K key){    //searching for a parent of node
		boolean flag=false;
		int i=0;
		IBTreeNode<K,V> node=root;
		while (!node.isLeaf()) {
			flag=false;
			for(int j=0;j<node.getChildren().size();j++) {
				if(node.getChildren().get(j).getKeys().contains(key)) {
					return node;
				}
			}
			for( i=0;i<node.getNumOfKeys() &&flag==false;i++) {
				if(key.compareTo(node.getKeys().get(i))<0) {
					node=node.getChildren().get(i);
					flag=true;
				}
			}
			if(flag==false) {
				node=node.getChildren().get(node.getNumOfKeys());
			}
		}
		return null;
	}
	
	private boolean exists(K key,IBTreeNode<K,V> node) {
		if(node.isLeaf()) {
			for(int i=0;i<node.getNumOfKeys();i++) {
				if(key.compareTo(node.getKeys().get(i))==0) {
					return true;
				}
			}
			return false;
		}
		for(int i=0;i<node.getNumOfKeys();i++) {
			if(key.compareTo(node.getKeys().get(i))==0) {
				return true;
			}
			if(key.compareTo(node.getKeys().get(i))<0) {
				return exists(key,node.getChildren().get(i));
			}
		}
			return exists(key,node.getChildren().get(node.getNumOfKeys()));
		
	}

	@Override
	public V search(K key) {
		if(key==null) {
			throw new RuntimeErrorException(null);
		}
		return searchHelper(key,root);
		
	}
	private V searchHelper(K key,IBTreeNode<K,V>node) {
		if(node==null) {                  //modification here
			return null;
		}
		if(node.isLeaf()) {
			for(int i=0;i<node.getNumOfKeys();i++) {
				if(key.compareTo(node.getKeys().get(i))==0) {
					return node.getValues().get(i);
				}
			}
			return null;
		}
		for(int i=0;i<node.getNumOfKeys();i++) {
			if(key.compareTo(node.getKeys().get(i))==0) {
				return node.getValues().get(i);
			}
			if(key.compareTo(node.getKeys().get(i))<0) {
				return searchHelper(key,node.getChildren().get(i));
			}
		}
			return searchHelper(key,node.getChildren().get(node.getNumOfKeys()));
	}

	public boolean delete(K key) {
		if(search(key)==null) {
			return false;
		}
		boolean flag =( (BTreeNode<K,V>)root).remove(key);
		if(!flag) {
			return false;
		}
		if (root.getNumOfKeys() == 0)
		{
			if (root.isLeaf())
			{
				root = null;
			}
			else
			{
				root =  root.getChildren().get(0);
			}
		}

		return true;
	}
	
	 

}

