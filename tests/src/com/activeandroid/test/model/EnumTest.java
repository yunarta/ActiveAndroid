package com.activeandroid.test.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.activeandroid.sebbia.ActiveAndroid;
import com.activeandroid.sebbia.Cache;
import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.TableInfo;
import com.activeandroid.sebbia.annotation.Column;
import com.activeandroid.sebbia.annotation.Table;
import com.activeandroid.sebbia.query.Delete;

public class EnumTest extends ModelTestCase {

	public enum Enumeration {
		TYPE_A,
		TYPE_B,
		TYPE_C
	}

	@Table(name = "enum_model")
	public static class EnumModel extends Model {

		@Column(name = "enum")
		Enumeration enumeration;

		public EnumModel() {

		}

		public EnumModel(Enumeration enumeration) {
			super();
			this.enumeration = enumeration;
		}

	}

	public void testEnumSaving() {
		new Delete().from(EnumModel.class).execute();
		List<EnumModel> models = new ArrayList<EnumModel>();
		for (Enumeration enumeration : Enumeration.values()) 
			models.add(new EnumModel(enumeration));
		
		Model.saveMultiple(models);
		
		TableInfo tableInfo = Cache.getTableInfo(EnumModel.class);
		Cursor cursor = ActiveAndroid.getDatabase().query(tableInfo.getTableName(), new String[] {"enum"}, null, null, null, null, null);
		while (cursor.moveToNext()) {
			assertTrue(cursor.getString(0).equals(Enumeration.values()[cursor.getPosition()]));
		}
		cursor.close();
	}
}
