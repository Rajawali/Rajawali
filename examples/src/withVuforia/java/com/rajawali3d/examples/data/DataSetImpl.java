package com.rajawali3d.examples.data;

import org.rajawali3d.examples.R;
import org.rajawali3d.examples.data.Category;
import org.rajawali3d.examples.data.Example;
import com.rajawali3d.examples.examples.vr_ar.RajawaliVRExampleActivity;
import com.rajawali3d.examples.examples.vr_ar.VuforiaExampleFragment;

import java.util.LinkedList;
import java.util.List;

public final class DataSetImpl {
    
    private static volatile DataSetImpl instance;
    
    private final List<Category> categories;
    
    DataSetImpl() {
        categories = new LinkedList<>();
        categories.add(new Category(R.string.category_vr_ar, new Example[]{
                new Example(R.string.example_vr_ar_cardboard_integration, RajawaliVRExampleActivity.class),
                new Example(R.string.example_vr_ar_vuforia_integration, VuforiaExampleFragment.class),
        }));
    }
    
    public static synchronized DataSetImpl getInstance() {
        if (instance == null) {
            synchronized (DataSetImpl.class) {
                if (instance == null) {
                    instance = new DataSetImpl();
                }
            }
        }
        
        return instance;
    }

    public List<Category> getCategories() {
        return categories;
    }
    
}
