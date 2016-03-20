package com.rajawali3d.examples.data;

import org.rajawali3d.examples.data.Category;

import java.util.LinkedList;
import java.util.List;

public final class DataSetImpl {
    
    private static volatile DataSetImpl instance;
    
    private final List<Category> categories;
    
    DataSetImpl() {
        categories = new LinkedList<>();
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
