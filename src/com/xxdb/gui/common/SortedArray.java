package com.xxdb.gui.common;

import java.util.*;
/**
 * A cute sorted array class which implements binary tree searching 
 * It only store object with unique value. For store and sort duplicating data,
 * you can first store data to java.util.ArrayList and then call java.util.Collections to sort
 */

public class SortedArray<T  extends Comparable<? super T>> extends ArrayList<T>{
	private static final long serialVersionUID = 1L;
	
	private Comparator<T> comparator;
    private int insertedPos;

    public SortedArray(int capacity, Comparator<T> comparator) {
        super(capacity);
        this.comparator=comparator;
    }

    public SortedArray(int capacity) {
        super(capacity);
        this.comparator=null;

    }

    public SortedArray(Comparator<T> comparator) {
        super();
        this.comparator=comparator;
    }

    public SortedArray() {
        super();
        this.comparator=null;
    }

    public int insertedPos(){
        return insertedPos;
    }

    public boolean add(T key){
        int pos;

        if(comparator==null)
            pos=Collections.binarySearch(this,key);
        else
            pos=Collections.binarySearch(this,key,comparator);
        if(pos<0){
            insertedPos = pos * ( -1) - 1;
            super.add(insertedPos,key);
            return true;
        }
        else{
            insertedPos = pos;
            return false;
        }
    }

    public int binarySearch(T key){
        if(comparator==null)
            return Collections.binarySearch(this,key);
        else
           return Collections.binarySearch(this,key,comparator);
    }

    public int binarySearch(T key, int start){
        return binarySearch(this,key,start,this.size()-1,comparator);
    }

    public int binarySearch(T key, int start, int end){
        return binarySearch(this,key,start,end, comparator);
    }

    public boolean contains(T key){
        int pos;

        if(comparator==null)
            pos=Collections.binarySearch(this,key);
        else
            pos=Collections.binarySearch(this,key,comparator);
        return pos>=0;
    }

    public SortedArray<T> copy(Comparator<T> comparator){
        SortedArray<T> newList;

        newList=new SortedArray<>();
        newList.addAll(this);
        newList.setComparator(comparator);
        return newList;
    }

    public SortedArray<T> copy(){
        return copy(null);
    }

    public Comparator<T> getComparator(){
        return comparator;
    }

    public void setComparator(Comparator<T> comparator){
        this.comparator =comparator;
        if(comparator==null)
            Collections.sort(this);
        else
            Collections.sort(this,comparator);
    }

    public static <T extends Comparable<T>> int binarySearch(List<T> list, T obj){
        return binarySearch(list,obj,0,list.size()-1,null);
    }

    public static <T extends Comparable<T>> int binarySearch(List<T> list, T obj, Comparator<T> comparator){
        return binarySearch(list,obj,0,list.size()-1,comparator);
    }

    public static <T extends Comparable<T>> int binarySearch(List<T> list, T obj, int start, int end){
        return binarySearch(list,obj,start,end,null);
    }

    public static <T extends Comparable<? super T>> int binarySearch(List<T> list, T obj, int start, int end, Comparator<T> comparator){
        int middle, retvalue;

        while(start<=end){
            middle=(start+end)/2;
            if(comparator==null)
                retvalue=(obj).compareTo(list.get(middle));
            else
                retvalue=comparator.compare(obj,list.get(middle));
            if(retvalue==0)
                return middle;
            else if(retvalue>0)
                start=middle+1;
            else
                end=middle-1;
        }
        return -(start+1);
    }
    
    public boolean addAll(Collection<? extends T> arg0){
    	return addAll(arg0,true);
    }

    public boolean addAll(Collection<? extends T> arg0, boolean toSort){
    	
    	if(!super.addAll(arg0))
    		return false;
    	if(toSort){
	    	if(comparator==null)
				Collections.sort(this);
			else
				Collections.sort(this,comparator);
    	}
    	return true;
    }
}