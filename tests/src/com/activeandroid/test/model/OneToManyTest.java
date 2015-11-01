package com.activeandroid.test.model;

import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.model.OneToManyRelation;
import com.activeandroid.test.MockModel;

import java.util.ArrayList;
import java.util.List;

public class OneToManyTest extends ModelTestCase
{

    public static class MockOneToManyRelation extends OneToManyRelation<MockModel, Model>
    {
        public MockOneToManyRelation()
        {
            super();
        }
    }

    public void testOneToManyRelation()
    {

        MockModel mockModelsHolder = new MockModel();
        mockModelsHolder.save("test");

        List<Model> mockModels = new ArrayList<Model>();
        for (int i = 0; i < 5; ++i)
        {
            MockModel mockModel = new MockModel();
            mockModel.save("test");
            mockModels.add(mockModel);
        }

        OneToManyRelation.setRelations("test", MockOneToManyRelation.class, mockModelsHolder, mockModels);

        mockModels = OneToManyRelation.getRelations("test", MockOneToManyRelation.class, mockModelsHolder);
        assertTrue(mockModels.size() == 5);
    }
}
