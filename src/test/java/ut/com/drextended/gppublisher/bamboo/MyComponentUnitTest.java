package ut.com.drextended.gppublisher.bamboo;

import org.junit.Test;
import com.drextended.gppublisher.bamboo.api.MyPluginComponent;
import com.drextended.gppublisher.bamboo.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}