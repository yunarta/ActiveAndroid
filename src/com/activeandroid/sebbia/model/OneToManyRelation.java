package com.activeandroid.sebbia.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.activeandroid.sebbia.Cache;
import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.TableInfo;
import com.activeandroid.sebbia.annotation.Column;
import com.activeandroid.sebbia.annotation.DoNotGenerate;
import com.activeandroid.sebbia.query.Delete;
import com.activeandroid.sebbia.util.Log;

@DoNotGenerate
public abstract class OneToManyRelation<T1 extends Model, T2 extends Model> extends Model {

	@Column (name = "entity1")
	private T1 entity1;
	@Column (name = "entity2Type")
	private String entity2Type;
	@Column (name = "entity2")
	private Model entity2;

	public static <T1 extends Model, T2 extends Model> void setRelations(Class<? extends OneToManyRelation<T1, T2>> relation, T1 entity1, List<T2> entities2) {
		if (entity1.getId() == null)
			throw new IllegalArgumentException(entity1.getClass().getSimpleName() + " is not saved to database yet, aborting");
		for (T2 entity2 : entities2) {
			if (entity2.getId() == null)
				throw new IllegalArgumentException(entity2.getClass().getSimpleName() + " is not saved to database yet, aborting");
		}

		new Delete().from(relation).where("entity1 = ?", entity1.getId()).execute();
		try {
			List<OneToManyRelation<T1, T2>> connections = new ArrayList<OneToManyRelation<T1, T2>>();
			for (T2 entity2 : entities2) {
				OneToManyRelation<T1, T2> connection = relation.newInstance();
				connection.entity1 = entity1;
				connection.entity2Type = entity2.getClass().getCanonicalName();
				connection.entity2 = entity2;
				connections.add(connection);
			}
			saveMultiple(connections);
		} catch (Exception e) {
			Log.e("Cannot create instance of class " + relation.getSimpleName());
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T1 extends Model, T2 extends Model> List<T2> getRelations(Class<? extends OneToManyRelation<T1, T2>> relation, T1 entity) {
		if (entity.getId() == null)
			throw new IllegalArgumentException(entity.getClass().getSimpleName() + " is not saved to database yet, aborting");

		TableInfo crossTableInfo = Cache.getTableInfo(relation);
		Cursor cursor = Cache.openDatabase().rawQuery("SELECT entity2Type, entity2 FROM " + crossTableInfo.getTableName() + " WHERE entity1 = ?", new String[] {entity.getId().toString()});
		final List<T2> entities = new ArrayList<T2>();
		try {
			if (cursor.moveToFirst()) {
				do {
					String typeName = cursor.getString(0);
					Class<? extends Model> entity2Class = (Class<? extends Model>) Class.forName(typeName);
					entities.add((T2) Model.load(entity2Class, cursor.getLong(1)));
				}
				while (cursor.moveToNext());
			}
		}
		catch (Exception e) {
			Log.e("Failed to process cursor.", e);
			throw new RuntimeException(e);
		} finally {
			cursor.close();
		}

		return entities;
	}

	public OneToManyRelation() {
		super();
	}
}
