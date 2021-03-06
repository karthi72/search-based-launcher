package com.vackosar.searchbasedlauncher.entity;

import android.content.Context;
import android.content.SharedPreferences;

import com.vackosar.searchbasedlauncher.FieldAccessor;
import com.vackosar.searchbasedlauncher.boundary.AutostartSelector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingletonPersisterTest {

    public static final String CONTEXT_FIELDNAME = "context";
    public static final String AUTOSTART_FIELDNAME = "autostart";
    @Mock private Context context;
    @Mock private SharedPreferences sharedPreferences;
    @Mock private SharedPreferences.Editor editor;
    @InjectMocks private SingletonPersister<AutostartSelector> autostartButtonSingletonPersister;

    private Map<String, String> preferences;
    private String preferenceName;
    private AutostartSelector autostartSelector;

    @Before
    public void init() throws IllegalAccessException {
        when(context.getSharedPreferences(anyString(), anyInt())).then(
                new Answer<SharedPreferences>() {

                    @Override
                    public SharedPreferences answer(InvocationOnMock invocation) throws Throwable {
                        preferenceName = (String) invocation.getArguments()[0];
                        return sharedPreferences;
                    }
                }
        );

                when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).then(new Answer<SharedPreferences.Editor>() {
            @Override
            public SharedPreferences.Editor answer(InvocationOnMock invocation) throws Throwable {
                final String key = (String) invocation.getArguments()[0];
                final String value = (String) invocation.getArguments()[1];
                preferences.put(key, value);
                return editor;
            }

            ;
        });
        when(sharedPreferences.getString(anyString(), anyString())).then(
                new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        String key = (String) invocation.getArguments()[0];
                        return preferences.get(key);
                    }
                });
        preferences = new HashMap<>();
        initAutostartButton();
        autostartButtonSingletonPersister = new SingletonPersister();
        FieldAccessor.setField(autostartButtonSingletonPersister, CONTEXT_FIELDNAME, context);
    }

    private void initAutostartButton() {
        autostartSelector = new AutostartSelector();
        FieldAccessor.setField(autostartSelector, AUTOSTART_FIELDNAME, true);
    }

    @Test
    public void save() {
        FieldAccessor.setField(autostartSelector, AUTOSTART_FIELDNAME, true);
        autostartButtonSingletonPersister.save(autostartSelector);
        Assert.assertEquals(1, preferences.entrySet().size());
        Assert.assertEquals("{\"autostart\":true}", preferences.get("json"));
        Assert.assertEquals(AutostartSelector.class.getCanonicalName(), preferenceName);
    }

    @Test
    public void load() {
        autostartButtonSingletonPersister.load(autostartSelector);
        Assert.assertEquals(true, FieldAccessor.getField(autostartSelector, AUTOSTART_FIELDNAME));
    }

}
