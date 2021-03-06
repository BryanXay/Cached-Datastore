package com.universeprojects.cacheddatastore;

import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class QueryHelper
{
	final private CachedDatastoreService ds;
	
	public QueryHelper(CachedDatastoreService ds)
	{
		this.ds = ds;
	}

	/**
	 * Use this if you only want to get a count of entities in the database.
	 * 
	 * This method will only count up to a maximum of 1000 entities.
	 * 
	 * This is more efficient than using the other filtered methods because it
	 * doesn't actually fetch all the data from the database, only a list of
	 * keys.
	 * 
	 * @param kind
	 * @param fieldName
	 * @param operator
	 * @param equalToValue
	 * @return
	 */
	public Integer getFilteredList_Count(String kind, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return ds.fetchAsList_Keys(kind, f1, 5000).size();
	}

	public List<CachedEntity> getFilteredList(String kind, int limit, Cursor cursor, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return ds.fetchAsList(kind, f1, limit, cursor);
	}

	public List<CachedEntity> getFilteredList(String kind, int limit, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return ds.fetchAsList(kind, f1, limit);
	}

	public List<CachedEntity> getFilteredList(String kind, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return ds.fetchAsList(kind, f1, 1000);
	}

	public Integer getFilteredList_Count(String kind, String fieldName, FilterOperator operator, Object equalToValue, String fieldName2, FilterOperator operator2, Object equalToValue2)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, operator2, equalToValue2);
		Filter f = CompositeFilterOperator.and(f1, f2);
		return ds.fetchAsList_Keys(kind, f, 5000).size();
	}

	public Integer getFilteredList_Count(String kind, String fieldName, FilterOperator operator, Object equalToValue, String fieldName2, FilterOperator operator2, Object equalToValue2, String fieldName3, FilterOperator operator3, Object equalToValue3)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, operator2, equalToValue2);
		FilterPredicate f3 = new FilterPredicate(fieldName3, operator3, equalToValue3);
		Filter f = CompositeFilterOperator.and(f1, f2, f3);
		return ds.fetchAsList_Keys(kind, f, 5000).size();
	}

	public List<CachedEntity> getFilteredList(String kind)
	{
		return ds.fetchAsList(kind, null, 1000);
	}

	public List<CachedEntity> getFilteredList(String kind, String fieldName, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		return ds.fetchAsList(kind, f1, 1000);
	}

	public List<CachedEntity> getFilteredList(String kind, String fieldName, Object equalToValue, String fieldName2, Object equalToValue2)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, FilterOperator.EQUAL, equalToValue2);
		Filter filter = CompositeFilterOperator.and(f1, f2);
		return ds.fetchAsList(kind, filter, 1000);
	}

	public List<CachedEntity> getFilteredORList(Cursor cursor, String kind, String fieldName, Object equalToValue, String fieldName2, Object equalToValue2)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, FilterOperator.EQUAL, equalToValue2);
		Filter filter = CompositeFilterOperator.or(f1, f2);
		Query q = new Query(kind);
		q.setFilter(filter);

		return ds.fetchAsList(q, 1000, cursor);
	}

	public List<CachedEntity> getFilteredORList(Cursor cursor, String kind, String fieldName, Object equalToValue, String fieldName2, Object equalToValue2, String fieldName3, Object equalToValue3)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, FilterOperator.EQUAL, equalToValue2);
		FilterPredicate f3 = new FilterPredicate(fieldName3, FilterOperator.EQUAL, equalToValue3);
		Filter filter = CompositeFilterOperator.or(f1, f2, f3);
		Query q = new Query(kind);
		q.setFilter(filter);

		return ds.fetchAsList(q, 1000, cursor);
	}	
	
	/**
	 * This performs an ancestor query to fetch all children (within a limit) 
	 * @param ds
	 * @param parent
	 * @param limit
	 * @return
	 */
	public List<CachedEntity> fetchChildren(Key parent, int limit)
	{
		Query q = new Query();
		q.setAncestor(parent);
		return ds.fetchAsList(q, limit);
	}


}
