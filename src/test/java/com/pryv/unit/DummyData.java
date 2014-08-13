package com.pryv.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.utils.JsonConverter;

/**
 * generates dummy data for testing
 *
 * @author ik
 *
 */
public class DummyData {

  private static final String ID = "testID";
  private static final String STREAMID = "testStreamID";
  private static final Long TIME = new Long(10000);
  private static final Long DURATION = new Long(20); // opt
  private static final String TYPE = "testType";
  private static final String CONTENT = "testContent it's a string"; // opt
  private static Set<String> tags = new HashSet<String>();
  private static final String TAGTEST = "test";
  private static final String TAGBASIC = "basic test";
  private static final Set<String> REFS = new HashSet<String>();
  private static final String REF = "refTest";
  private static final String DESCRIPTION = "the test description";
  private static final Set<Attachment> ATTACHMENTS = new HashSet<Attachment>();
  private static final Map<String, Object> CLIENTDATA = new HashMap<String, Object>();
  private static final String CLIENTKEY = "color";
  private static final String CLIENTVALUE = "value";
  private static final Boolean TRASHED = false;
  private static final Long CREATED = new Long(10);
  private static final String CREATEDBY = "Bob";
  private static final Long MODIFIED = new Long(50);
  private static final String MODIFIED_BY = "Tom";
  private static final String ATTACH_ID = "abc";
  private static final String TEMP_REF_ID = "temp ref id";

  private static Event testEvent;
  private static String jsonTestEvent;

  public static Event generateFullEvent() {
    tags.add(TAGTEST);
    tags.add(TAGBASIC);
    REFS.add(REF);
    ATTACHMENTS.add(new Attachment(ATTACH_ID, "testfile", "test", 0, "abc132"));
    CLIENTDATA.put(CLIENTKEY, CLIENTVALUE);
    return new Event(ID, STREAMID, TIME, DURATION, TYPE, CONTENT, tags, REFS, DESCRIPTION,
      ATTACHMENTS, CLIENTDATA, TRASHED, CREATED, CREATEDBY, MODIFIED, MODIFIED_BY, TEMP_REF_ID);
  }

  public static String generateJsonEvent() {
    try {
      return JsonConverter.toJson(generateFullEvent());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getId() {
    return ID;
  }

  public static String getStreamid() {
    return STREAMID;
  }

  public static Long getTime() {
    return TIME;
  }

  public static Long getDuration() {
    return DURATION;
  }

  public static String getType() {
    return TYPE;
  }

  public static String getContent() {
    return CONTENT;
  }

  public static Set<String> getTags() {
    return tags;
  }

  public static String getTagTest() {
    return TAGTEST;
  }

  public static String getTagbasic() {
    return TAGBASIC;
  }

  public static Set<String> getRefs() {
    return REFS;
  }

  public static String getRef() {
    return REF;
  }

  public static String getDescription() {
    return DESCRIPTION;
  }

  public static Set<Attachment> getAttachments() {
    return ATTACHMENTS;
  }

  public static Map<String, Object> getClientdata() {
    return CLIENTDATA;
  }

  public static String getClientkey() {
    return CLIENTKEY;
  }

  public static String getClientvalue() {
    return CLIENTVALUE;
  }

  public static Boolean getTrashed() {
    return TRASHED;
  }

  public static Long getCreated() {
    return CREATED;
  }

  public static String getCreatedby() {
    return CREATEDBY;
  }

  public static Long getModified() {
    return MODIFIED;
  }

  public static String getModifiedBy() {
    return MODIFIED_BY;
  }

  public static String getAttachId() {
    return ATTACH_ID;
  }

  public static String getTempRefId() {
    return TEMP_REF_ID;
  }

  public static Event getTestEvent() {
    return testEvent;
  }

  public static String getJsonTestEvent() {
    return jsonTestEvent;
  }

}
