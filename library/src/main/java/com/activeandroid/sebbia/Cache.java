package com.activeandroid.sebbia;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LruCache;
import android.util.SparseArray;

import com.activeandroid.sebbia.annotation.DoNotGenerate;
import com.activeandroid.sebbia.internal.EmptyModelFiller;
import com.activeandroid.sebbia.internal.ModelFiller;
import com.activeandroid.sebbia.serializer.TypeSerializer;
import com.activeandroid.sebbia.util.Log;
import com.activeandroid.sebbia.util.ReflectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Cache
{
    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////////

    public static final int DEFAULT_CACHE_SIZE = 1024;

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    private static Context sContext;

    private static ModelInfo sModelInfo;

    private static SparseArray<DatabaseHelper> sDatabaseHelper = new SparseArray<>();;

    private static LruCache<String, Model> sEntities;

    private static boolean sIsInitialized = false;

    private static Map<Class<? extends Model>, ModelFiller> sFillers;
    private static int                                      sVersion;
    private static String                                   sSqlParser;

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    private Cache()
    {
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public static synchronized void initialize(Configuration configuration)
    {
        if (sIsInitialized)
        {
            Log.v("ActiveAndroid already initialized.");
            return;
        }

        sContext = configuration.getContext();
        sModelInfo = new ModelInfo(configuration);

        sVersion = configuration.getDatabaseVersion();
        sSqlParser = configuration.getSqlParser();

        sDatabaseHelper = new SparseArray<>();
        // sDatabaseHelper = new DatabaseHelper(configuration.getContext(), configuration.getDatabaseName(), sVersion, sSqlParser);

        // TODO: It would be nice to override sizeOf here and calculate the memory
        // actually used, however at this point it seems like the reflection
        // required would be too costly to be of any benefit. We'll just set a max
        // object size instead.
        sEntities = new LruCache<String, Model>(configuration.getCacheSize());

        initializeModelFillers();

        // openDatabase(database.hashCode());

        sIsInitialized = true;

        Log.v("ActiveAndroid initialized successfully.");
    }


    public static synchronized void clear()
    {
        sEntities.evictAll();
        Log.v("Cache cleared.");
    }

    public static synchronized void dispose()
    {
        int size = sDatabaseHelper.size();
        for (int i = 0; i < size; i++)
        {
            DatabaseHelper helper = sDatabaseHelper.valueAt(i);
            helper.close();
        }

        sEntities = null;
        sModelInfo = null;
        sDatabaseHelper = null;

        sIsInitialized = false;

        Log.v("ActiveAndroid disposed. Call initialize to use library.");
    }

    // Database access

    public static boolean isInitialized()
    {
        return sIsInitialized;
    }

    public static synchronized SQLiteDatabase openDatabase(String database)
    {
        int key = database.hashCode();

        DatabaseHelper helper = sDatabaseHelper.get(key);
        if (helper == null)
        {
            helper = new DatabaseHelper(sContext, database, sVersion, sSqlParser);
            sDatabaseHelper.put(key, helper);
        }

        return helper.getWritableDatabase();
    }

    public static synchronized void closeDatabase(String database)
    {
        int key = database.hashCode();

        DatabaseHelper helper = sDatabaseHelper.get(key);
        if (helper != null)
        {
            helper.close();
        }
    }

    // Context access

    public static Context getContext()
    {
        return sContext;
    }

    // Entity cache

    public static String getIdentifier(Class<? extends Model> type, Long id)
    {
        return getTableName(type) + "@" + id;
    }

    public static String getIdentifier(Model entity)
    {
        return getIdentifier(entity.getClass(), entity.getId());
    }

    public static synchronized void addEntity(Model entity)
    {
        sEntities.put(getIdentifier(entity), entity);
    }

    public static synchronized Model getEntity(Class<? extends Model> type, long id)
    {
        return sEntities.get(getIdentifier(type, id));
    }

    public static synchronized void removeEntity(Model entity)
    {
        sEntities.remove(getIdentifier(entity));
    }

    // Model cache

    public static synchronized Collection<TableInfo> getTableInfos()
    {
        return sModelInfo.getTableInfos();
    }

    public static synchronized TableInfo getTableInfo(Class<? extends Model> type)
    {
        return sModelInfo.getTableInfo(type);
    }

    public static synchronized TypeSerializer getParserForType(Class<?> type)
    {
        return sModelInfo.getTypeSerializer(type);
    }

    public static synchronized String getTableName(Class<? extends Model> type)
    {
        return sModelInfo.getTableInfo(type).getTableName();
    }

    public static synchronized String getDatabaseName(Class<? extends Model> type)
    {
        return sModelInfo.getTableInfo(type).getDatabaseName();
    }

    static ModelFiller getFiller(Class<? extends Model> type)
    {
        return sFillers.get(type);
    }

    private static void initializeModelFillers()
    {
        sFillers = new HashMap<Class<? extends Model>, ModelFiller>();
        for (TableInfo tableInfo : sModelInfo.getTableInfos())
        {
            try
            {
                Class<? extends Model> type = tableInfo.getType();
                if (!isDoNotGenerate(type))
                {
                    sFillers.put(type, instantiateFiller(type));
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException(e);
            }
        }


    }

    private static boolean isDoNotGenerate(Class<?> clazz)
    {
        if (clazz.isAnnotationPresent(DoNotGenerate.class))
        {
            return true;
        }
        if (clazz.getSuperclass() != null)
        {
            return isDoNotGenerate(clazz.getSuperclass());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static ModelFiller instantiateFiller(Class<? extends Model> type) throws IllegalAccessException, InstantiationException
    {
        ModelFiller modelFiller = sFillers.get(type);
        if (modelFiller == null)
        {
            String fillerClassName = type.getName() + ModelFiller.SUFFIX;
            try
            {
                Class<? extends ModelFiller> fillerType = (Class<? extends ModelFiller>) Class.forName(fillerClassName);
                modelFiller = fillerType.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                modelFiller = new EmptyModelFiller();
            }
            if (type.getSuperclass() != null && ReflectionUtils.isModel(type.getSuperclass()))
            {
                modelFiller.superModelFiller = instantiateFiller((Class<? extends Model>) type.getSuperclass());
            }
        }
        return modelFiller;
    }
}
