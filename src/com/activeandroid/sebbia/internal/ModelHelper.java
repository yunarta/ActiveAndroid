package com.activeandroid.sebbia.internal;

import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.activeandroid.sebbia.Cache;
import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.query.Select;
import com.activeandroid.sebbia.serializer.TypeSerializer;
import com.activeandroid.sebbia.util.Log;
import com.activeandroid.sebbia.util.ReflectionUtils;

public class ModelHelper {
	
	public static boolean isSerializable(Class<?> fieldType) {
		return Cache.getParserForType(fieldType) != null;
	}
	
	public static Object getSerializable(Cursor cursor, Class<?> fieldType, int columnIndex) {
		TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
		if (typeSerializer == null)
			return null;
		
		if (cursor.isNull(columnIndex))
			return null;
		
		fieldType = typeSerializer.getSerializedType();
		Object value = getValueFromCursor(cursor, fieldType, columnIndex);
		if (value != null) {
			value = typeSerializer.deserialize(value);
		}
		return value;
	}
	
	public static void setSerializable(ContentValues values, Class<?> fieldType, Object value, String fieldName) {
		if (value == null) {
			values.putNull(fieldName);
			return;
		}
		
		TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
		if (typeSerializer == null)
			return;
		value = typeSerializer.serialize(value);
		if (value != null) {
			fieldType = value.getClass();
			// check that the serializer returned what it promised
			if (!fieldType.equals(typeSerializer.getSerializedType())) {
				Log.w(String.format("TypeSerializer returned wrong type: expected a %s but got a %s",
						typeSerializer.getSerializedType(), fieldType));
			}
		}
		// TODO: Find a smarter way to do this? This if block is necessary because we
		// can't know the type until runtime.
		if (value == null) {
			values.putNull(fieldName);
		} else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
			values.put(fieldName, (Byte) value);
		}
		else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
			values.put(fieldName, (Short) value);
		}
		else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
			values.put(fieldName, (Integer) value);
		}
		else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
			values.put(fieldName, (Long) value);
		}
		else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
			values.put(fieldName, (Float) value);
		}
		else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
			values.put(fieldName, (Double) value);
		}
		else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
			values.put(fieldName, (Boolean) value);
		}
		else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
			values.put(fieldName, value.toString());
		}
		else if (fieldType.equals(String.class)) {
			values.put(fieldName, value.toString());
		}
		else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
			values.put(fieldName, (byte[]) value);
		}
		else if (ReflectionUtils.isModel(fieldType)) {
			values.put(fieldName, ((Model) value).getId());
		}
		else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
			values.put(fieldName, ((Enum<?>) value).name());
		}
	}
	
	public static void setSerializable(SQLiteStatement statement, Map<String, Integer> columns, Class<?> fieldType, Object value, String fieldName) {
		if (value == null) {
			return;
		}
		
		TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
		if (typeSerializer == null)
			return;
		
		int columnIndex = columns.get(fieldName);
		value = typeSerializer.serialize(value);
		if (value != null) {
			fieldType = value.getClass();
			// check that the serializer returned what it promised
			if (!fieldType.equals(typeSerializer.getSerializedType())) {
				Log.w(String.format("TypeSerializer returned wrong type: expected a %s but got a %s",
						typeSerializer.getSerializedType(), fieldType));
			}
		}
		// TODO: Find a smarter way to do this? This if block is necessary because we
		// can't know the type until runtime.
		if (value == null) {
			return;
		}
		
		if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
			statement.bindLong(columnIndex, (Long) value);
		}
		else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
			statement.bindLong(columnIndex, (Long) value);
		}
		else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
			statement.bindLong(columnIndex, (Long) value);
		}
		else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
			statement.bindLong(columnIndex, (Long) value);
		}
		else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
			statement.bindDouble(columnIndex, (Double) value);
		}
		else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
			statement.bindDouble(columnIndex,  (Double) value);
		}
		else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
			statement.bindLong(columnIndex, ((Boolean) value) ? 1L : 0L);
		}
		else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
			statement.bindString(columnIndex, value.toString());
		}
		else if (fieldType.equals(String.class)) {
			statement.bindString(columnIndex, (String) value);
		}
		else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
			statement.bindBlob(columnIndex, (byte[]) value);
		}
		else if (ReflectionUtils.isModel(fieldType)) {
			statement.bindLong(columnIndex, ((Model) value).getId());
		}
		else if (ReflectionUtils.isSubclassOf(fieldType, Enum.class)) {
			statement.bindString(columnIndex, ((Enum<?>) value).name());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Object getModel(Cursor cursor, Class<?> fieldType, int columnIndex) {
		final long entityId = cursor.getLong(columnIndex);
		final Class<? extends Model> entityType = (Class<? extends Model>) fieldType;

		Model entity = Cache.getEntity(entityType, entityId);
		if (entity == null) {
			entity = new Select().from(entityType).where(Cache.getTableInfo(entityType).getIdName() + "=?", entityId).executeSingle();
		}
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	public static Object getEnum(Cursor cursor, Class<?> fieldType, int columnIndex) {
		@SuppressWarnings("rawtypes")
		final Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
		String enumName = cursor.getString(columnIndex);
		if (!TextUtils.isEmpty(enumName))
			return Enum.valueOf(enumType, cursor.getString(columnIndex));
		return null;
	}
	
	public static Object getValueFromCursor(Cursor cursor, Class<?> fieldType, int columnIndex) {
		// TODO: Find a smarter way to do this? This if block is necessary because we
		// can't know the type until runtime.
		Object value = null;
		if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
			value = cursor.getInt(columnIndex);
		}
		else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
			value = cursor.getInt(columnIndex);
		}
		else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
			value = cursor.getInt(columnIndex);
		}
		else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
			value = cursor.getLong(columnIndex);
		}
		else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
			value = cursor.getFloat(columnIndex);
		}
		else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
			value = cursor.getDouble(columnIndex);
		}
		else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
			value = cursor.getInt(columnIndex) != 0;
		}
		else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
			value = cursor.getString(columnIndex).charAt(0);
		}
		else if (fieldType.equals(String.class)) {
			value = cursor.getString(columnIndex);
		}
		else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
			value = cursor.getBlob(columnIndex);
		}
		return value;
	}

}
