package com.pryv.util;

import com.pryv.model.Attachment;
import com.pryv.model.Event;
import com.pryv.model.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUtils {

    private static final Double TIME_DELTA = 10.0;

    /**
     * Verifies assertions for an Event/'s parameters
     *
     * @param expected
     * @param actual
     */
    public static void checkEvent(Event expected, Event actual) {
        if (expected != actual) {
            if (expected.getId() != null) {
                assertEquals(expected.getId(), actual.getId());
            }
            if (expected.getStreamId() != null) {
                assertEquals(expected.getStreamId(), actual.getStreamId());
            }
            if (expected.getTime() != null) {
                assertEquals(expected.getTime(), actual.getTime(), TIME_DELTA);
            }
            if (expected.getType() != null) {
                assertEquals(expected.getType(), actual.getType());
            }
            if (expected.getCreated() != null) {
                assertEquals(expected.getCreated(), actual.getCreated(), TIME_DELTA);
            }
            if (expected.getCreatedBy() != null) {
                assertEquals(expected.getCreatedBy(), actual.getCreatedBy());
            }
            if (expected.getModified() != null) {
                assertEquals(expected.getModified(), actual.getModified(), TIME_DELTA);
            }
            if (expected.getModifiedBy() != null) {
                assertEquals(expected.getModifiedBy(), actual.getModifiedBy());
            }
            if (expected.getDuration() != null) {
                assertEquals(expected.getDuration(), actual.getDuration());
            }
            if (expected.getContent() != null) {
                assertEquals(expected.getContent(), actual.getContent());
            }
            if (expected.getDescription() != null) {
                assertEquals(expected.getDescription(), actual.getDescription());
            }

            assertEquals(expected.isTrashed(), actual.isTrashed());

            if (expected.getTags() != null) {
                assertNotNull(actual.getTags());
                boolean found = false;
                for (String expectedTag : expected.getTags()) {
                    found = false;
                    for (String receivedTag : actual.getTags()) {
                        if (expectedTag.equals(receivedTag)) {
                            found = true;
                        }
                    }
                    assertTrue(found);
                }
            }

            if (expected.getAttachments() != null) {
                assertNotNull(actual.getAttachments());
                assertEquals(expected.getAttachments().size(), actual.getAttachments().size());
                for (Attachment testedAttachment : actual.getAttachments()) {
                    boolean attachmentsMatch = false;
                    for (Attachment trueAttachment : expected.getAttachments()) {
                        if (testedAttachment.getId().equals(trueAttachment.getId())) {
                            attachmentsMatch = true;
                            assertEquals(trueAttachment.getFileName(), testedAttachment.getFileName());
                            assertEquals(trueAttachment.getReadToken(), testedAttachment.getReadToken());
                            assertEquals(trueAttachment.getType(), testedAttachment.getType());
                            assertTrue(trueAttachment.getSize() == testedAttachment.getSize());
                        }
                    }
                    assertTrue(attachmentsMatch);
                }
            }

            if (expected.getClientData() != null) {
                assertEquals(expected.formatClientDataAsString(), actual.formatClientDataAsString());
            }
        }
    }

    /**
     * Verifies assertions between the expected Stream and the received
     *
     * @param expected
     * @param received
     */
    public static void checkStream(Stream expected, Stream received) {
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
                assertEquals(expected.getCreated(), received.getCreated(), TIME_DELTA);
            }
            if (expected.getCreatedBy() != null) {
                assertEquals(expected.getCreatedBy(), received.getCreatedBy());
            }
            if (expected.getModified() != null) {
                assertEquals(expected.getModified(), received.getModified(), TIME_DELTA);
            }
            if (expected.getModifiedBy() != null) {
                assertEquals(expected.getModifiedBy(), received.getModifiedBy());
            }

            assertEquals(expected.isTrashed(), received.isTrashed());

            if (expected.isSingleActivity() != null) {
                assertEquals(expected.isSingleActivity(), received.isSingleActivity());
            }

            // TODO add children and clientData comparison
        }

    }
}
