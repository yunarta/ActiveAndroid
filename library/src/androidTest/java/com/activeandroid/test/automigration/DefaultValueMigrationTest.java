package com.activeandroid.test.automigration;

import android.database.Cursor;

import com.activeandroid.sebbia.ActiveAndroid;
import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.annotation.Column;
import com.activeandroid.sebbia.annotation.Table;
import com.activeandroid.sebbia.query.Select;

import java.util.List;

public class DefaultValueMigrationTest extends AutoMigrationTest
{
    private static final String TABLE = "default_value_migration";

    @Table(name = TABLE)
    public static class DefaultValueMigrationModel extends Model
    {
        @Column(name = "textValue")
        public String  textValue;
        @Column(name = "boolValue")
        public boolean boolValue;
        @Column(name = "floatValue")
        public float   floatValue;
        @Column(name = "defaultValue", defaultValue = "some_value")
        public String  defaultValue;

        public DefaultValueMigrationModel()
        {

        }
    }

    public DefaultValueMigrationTest()
    {
        super(TABLE);
    }

    public void testDefaultValueMigrationTest()
    {
        createOldDatabase();
        initializeActiveAndroid(DefaultValueMigrationModel.class);
        List<DefaultValueMigrationModel> migrationModels = new Select().from(DefaultValueMigrationModel.class).execute("test");
        assertEquals(10, migrationModels.size());
        for (int i = 0; i < 10; ++i)
        {
            DefaultValueMigrationModel migrationModel = migrationModels.get(i);
            assertEquals(Long.valueOf(i + 1), migrationModel.getId());
            assertEquals("Text " + i, migrationModel.textValue);
            assertEquals(i % 2 == 0, migrationModel.boolValue);
            assertEquals((float) i, migrationModel.floatValue);
            assertEquals("some_value", migrationModel.defaultValue);
        }

        Cursor cursor = ActiveAndroid.getDatabase("test").query(TABLE, null, null, null, null, null, null);
        assertTrue(cursor.getColumnIndex("unusedColumn") != -1);
    }

}
