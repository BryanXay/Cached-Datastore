package com.universeprojects.cacheddatastore;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

public class CachedEntity implements Cloneable,Serializable {
	private static Logger log = Logger.getLogger(CachedEntity.class.toString());
	
	private static final long serialVersionUID = 3034412029610092898L;
	private Entity entity;
	boolean unsavedChanges = false;
	
	public CachedEntity(Key key)
	{
		this(new Entity(key));
	}
	
	/**
	 * This will generate the entity using the by-ID method, but 
	 * it will use preallocated IDs instead of waiting to finish the
	 * key on the first .put().
	 * 
	 * @param kind
	 * @param ds
	 */
	public CachedEntity(String kind, CachedDatastoreService ds)
	{
		this(new Entity(kind, ds.getPreallocatedIdFor(kind)));

		unsavedChanges = true;
	}
	
	public CachedEntity(String kind)
	{
		this(new Entity(kind));
		
		unsavedChanges = true;
	}
	
	public CachedEntity(String kind, Key parent)
	{
		this(new Entity(kind, parent));
		unsavedChanges = true;
	}
	
	public CachedEntity(String kind, long id)
	{
		this(new Entity(kind, id));
		unsavedChanges = true;
	}
	
	public CachedEntity(String kind, long id, Key parent)
	{
		this(new Entity(kind, id, parent));
		unsavedChanges = true;
	}
	
	public CachedEntity(String kind, String keyName)
	{
		this(new Entity(kind, keyName));
		unsavedChanges = true;
	}
	
	public CachedEntity(String kind, String keyName, Key parent)
	{
		this(new Entity(kind, keyName, parent));
		unsavedChanges = true;
	}
	
	
	
	private CachedEntity(Entity entity) {
		this.entity = entity;
	}
	
	
	/**
	 * Since this class is meant to make Entity more compatible with
	 * the schema system for the GEF, it is recommended to just use
	 * CachedEntity for normal Entity interactions. 
	 * 
	 * If you must, use this call wisely.
	 * 
	 * @return
	 */
	public Entity getEntity()
	{
		return entity;
	}
	
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public CachedEntity clone()
	{
		return new CachedEntity(entity.clone());
	}
	
	/**
	 * If this entity has an incomplete key, this
	 * method can be used to set the ID on the entity.
	 * 
	 * @param id
	 */
	protected void setId(long id)
	{
		if (entity.getKey().isComplete())
			throw new IllegalStateException("You cannot set an ID for an entity that already has a complete key.");
		
		// First create the new entity with the new ID
		Entity newEntity = new Entity(entity.getKind(), id);
		CachedDatastoreService.copyFieldValues(entity, newEntity);
		
		entity = newEntity;
	}
	
	/**
	 * This method will go through all field values stored in the entity
	 * looking for references to the original key and replacing it with 
	 * the new key. 
	 * 
	 * @param newKey
	 */
	protected void updateStoredKey(Key originalKey, Key newKey)
	{
		for(String field:this.getProperties().keySet())
		{
			Object value = this.getProperty(field);
			if (value instanceof Key)
			{
				if (originalKey == value)
				{
					this.setProperty(field, newKey);
				
					log.warning(this.getKey()+"."+field+" was "+originalKey+" and is now "+newKey);
				}
			}
			else if (value instanceof List)
			{
				List list = (List)value;
				for(int i = 0; i<list.size(); i++)
					if (originalKey == list.get(i))
					{
						list.set(i, newKey);
						
						log.warning(this.getKey()+"."+field+"["+i+"] was "+originalKey+" and is now "+newKey);
					}
			}
		}
		
	}
	
	public boolean equals(Object object)
	{
		if (object==null) return false;
		if (object==this)
			return true;
		if (object instanceof CachedEntity)
			return this.entity.equals(((CachedEntity)object).entity);
		else if (object instanceof Entity)
			return this.entity.equals(object);
		else
			return false;
	}
	
	public String getAppId()
	{
		return entity.getAppId();
	}
	
	public Key getKey()
	{
		return entity.getKey();
	}
	
	public String getKind()
	{
		return entity.getKind();
	}
	
	public Long getId()
	{
		return entity.getKey().getId();
	}
	
	public String getNamespace()
	{
		return entity.getNamespace();
	}
	
	public Key getParent()
	{
		return entity.getParent();
	}
	
	public int hashCode()
	{
		return entity.hashCode();
	}
	
	public String toString()
	{
		return entity.toString();
	}
	
	public Map<String, Object> getProperties()
	{
		return entity.getProperties();
	}
	
	public Object getProperty(String propertyName)
	{
		if (entity.getProperty(propertyName) instanceof Text)
			return ((Text)entity.getProperty(propertyName)).getValue();
		else
			return entity.getProperty(propertyName);
	}
	
	public boolean hasProperty(String propertyName)
	{
		return entity.hasProperty(propertyName);
	}
	
	public boolean isUnindexedProperty(String propertyName)
	{
		return entity.isUnindexedProperty(propertyName);
	}
	
	public void removeProperty(String propertyName)
	{
		entity.removeProperty(propertyName);
	}
	
	
	public void setProperty(String propertyName, Object value)
	{
		// Determine if this field should be indexed or not depending on the schema
		CachedSchema schema = SchemaInitializer.getSchema();

		Boolean unindexed=false;

		if (schema!=null)
		{
			unindexed = schema.isFieldUnindexed(entity.getKind(), propertyName);

			String fieldType = schema.getFieldTypeName(entity.getKind(), propertyName);
			// Here is a little nice thing to convert a string to a Text if that is the setting in the schema field
			if ("Text".equals(fieldType) && value instanceof String)
				value = new Text((String)value);
		}

		
		
		if (unindexed)
			entity.setUnindexedProperty(propertyName, value);
		else
			entity.setProperty(propertyName, value);
		
		unsavedChanges = true;
	}
	
	public static CachedEntity wrap(Entity obj)
	{
		if (obj==null) return null;
		return new CachedEntity(obj);
	}

	public void setPropertyManually(String propertyName, Object value)
	{
		entity.setProperty(propertyName, value);
		
		unsavedChanges = true;
	}
	
	public void setUnindexedPropertyManually(String propertyName, Object value)
	{
		entity.setUnindexedProperty(propertyName, value);
		
		unsavedChanges = true;
	}
	
	public boolean isUnsaved()
	{
		return unsavedChanges;
	}
	
	

}
