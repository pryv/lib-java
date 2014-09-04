package com.pryv.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.Filter;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.JsonConverter;

/**
 * generates dummy data for testing
 *
 * @author ik
 *
 */
public class DummyData {

  private static final String EVENT_CLIENT_ID = "testClientId";
  private static final String EVENT_STREAM_CLIENT_ID = "testStreamClientId";
  private static final String EVENT_ID = "testID";
  private static final String EVENT_STREAM_ID = "testStreamID";
  private static final Double EVENT_TIME = new Double(10000);
  private static final Double EVENT_DURATION = new Double(20); // opt
  private static final String EVENT_TYPE = "testType";
  private static final String EVENT_CONTENT = "testContent its a string"; // opt
  private static Set<String> eventTags;
  private static final String EVENT_TAGTEST = "test";
  private static final String EVENT_TAGBASIC = "basic test";
  private static Set<String> eventRefs;
  private static final String EVENT_REF_FIRST = "refTest1";
  private static final String EVENT_REF_SECOND = "refTest2";
  private static final String EVENT_DESCRIPTION = "the test description";
  private static Set<Attachment> eventAttachments;
  private static Map<String, Object> eventClientData;
  private static final String EVENT_CLIENTKEY = "color";
  private static final String EVENT_CLIENTVALUE = "value";
  private static final Boolean EVENT_TRASHED = false;
  private static final Double EVENT_CREATED = new Double(10);
  private static final String EVENT_CREATEDBY = "event creator";
  private static final Double EVENT_MODIFIED = new Double(50);
  private static final String EVENT_MODIFIED_BY = "event modifier";
  private static final String EVENT_ATTACH_ID = "abc";
  private static final String EVENT_TEMP_REF_ID = "temp ref id";

  private static final String STREAM_CLIENT_ID = "testStreamClientId";
  private static final String STREAM_PARENT_CLIENT_ID = STREAM_CLIENT_ID;
  private static final String STREAM_ID = "testStreamId";
  private static final String STREAM_NAME = "testStreamName";
  private static final String STREAM_PARENT_ID = STREAM_ID;
  private static final Boolean STREAM_SINGLE_ACTIVITY = true;
  private static Map<String, Object> streamClientData;
  private static final String STREAM_CLIENT_KEY = "testKey";
  private static final String STREAM_CLIENT_VALUE = "testValue";
  private static List<Stream> streamChildren;
  private static final Boolean STREAM_TRASHED = true;
  private static final Double STREAM_CREATED = new Double(1000);
  private static final String STREAM_CREATED_BY = "Bob";
  private static final Double STREAM_MODIFIED = new Double(1500);
  private static final String STREAM_MODIFIED_BY = "Bill";
  private static final String STREAM_CHILD_CLIENT_ID = "testChildClientId";
  private static final String STREAM_CHILD_ID = "childid";
  private static final String STREAM_CHILD_NAME = "childname";
  private static final Boolean STREAM_CHILD_TRASHED = false;
  private static final Double STREAM_CHILD_CREATED = new Double(200);
  private static final String STREAM_CHILD_CREATED_BY = "creator of child";
  private static final Double STREAM_CHILD_MODIFIED = new Double(300);
  private static final String STREAM_CHILD_MODIFIED_BY = "modifier of child";

  private static final String ATTACHMENT_ID_FIRST = "firstAttachId";
  private static final String ATTACHMENT_ID_SECOND = "secondAttachId";
  private static final String ATTACHMENT_FILENAME_FIRST = "firstAttachFilename";
  private static final String ATTACHMENT_FILENAME_SECOND = "secondAttachFilename";
  private static final String ATTACHMENT_TYPE_FIRST = "firstAttachType";
  private static final String ATTACHMENT_TYPE_SECOND = "secondAttachType";
  private static final int ATTACHMENT_NUMBER_FIRST = 10;
  private static final int ATTACHMENT_NUMBER_SECOND = 20;
  private static final String ATTACHMENT_READ_TOKEN_FIRST = "firstAttachReadToken";
  private static final String ATTACHMENT_READ_TOKEN_SECOND = "secondAttachReadToken";

  public static Stream generateFullStream() {
    streamClientData = new HashMap<String, Object>();
    streamClientData.put(STREAM_CLIENT_KEY, STREAM_CLIENT_VALUE);
    Stream streamChild =
      new Stream(STREAM_CHILD_CLIENT_ID, STREAM_CLIENT_ID, STREAM_CHILD_ID, STREAM_CHILD_NAME,
        STREAM_ID, null, null, null, STREAM_CHILD_TRASHED, STREAM_CHILD_CREATED,
        STREAM_CHILD_CREATED_BY, STREAM_CHILD_MODIFIED, STREAM_CHILD_MODIFIED_BY);
    Stream testStream =
      new Stream(STREAM_CLIENT_ID, null, STREAM_ID, STREAM_NAME, null, STREAM_SINGLE_ACTIVITY,
        streamClientData, null, STREAM_TRASHED, STREAM_CREATED, STREAM_CREATED_BY, STREAM_MODIFIED,
        STREAM_MODIFIED_BY);
    testStream.addChildStream(streamChild);
    return testStream;
  }

  public static Set<Attachment> generateAttachments() {
    Set<Attachment> attachments = new HashSet<Attachment>();
    attachments.add(new Attachment(ATTACHMENT_ID_FIRST, ATTACHMENT_FILENAME_FIRST,
      ATTACHMENT_TYPE_FIRST, ATTACHMENT_NUMBER_FIRST, ATTACHMENT_READ_TOKEN_FIRST));
    attachments.add(new Attachment(ATTACHMENT_ID_SECOND, ATTACHMENT_FILENAME_SECOND,
      ATTACHMENT_TYPE_SECOND, ATTACHMENT_NUMBER_SECOND, ATTACHMENT_READ_TOKEN_SECOND));
    return attachments;
  }

  public static String generateJsonStream() {
    try {
      return JsonConverter.toJson(generateFullStream());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Event generateFullEvent() {
    eventTags = new HashSet<String>();
    eventTags.add(EVENT_TAGTEST);
    eventTags.add(EVENT_TAGBASIC);
    eventRefs = new HashSet<String>();
    eventRefs.add(EVENT_REF_FIRST);
    eventRefs.add(EVENT_REF_SECOND);
    eventAttachments = generateAttachments();
    eventClientData = new HashMap<String, Object>();
    eventClientData.put(EVENT_CLIENTKEY, EVENT_CLIENTVALUE);
    return new Event(EVENT_CLIENT_ID, EVENT_STREAM_CLIENT_ID, EVENT_ID, EVENT_STREAM_ID,
      EVENT_TIME, EVENT_DURATION, EVENT_TYPE, EVENT_CONTENT, eventTags, eventRefs,
      EVENT_DESCRIPTION, eventAttachments, eventClientData, EVENT_TRASHED, EVENT_CREATED,
      EVENT_CREATEDBY, EVENT_MODIFIED, EVENT_MODIFIED_BY, EVENT_TEMP_REF_ID);

  }

  public static String generateJsonEvent() {
    try {
      return JsonConverter.toJson(generateFullEvent());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Filter generateFullFilter() {
    Set<String> dummyStringSet = new HashSet<String>();
    dummyStringSet.add("dummyValue1");
    dummyStringSet.add("dummyValue2");
    return new Filter(new Double(0), new Double(1000), dummyStringSet, dummyStringSet,
      dummyStringSet, false, true, new Integer(0), new Integer(10), Filter.State.ALL, new Double(
        500));
  }

  public static String getId() {
    return EVENT_ID;
  }

  public static String getStreamid() {
    return EVENT_STREAM_ID;
  }

  public static Double getTime() {
    return EVENT_TIME;
  }

  public static Double getDuration() {
    return EVENT_DURATION;
  }

  public static String getType() {
    return EVENT_TYPE;
  }

  public static String getContent() {
    return EVENT_CONTENT;
  }

  public static Set<String> getTags() {
    return eventTags;
  }

  public static String getTagTest() {
    return EVENT_TAGTEST;
  }

  public static String getTagbasic() {
    return EVENT_TAGBASIC;
  }

  public static Set<String> getRefs() {
    return eventRefs;
  }

  public static String getRef() {
    return EVENT_REF_FIRST;
  }

  public static String getDescription() {
    return EVENT_DESCRIPTION;
  }

  public static Set<Attachment> getAttachments() {
    return eventAttachments;
  }

  public static Map<String, Object> getClientdata() {
    return eventClientData;
  }

  public static String getClientkey() {
    return EVENT_CLIENTKEY;
  }

  public static String getClientvalue() {
    return EVENT_CLIENTVALUE;
  }

  public static Boolean getTrashed() {
    return EVENT_TRASHED;
  }

  public static Double getCreated() {
    return EVENT_CREATED;
  }

  public static String getCreatedby() {
    return EVENT_CREATEDBY;
  }

  public static Double getModified() {
    return EVENT_MODIFIED;
  }

  public static String getModifiedBy() {
    return EVENT_MODIFIED_BY;
  }

  public static String getAttachId() {
    return EVENT_ATTACH_ID;
  }

  public static String getTempRefId() {
    return EVENT_TEMP_REF_ID;
  }

  public static String getTagtest() {
    return EVENT_TAGTEST;
  }

  public static String getStreamId() {
    return STREAM_ID;
  }

  public static String getStreamName() {
    return STREAM_NAME;
  }

  public static String getStreamParentId() {
    return STREAM_PARENT_ID;
  }

  public static Boolean getStreamSingleActivity() {
    return STREAM_SINGLE_ACTIVITY;
  }

  public static Map<String, Object> getStreamClientData() {
    return streamClientData;
  }

  public static String getStreamClientKey() {
    return STREAM_CLIENT_KEY;
  }

  public static String getStreamClientValue() {
    return STREAM_CLIENT_VALUE;
  }

  public static List<Stream> getStreamChildren() {
    return streamChildren;
  }

  public static Boolean getStreamTrashed() {
    return STREAM_TRASHED;
  }

  public static Double getStreamCreated() {
    return STREAM_CREATED;
  }

  public static String getStreamCreatedBy() {
    return STREAM_CREATED_BY;
  }

  public static Double getStreamModified() {
    return STREAM_MODIFIED;
  }

  public static String getStreamModifiedBy() {
    return STREAM_MODIFIED_BY;
  }

  public static String getStreamChildId() {
    return STREAM_CHILD_ID;
  }

  public static String getEventId() {
    return EVENT_ID;
  }

  public static String getEventStreamid() {
    return EVENT_STREAM_ID;
  }

  public static Double getEventTime() {
    return EVENT_TIME;
  }

  public static Double getEventDuration() {
    return EVENT_DURATION;
  }

  public static String getEventType() {
    return EVENT_TYPE;
  }

  public static String getEventContent() {
    return EVENT_CONTENT;
  }

  public static Set<String> getEventTags() {
    return eventTags;
  }

  public static String getEventTagtest() {
    return EVENT_TAGTEST;
  }

  public static String getEventTagbasic() {
    return EVENT_TAGBASIC;
  }

  public static Set<String> getEventRefs() {
    return eventRefs;
  }

  public static String getEventRef() {
    return EVENT_REF_FIRST;
  }

  public static String getEventDescription() {
    return EVENT_DESCRIPTION;
  }

  public static Set<Attachment> getEventAttachments() {
    return eventAttachments;
  }

  public static Map<String, Object> getEventClientData() {
    return eventClientData;
  }

  public static String getEventClientkey() {
    return EVENT_CLIENTKEY;
  }

  public static String getEventClientvalue() {
    return EVENT_CLIENTVALUE;
  }

  public static Boolean getEventTrashed() {
    return EVENT_TRASHED;
  }

  public static Double getEventCreated() {
    return EVENT_CREATED;
  }

  public static String getEventCreatedby() {
    return EVENT_CREATEDBY;
  }

  public static Double getEventModified() {
    return EVENT_MODIFIED;
  }

  public static String getEventModifiedBy() {
    return EVENT_MODIFIED_BY;
  }

  public static String getEventAttachId() {
    return EVENT_ATTACH_ID;
  }

  public static String getEventTempRefId() {
    return EVENT_TEMP_REF_ID;
  }

  public static String getStreamChildName() {
    return STREAM_CHILD_NAME;
  }

  public static Boolean getStreamChildTrashed() {
    return STREAM_CHILD_TRASHED;
  }

  public static Double getStreamChildCreated() {
    return STREAM_CHILD_CREATED;
  }

  public static String getStreamChildCreatedBy() {
    return STREAM_CHILD_CREATED_BY;
  }

  public static Double getStreamChildModified() {
    return STREAM_CHILD_MODIFIED;
  }

  public static String getStreamChildModifiedBy() {
    return STREAM_CHILD_MODIFIED_BY;
  }

  public static String getAttachmentIdFirst() {
    return ATTACHMENT_ID_FIRST;
  }

  public static String getAttachmentIdSecond() {
    return ATTACHMENT_ID_SECOND;
  }

  public static String getAttachmentFilenameFirst() {
    return ATTACHMENT_FILENAME_FIRST;
  }

  public static String getAttachmentFilenameSecond() {
    return ATTACHMENT_FILENAME_SECOND;
  }

  public static String getAttachmentTypeFirst() {
    return ATTACHMENT_TYPE_FIRST;
  }

  public static String getAttachmentTypeSecond() {
    return ATTACHMENT_TYPE_SECOND;
  }

  public static int getAttachmentNumberFirst() {
    return ATTACHMENT_NUMBER_FIRST;
  }

  public static int getAttachmentNumberSecond() {
    return ATTACHMENT_NUMBER_SECOND;
  }

  public static String getAttachmentReadTokenFirst() {
    return ATTACHMENT_READ_TOKEN_FIRST;
  }

  public static String getAttachmentReadTokenSecond() {
    return ATTACHMENT_READ_TOKEN_SECOND;
  }

}
