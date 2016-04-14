package com.pryv.utils;

import java.util.HashMap;
import java.util.Map;

import com.pryv.model.Stream;

/**
 * utilitary functions for Streams manipulation
 *
 * @author ik
 *
 */
public class StreamUtils {

  /**
   * Returns a reference to the child Stream with ID childId contained in the
   * Map<String, Stream> pStreams. The search is done in Breadth-First.
   *
   * @param searchId
   *          the ID of the Stream you are looking for
   * @param rootStreams
   *          the root streams for the search
   * @return the reference to the Stream object with ID childId or null if no
   *         Stream with such an Id is found.
   */
  public static Stream findStreamReference(String searchId, Map<String, Stream> rootStreams) {
    if (rootStreams != null && searchId != null) {
      if (rootStreams.size() > 0) {
        Map<String, Stream> tempStreams = new HashMap<String, Stream>();
        for (String rootStreamId : rootStreams.keySet()) {
          if (rootStreamId.equals(searchId)) {
            return rootStreams.get(searchId);
          } else if (rootStreams.get(rootStreamId).getChildrenMap() != null) {
            tempStreams.putAll(rootStreams.get(rootStreamId).getChildrenMap());
          }
        }
        return findStreamReference(searchId, tempStreams);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

}
