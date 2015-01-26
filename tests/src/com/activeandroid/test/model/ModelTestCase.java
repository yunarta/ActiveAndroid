package com.activeandroid.test.model;

import com.activeandroid.sebbia.ActiveAndroid;
import com.activeandroid.sebbia.Configuration;
import com.activeandroid.test.ActiveAndroidTestCase;

public class ModelTestCase extends ActiveAndroidTestCase {
	@Override
	protected void setUp() throws Exception {
		Configuration configuration = new Configuration.Builder(getContext())
        .setDatabaseName("model.db")
        .setDatabaseVersion(3)
        .create();
		ActiveAndroid.initialize(configuration, true);
	}
}
