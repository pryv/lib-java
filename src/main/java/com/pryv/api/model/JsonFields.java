package com.pryv.api.model;

/**
 *
 * represents the tags of the fields of datastructure objects Event, Stream,
 * Attachment, Access in JSON format
 *
 * @author ik
 *
 */
public enum JsonFields {

  ID("id"), TIME("time"), STREAM_ID("streamId"), DURATION("duration"), TAGS("tags"), TYPE("type"),
  CONTENT("content"), REFERENCES("references"), DESCRIPTION("description"), ATTACHMENTS(
      "attachments"), CLIENT_DATA("clientData"), TRASHED("trashed"), CREATED("created"),
  CREATED_BY("createdBy"), MODIFIED("modified"), MODIFIED_BY("modifiedBy"), FILENAME("filename"),
  NUMBER("number"), READ_TOKEN("readToken"), NAME("name"), PARENT_ID("parentId"), SINGLE_ACTIVITY(
      "singleActivity"), CHILDREN("children"), LEVEL("level"), DEFAULT_NAME("defaultName"),
  LANGUAGE_CODE("languageCode");

  private final String field;

  JsonFields(String pField) {
    field = pField;
  }

  @Override
  public String toString() {
    return field;
  }

}
