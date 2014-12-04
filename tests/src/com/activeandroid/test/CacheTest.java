package com.activeandroid.test;

import java.util.Collection;

import android.test.AndroidTestCase;

import com.activeandroid.sebbia.ActiveAndroid;
import com.activeandroid.sebbia.Cache;
import com.activeandroid.sebbia.Configuration;
import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.TableInfo;
import com.activeandroid.sebbia.annotation.Table;

public class CacheTest extends AndroidTestCase {

    @Override
    protected void setUp() {
        Configuration conf = new Configuration.Builder(getContext())
                .setDatabaseName("CacheTest")
                .addModelClasses(CacheTestModel.class, CacheTestModel2.class)
                .create();
        ActiveAndroid.initialize(conf, true);
    }

    public void testGetTableInfos() {
        assertNotNull(Cache.getContext());
        Collection<TableInfo> tableInfos = Cache.getTableInfos();
        assertEquals(2, tableInfos.size());

        {
            TableInfo tableInfo = Cache.getTableInfo(CacheTestModel.class);
            assertNotNull(tableInfo);
            assertEquals("CacheTestModel", tableInfo.getTableName());
        }

        {
            TableInfo tableInfo = Cache.getTableInfo(CacheTestModel2.class);
            assertNotNull(tableInfo);
            assertEquals("CacheTestModel2", tableInfo.getTableName());
        }
    }

    @Table(name = "CacheTestModel")
    static class CacheTestModel extends Model {
    }

    @Table(name = "CacheTestModel2")
    static class CacheTestModel2 extends Model {
    }
}
