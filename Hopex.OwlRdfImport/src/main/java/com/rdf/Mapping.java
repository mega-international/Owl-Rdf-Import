package com.rdf;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mega.modeling.api.MegaObject;
import com.mega.modeling.api.MegaRoot;

public class Mapping {
  private ArrayList<MappingType> mappingTypes;
  private ArrayList<MegaObject>  megaObjects;
  private ArrayList<RdfInstance> instances;

  public Mapping(final MegaRoot mgRoot, final JSONObject mappingJSON) {
    this.mappingTypes = new ArrayList<MappingType>();
    this.megaObjects = new ArrayList<MegaObject>();
    this.instances = new ArrayList<RdfInstance>();
    try {
      final JSONArray mappingsJSON = mappingJSON.has("mappings") ? mappingJSON.getJSONArray("mappings") : null;
      if (mappingsJSON != null) {
        for (int i = 0; i < mappingsJSON.length(); ++i) {
          final JSONObject mappingItemJSON = mappingsJSON.getJSONObject(i);
          this.mappingTypes.add(new MappingType(mgRoot, this, mappingItemJSON));
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<MappingType> getMappingTypes() {
    return this.mappingTypes;
  }

  public ArrayList<RdfInstance> getRdfInstances() {
    return this.instances;
  }

  public ArrayList<MegaObject> getMegaObjects() {
    return this.megaObjects;
  }
}
