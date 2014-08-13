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
  private static final Set<String> EVENT_TAGS = new HashSet<String>();
  private static final String EVENT_TAGTEST = "test";
  private static final String EVENT_TAGBASIC = "basic test";
  private static final Set<String> EVENT_REFS = new HashSet<String>();
  private static final String EVENT_REF = "refTest";
  private static final String EVENT_DESCRIPTION = "the test description";
  private static final Set<Attachment> EVENT_ATTACHMENTS = new HashSet<Attachment>();
  private static final Map<String, Object> EVENT_CLIENTDATA = new HashMap<String, Object>();
  private static final String EVENT_CLIENTKEY = "color";
  private static final String EVENT_CLIENTVALUE = "value";
  private static final Boolean EVENT_TRASHED = false;
  private static final Long EVENT_CREATED = new Long(10);
  private static final String EVENT_CREATEDBY = "Bob";
  private static final Long EVENT_MODIFIED = new Long(50);
  private static final String EVENT_MODIFIED_BY = "Tom";
  private static final String EVENT_ATTACH_ID = "abc";
  private static final String EVENT_TEMP_REF_ID = "temp ref id";

  private static final String STREAM_ID = "abc";
  private static final String STREAM_NAME = "testStream";
  private static final String STREAM_PARENT_ID = "ABC";
  private static final Boolean STREAM_SINGLE_ACTIVITY = true;
  private static final Map<String, Object> STREAM_CLIENT_DATA = new HashMap<String, Object>();
  private static final String STREAM_CLIENT_KEY = "testKey";
  private static final String STREAM_CLIENT_VALUE = "testValue";
  private static final List<Stream> STREAM_CHILDREN = new ArrayList<Stream>();
  private static final Boolean STREAM_TRASHED = false;
  private static final Long STREAM_CREATED = new Long(1000);
  private static final String STREAM_CREATED_BY = "Bob";
  private static final Long STREAM_MODIFIED = new Long(1500);
  private static final String STREAM_MODIFIED_BY = "Bill";
  private static final String STREAM_CHILD_ID = "aaa";

  public static Stream generateFullStream() {
    STREAM_CLIENT_DATA.put(STREAM_CLIENT_KEY, STREAM_CLIENT_VALUE);
    STREAM_CHILDREN.add(new Stream(STREAM_CHILD_ID, null, null, null, null, null, false, null,
      null, null, null));
    return new Stream(STREAM_ID, STREAM_NAME, STREAM_PARENT_ID, STREAM_SINGLE_ACTIVITY,
      STREAM_CLIENT_DATA, STREAM_CHILDREN, STREAM_TRASHED, STREAM_CREATED, STREAM_CREATED_BY,
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
    EVENT_TAGS.add(EVENT_TAGTEST);
    EVENT_TAGS.add(EVENT_TAGBASIC);
    EVENT_REFS.add(EVENT_REF);
    EVENT_ATTACHMENTS.add(new Attachment(EVENT_ATTACH_ID, "testfile", "test", 0, "abc132"));
    EVENT_CLIENTDATA.put(EVENT_CLIENTKEY, EVENT_CLIENTVALUE);
    return new Event(EVENT_ID, EVENT_STREAMID, EVENT_TIME, EVENT_DURATION, EVENT_TYPE, EVENT_CONTENT, EVENT_TAGS, EVENT_REFS, EVENT_DESCRIPTION,
      EVENT_ATTACHMENTS, EVENT_CLIENTDATA, EVENT_TRASHED, EVENT_CREATED, EVENT_CREATEDBY, EVENT_MODIFIED, EVENT_MODIFIED_BY, EVENT_TEMP_REF_ID);
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
    return EVENT_TAGS;
  }

  public static String getTagTest() {
    return EVENT_TAGTEST;
  }

  public static String getTagbasic() {
    return EVENT_TAGBASIC;
  }

  public static Set<String> getRefs() {
    return EVENT_REFS;
  }

  public static String getRef() {
    return EVENT_REF;
  }

  public static String getDescription() {
    return EVENT_DESCRIPTION;
  }

  public static Set<Attachment> getAttachments() {
    return EVENT_ATTACHMENTS;
  }

  public static Map<String, Object> getClientdata() {
    return EVENT_CLIENTDATA;
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
    return STREAM_CLIENT_DATA;
  }

  public static String getStreamClientKey() {
    return STREAM_CLIENT_KEY;
  }

  public static String getStreamClientValue() {
    return STREAM_CLIENT_VALUE;
  }

  public static List<Stream> getStreamChildren() {
    return STREAM_CHILDREN;
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
