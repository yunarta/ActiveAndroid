package com.activeandroid.test.model;

import com.activeandroid.sebbia.Model;
import com.activeandroid.sebbia.annotation.Column;
import com.activeandroid.sebbia.annotation.DoNotGenerate;
import com.activeandroid.sebbia.query.Select;

import java.util.ArrayList;
import java.util.List;

public class DoNotGenerateTest extends ModelTestCase
{

    @DoNotGenerate
    public static class DoNotGenerateModel extends Model
    {

        @Column(name = "value")
        private String value;
    }

    public void testNoGenerate()
    {
        List<DoNotGenerateModel> impls = new ArrayList<DoNotGenerateModel>();
        for (int i = 0; i < 100; ++i)
        {
            DoNotGenerateModel model = new DoNotGenerateModel();
            model.value = Integer.toString(i);
            impls.add(model);
        }

        Model.saveMultiple("test", impls);

        impls = new Select().from(DoNotGenerateModel.class).execute("test");
        assertEquals(100, impls.size());
        for (int i = 0; i < impls.size(); ++i)
        {
            DoNotGenerateModel impl = impls.get(i);
            assertNotNull(impl);
            assertNotNull(impl.value);
            assertTrue(impl.value.equalsIgnoreCase(Integer.toString(i)));
        }
    }
}
