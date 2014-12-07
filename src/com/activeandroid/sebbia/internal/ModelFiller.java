package com.activeandroid.sebbia.internal;

import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.activeandroid.sebbia.Model;

public abstract class ModelFiller {
	public static final String SUFFIX = "$$ActiveAndroidModelFiller";
	public ModelFiller superModelFiller;

	public abstract void loadFromCursor(Model model, Cursor cursor);

	public abstract void fillContentValues(Model model, ContentValues contentValues);

	public abstract void bindStatement(Model model, SQLiteStatement statement, Map<String, Integer> columns);
}
