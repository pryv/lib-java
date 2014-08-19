package com.pryv.unit;

import java.util.ArrayList;
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

  private static final String EVENT_ID = "testID";
  private static final String EVENT_STREAMID = "testStreamID";
  private static final Long EVENT_TIME = new Long(10000);
  private static final Long EVENT_DURATION = new Long(20); // opt
  private static final String EVENT_TYPE = "testType";
  private static final String EVENT_CONTENT = "testContent its a string"; // opt
  private static Set<String> eventTags;
  private static final String EVENT_TAGTEST = "test";
  private static final String EVENT_TAGBASIC = "basic test";
  private static Set<String> eventRefs;
  private static final String EVENT_REF = "refTest";
  private static final String EVENT_DESCRIPTION = "the test description";
  private static Set<Attachment> eventAttachments;
  private static Map<String, Object> eventClientData;
  private static final String EVENT_CLIENTKEY = "color";
  private static final String EVENT_CLIENTVALUE = "value";
  private static final Boolean EVENT_TRASHED = false;
  private static final Long EVENT_CREATED = new Long(10);
  private static final String EVENT_CREATEDBY = "event creator";
  private static final Long EVENT_MODIFIED = new Long(50);
  private static final String EVENT_MODIFIED_BY = "event modifier";
  private static final String EVENT_ATTACH_ID = "abc";
  private static final String EVENT_TEMP_REF_ID = "temp ref id";

  private static final String STREAM_ID = "abc";
  private static final String STREAM_NAME = "testStream";
  private static final String STREAM_PARENT_ID = STREAM_ID;
  private static final Boolean STREAM_SINGLE_ACTIVITY = true;
  private static Map<String, Object> streamClientData;
  private static final String STREAM_CLIENT_KEY = "testKey";
  private static final String STREAM_CLIENT_VALUE = "testValue";
  private static List<Stream> streamChildren;
  private static final Boolean STREAM_TRASHED = true;
  private static final Long STREAM_CREATED = new Long(1000);
  private static final String STREAM_CREATED_BY = "Bob";
  private static final Long STREAM_MODIFIED = new Long(1500);
  private static final String STREAM_MODIFIED_BY = "Bill";
  private static final String STREAM_CHILD_ID = "childid";
  private static final String STREAM_CHILD_NAME = "childname";
  private static final Boolean STREAM_CHILD_TRASHED = false;
  private static final Long STREAM_CHILD_CREATED = new Long(200);
  private static final String STREAM_CHILD_CREATED_BY = "creator of child";
  private static final Long STREAM_CHILD_MODIFIED = new Long(300);
  private static final String STREAM_CHILD_MODIFIED_BY = "modified of child";

  public static Stream generateFullStream() {
    streamClientData = new HashMap<String, Object>();
    streamClientData.put(STREAM_CLIENT_KEY, STREAM_CLIENT_VALUE);
    streamChildren = new ArrayList<Stream>();
    streamChildren.add(new Stream(STREAM_CHILD_ID, STREAM_CHILD_NAME, STREAM_ID, null, null, null,
      STREAM_CHILD_TRASHED, STREAM_CHILD_CREATED, STREAM_CHILD_CREATED_BY, STREAM_CHILD_MODIFIED,
      STREAM_CHILD_MODIFIED_BY));
    return new Stream(STREAM_ID, STREAM_NAME, null, STREAM_SINGLE_ACTIVITY,
      streamClientData, streamChildren, STREAM_TRASHED, STREAM_CREATED, STREAM_CREATED_BY,
      STREAM_MODIFIED, STREAM_MODIFIED_BY);
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
    eventRefs.add(EVENT_REF);
    eventAttachments = new HashSet<Attachment>();
    eventAttachments.add(new Attachment(EVENT_ATTACH_ID, "testfile", "test", 0, "abc132"));
    eventClientData = new HashMap<String, Object>();
    eventClientData.put(EVENT_CLIENTKEY, EVENT_CLIENTVALUE);
    return new Event(EVENT_ID, EVENT_STREAMID, EVENT_TIME, EVENT_DURATION, EVENT_TYPE,
      EVENT_CONTENT, eventTags, eventRefs, EVENT_DESCRIPTION, eventAttachments,
      eventClientData, EVENT_TRASHED, EVENT_CREATED, EVENT_CREATEDBY, EVENT_MODIFIED,
      EVENT_MODIFIED_BY, EVENT_TEMP_REF_ID);
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
    return new Filter(new Long(0), new Long(1000), dummyStringSet, dummyStringSet, dummyStringSet,
      false, true, new Integer(0), new Integer(10), Filter.State.ALL, new Long(500));
  }

  public static String getId() {
    return EVENT_ID;
  }

  public static String getStreamid() {
    return EVENT_STREAMID;
  }

  public static Long getTime() {
    return EVENT_TIME;
  }

  public static Long getDuration() {
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
    return EVENT_REF;
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

  public static Long getCreated() {
    return EVENT_CREATED;
  }

  public static String getCreatedby() {
    return EVENT_CREATEDBY;
  }

  public static Long getModified() {
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

  public static Long getStreamCreated() {
    return STREAM_CREATED;
  }

  public static String getStreamCreatedBy() {
    return STREAM_CREATED_BY;
  }

  public static Long getStreamModified() {
    return STREAM_MODIFIED;
  }

  public static String getStreamModifiedBy() {
    return STREAM_MODIFIED_BY;
  }

  public static String getStreamChildId() {
    return STREAM_CHILD_ID;
  }

}
