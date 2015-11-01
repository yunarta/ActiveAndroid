package com.activeandroid.sebbia.model;

import com.activeandroid.sebbia.Cache;
import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.TableInfo;
import com.activeandroid.sebbia.annotation.Column;
import com.activeandroid.sebbia.annotation.DoNotGenerate;
import com.activeandroid.sebbia.query.Delete;
import com.activeandroid.sebbia.util.Log;
import com.activeandroid.sebbia.util.SQLiteUtils;

import java.util.ArrayList;
import java.util.List;

@DoNotGenerate
public abstract class ManyToManyRelation<T1 extends Model, T2 extends Model> extends Model
{

    @Column(name = "entity1")
    private T1 entity1;
    @Column(name = "entity2")
    private T2 entity2;

    public static <T1 extends Model, T2 extends Model> void setRelationsFront(String database, Class<? extends ManyToManyRelation<T1, T2>> relation, T1 entity1, List<T2> entities2)
    {
        if (entity1.getId() == null)
        {
            throw new IllegalArgumentException(entity1.getClass().getSimpleName() + " is not saved to database yet, aborting");
        }
        for (T2 entity2 : entities2)
        {
            if (entity2.getId() == null)
            {
                throw new IllegalArgumentException(entity2.getClass().getSimpleName() + " is not saved to database yet, aborting");
            }
        }

        new Delete().from(relation).where("entity1 = ?", entity1.getId()).execute(database);
        try
        {
            ArrayList<ManyToManyRelation<T1, T2>> connections = new ArrayList<ManyToManyRelation<T1, T2>>();
            for (T2 entity2 : entities2)
            {
                ManyToManyRelation<T1, T2> connection = relation.newInstance();
                connection.entity1 = entity1;
                connection.entity2 = entity2;
                connections.add(connection);
            }
            saveMultiple(database, connections);
        }
        catch (Exception e)
        {
            Log.e("Cannot create instance of class " + relation.getSimpleName());
            throw new RuntimeException(e);
        }
    }

    public static <T1 extends Model, T2 extends Model> void setRelationsReverse(String database, Class<? extends ManyToManyRelation<T1, T2>> relation, T2 entity2, List<T1> entities1)
    {
        if (entity2.getId() == null)
        {
            throw new IllegalArgumentException(entity2.getClass().getSimpleName() + " is not saved to database yet, aborting");
        }
        for (T1 entity1 : entities1)
        {
            if (entity1.getId() == null)
            {
                throw new IllegalArgumentException(entity1.getClass().getSimpleName() + " is not saved to database yet, aborting");
            }
        }

        new Delete().from(relation).where("entity2 = ?", entity2.getId()).execute(database);
        try
        {
            ArrayList<ManyToManyRelation<T1, T2>> connections = new ArrayList<ManyToManyRelation<T1, T2>>();
            for (T1 entity1 : entities1)
            {
                ManyToManyRelation<T1, T2> connection = relation.newInstance();
                connection.entity1 = entity1;
                connection.entity2 = entity2;
                connections.add(connection);
            }
            saveMultiple(database, connections);
        }
        catch (Exception e)
        {
            Log.e("Cannot create instance of class " + relation.getSimpleName());
            throw new RuntimeException(e);
        }
    }

    public static <T1 extends Model, T2 extends Model> List<T2> getRelationsFront(String database, Class<? extends ManyToManyRelation<T1, T2>> relation, T1 entity)
    {
        if (entity.getId() == null)
        {
            throw new IllegalArgumentException(entity.getClass().getSimpleName() + " is not saved to database yet, aborting");
        }

        Class<? extends Model> entity1Class;
        Class<? extends Model> entity2Class;
        TableInfo              entity1TableInfo;
        TableInfo              entity2TableInfo;
        TableInfo              crossTableInfo;
        try
        {
            ManyToManyRelation<T1, T2> instance = relation.newInstance();
            entity1Class = instance.getEntity1Class();
            entity2Class = instance.getEntity2Class();
            entity1TableInfo = Cache.getTableInfo(entity1Class);
            entity2TableInfo = Cache.getTableInfo(entity2Class);
            crossTableInfo = Cache.getTableInfo(relation);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append(entity2TableInfo.getTableName());
        queryBuilder.append(".* FROM (");
        queryBuilder.append(entity1TableInfo.getTableName());
        queryBuilder.append(" JOIN ");
        queryBuilder.append(crossTableInfo.getTableName());
        queryBuilder.append(" ON ");
        queryBuilder.append(entity1TableInfo.getTableName());
        queryBuilder.append(".id == ");
        queryBuilder.append(crossTableInfo.getTableName());
        queryBuilder.append(".entity1) JOIN ");
        queryBuilder.append(entity2TableInfo.getTableName());
        queryBuilder.append(" ON ");
        queryBuilder.append(entity2TableInfo.getTableName());
        queryBuilder.append(".id == ");
        queryBuilder.append(crossTableInfo.getTableName());
        queryBuilder.append(".entity2 WHERE ");
        queryBuilder.append(entity1TableInfo.getTableName());
        queryBuilder.append(".id == ?");

        return SQLiteUtils.rawQuery(database, entity2Class, queryBuilder.toString(), new String[]{entity.getId().toString()});
    }

    public static <T1 extends Model, T2 extends Model> List<T1> getRelationsReverse(String database, Class<? extends ManyToManyRelation<T1, T2>> relation, T2 entity)
    {
        if (entity.getId() == null)
        {
            throw new IllegalArgumentException(entity.getClass().getSimpleName() + " is not saved to database yet, aborting");
        }

        Class<? extends Model> entity1Class;
        Class<? extends Model> entity2Class;
        TableInfo              entity1TableInfo;
        TableInfo              entity2TableInfo;
        TableInfo              crossTableInfo;
        try
        {
            ManyToManyRelation<T1, T2> instance = relation.newInstance();
            entity1Class = instance.getEntity1Class();
            entity2Class = instance.getEntity2Class();
            entity1TableInfo = Cache.getTableInfo(entity1Class);
            entity2TableInfo = Cache.getTableInfo(entity2Class);
            crossTableInfo = Cache.getTableInfo(relation);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        queryBuilder.append(entity1TableInfo.getTableName());
        queryBuilder.append(".* FROM (");
        queryBuilder.append(entity2TableInfo.getTableName());
        queryBuilder.append(" JOIN ");
        queryBuilder.append(crossTableInfo.getTableName());
        queryBuilder.append(" ON ");
        queryBuilder.append(entity2TableInfo.getTableName());
        queryBuilder.append(".id == ");
        queryBuilder.append(crossTableInfo.getTableName());
        queryBuilder.append(".entity2) JOIN ");
        queryBuilder.append(entity1TableInfo.getTableName());
        queryBuilder.append(" ON ");
        queryBuilder.append(entity1TableInfo.getTableName());
        queryBuilder.append(".id == ");
        queryBuilder.append(crossTableInfo.getTableName());
        queryBuilder.append(".entity1 WHERE ");
        queryBuilder.append(entity2TableInfo.getTableName());
        queryBuilder.append(".id == ?");

        return SQLiteUtils.rawQuery(database, entity1Class, queryBuilder.toString(), new String[]{entity.getId().toString()});
    }

    public ManyToManyRelation()
    {
        super();
    }

    public abstract Class<T1> getEntity1Class();

    public abstract Class<T2> getEntity2Class();


}
