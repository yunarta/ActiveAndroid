package com.activeandroid.sebbia.internal;

import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.activeandroid.sebbia.Model;

public class EmptyModelFiller extends ModelFiller {

	@Override
	public void loadFromCursor(Model model, Cursor cursor) {
		if (superModelFiller != null)
			superModelFiller.loadFromCursor(model, cursor);
	}

	@Override
	public void fillContentValues(Model model, ContentValues contentValues) {
		if (superModelFiller != null)
			superModelFiller.fillContentValues(model, contentValues);
	}

	@Override
	public void bindStatement(Model model, SQLiteStatement statement, Map<String, Integer> columns) {
		if (superModelFiller != null)
			superModelFiller.bindStatement(model, statement, columns);
	}
}
