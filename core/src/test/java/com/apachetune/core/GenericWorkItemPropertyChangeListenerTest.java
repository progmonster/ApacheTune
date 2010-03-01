package com.apachetune.core;

import org.testng.annotations.*;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.FileAssert.fail;
import com.apachetune.core.utils.*;
import com.apachetune.core.impl.*;

import java.beans.*;

/**
 * FIXDOC
 *
 * @author <a href="mailto:aleksey.katorgin@trustverse.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
@Test
public class GenericWorkItemPropertyChangeListenerTest {
    @Test
    public void testPropertyChangeEvent() {
        RootWorkItem rootWorkItem = new RootWorkItemImpl();

        final PropertyChangeTestWorkItem workItem = new PropertyChangeTestWorkItem();

        rootWorkItem.addChildWorkItem(workItem);

        final BooleanValue isRaised = new BooleanValue();

        workItem.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                isRaised.value = true;

                assertEquals(evt.getSource(), workItem);
                assertEquals(evt.getPropertyName(), "fakeProperty");
                assertEquals(evt.getOldValue(), "fakeOldValue");
                assertEquals(evt.getNewValue(), "fakeNewValue");
            }
        });

        workItem.initialize();
        workItem.fireEvent();

        assertTrue(isRaised.value);
    }

    @Test
    public void testRemoveListener() {
        RootWorkItem rootWorkItem = new RootWorkItemImpl();

        final PropertyChangeTestWorkItem workItem = new PropertyChangeTestWorkItem();

        rootWorkItem.addChildWorkItem(workItem);

        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fail();
            }
        };

        workItem.addPropertyChangeListener(listener);
        workItem.removePropertyChangeListener(listener);

        workItem.initialize();
        workItem.fireEvent();
    }

    @Test
    public void testRemoveAllListeners() {
        RootWorkItem rootWorkItem = new RootWorkItemImpl();

        final PropertyChangeTestWorkItem workItem = new PropertyChangeTestWorkItem();

        rootWorkItem.addChildWorkItem(workItem);

        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fail();
            }
        };

        workItem.addPropertyChangeListener(listener);
        workItem.removeAllPropertyChangeListeners();

        workItem.initialize();
        workItem.fireEvent();
    }
}

class PropertyChangeTestWorkItem extends SimpleWorkItem {
    public PropertyChangeTestWorkItem() {
        super("TEST_WORK_ITEM");
    }

    public void fireEvent() {
        firePropertyChangeEvent("fakeProperty", "fakeOldValue", "fakeNewValue");
    }
}