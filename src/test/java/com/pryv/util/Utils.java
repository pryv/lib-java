package com.pryv.util;

import com.pryv.model.Event;
import com.pryv.model.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by ik on 4/14/16.
 */
public class Utils {

    /**
     * Verifies assertions for an Event/'s parameters
     *
     * @param expected
     * @param received
     */
    public void checkEvent(Event expected, Event received) {
        if (expected != received) {
            if (expected.getClientId() != null) {
                assertEquals(expected.getClientId(), received.getClientId());
            }
            if (expected.getId() != null) {
                assertEquals(expected.getId(), received.getId());
            }
            if (expected.getStreamId() != null) {
                assertEquals(expected.getStreamId(), received.getStreamId());
            }
            if (expected.getTime() != null) {
                assertEquals(expected.getTime(), received.getTime());
            }
            if (expected.getType() != null) {
                assertEquals(expected.getType(), received.getType());
            }
            if (expected.getCreated() != null) {
                assertEquals(expected.getCreated(), received.getCreated());
            }
            if (expected.getCreatedBy() != null) {
                assertEquals(expected.getCreatedBy(), received.getCreatedBy());
            }
            if (expected.getModified() != null) {
                assertEquals(expected.getModified(), received.getModified());
            }
            if (expected.getModifiedBy() != null) {
                assertEquals(expected.getModifiedBy(), received.getModifiedBy());
            }
            if (expected.getDuration() != null) {
                assertEquals(expected.getDuration(), received.getDuration());
            }
            if (expected.getContent() != null) {
                assertEquals(expected.getContent(), received.getContent());
            }
            if (expected.getDescription() != null) {
                assertEquals(expected.getDescription(), received.getDescription());
            }
            if (expected.isTrashed() != null) {
                assertEquals(expected.isTrashed(), received.isTrashed());
            }
            if (expected.getTags() != null) {
                assertNotNull(received.getTags());
                boolean found = false;
                for (String expectedTag : expected.getTags()) {
                    found = false;
                    for (String receivedTag : received.getTags()) {
                        assertEquals(expectedTag, receivedTag);
                    }
                    assertTrue(found);
                }
            }
            if (expected.getReferences() != null) {
                assertNotNull(received.getReferences());
                boolean found = false;
                for (String expectedReference : expected.getReferences()) {
                    found = false;
                    for (String receivedReference : received.getReferences()) {
                        assertEquals(expectedReference, receivedReference);
                    }
                    assertTrue(found);
                }
            }

            // TODO compare Attachments
            // TODO compare clientData
        }
    }

    /**
     * Verifies assertions between the expected Stream and the received
     *
     * @param expected
     * @param received
     */
    public void checkStream(Stream expected, Stream received) {
        if (expected != received) {
            if (expected.getId() != null) {
                assertEquals(expected.getId(), received.getId());
            }
            if (expected.getName() != null) {
                assertEquals(expected.getName(), received.getName());
            }
            if (expected.getParentId() != null) {
                assertEquals(expected.getParentId(), received.getParentId());
            }
            if (expected.getCreated() != null) {
                assertEquals(expected.getCreated(), received.getCreated());
            }
            if (expected.getCreatedBy() != null) {
                assertEquals(expected.getCreatedBy(), received.getCreatedBy());
            }
            if (expected.getModified() != null) {
                assertEquals(expected.getModified(), received.getModified());
            }
            if (expected.getModifiedBy() != null) {
                assertEquals(expected.getModifiedBy(), received.getModifiedBy());
            }
            if (expected.isTrashed() != null) {
                assertEquals(expected.isTrashed(), received.isTrashed());
            }
            if (expected.isSingleActivity() != null) {
                assertEquals(expected.isSingleActivity(), received.isSingleActivity());
            }

            // TODO add children and clientData comparison
        }

    }
}
