package com.pryv.api.model;

import com.google.gson.Gson;

/**
 *
 * DataStructure superclass used to implement updates from json
 *
 * @author ik
 *
 */
public class DataStructure {

  private Gson gson = new Gson();

  public Object updateFromJSon(String json, Object obj) {
    obj = gson.fromJson(json, obj.getClass());




    return obj;
  }

}
