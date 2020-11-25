package com.rdf;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mega.modeling.api.MegaObject;
import com.mega.modeling.api.MegaRoot;

public class MappingType {
  private RdfType                     rdfType;
  private MegaObject                  metaClass;
  private ArrayList<MappingCondition> conditions;
  private ArrayList<MappingField>     fields;
  private Mapping                     mapping;

  public MappingType(final MegaRoot mgRoot, final Mapping mapping, final JSONObject mappingTypeJSON) {
    this.mapping = mapping;
    this.conditions = new ArrayList<MappingCondition>();
    this.fields = new ArrayList<MappingField>();
    if (!mappingTypeJSON.has("rdf_type")) {
      throw new RuntimeException("JSON Mapping must have rdf_type");
    }
    if (!mappingTypeJSON.has("metaclass")) {
      throw new RuntimeException("JSON Mapping must have metaclass");
    }
    try {
      this.rdfType = new RdfType(mappingTypeJSON.getString("rdf_type"));
      this.metaClass = mgRoot.getObjectFromID(mappingTypeJSON.getString("metaclass"));
      if (mappingTypeJSON.has("conditions")) {
        final JSONArray conditionsJSON = mappingTypeJSON.getJSONArray("conditions");
        for (int i = 0; i < conditionsJSON.length(); ++i) {
          final JSONObject conditionJSON = conditionsJSON.getJSONObject(i);
          final String field = conditionJSON.has("rdf_field") ? conditionJSON.getString("rdf_field") : null;
          final String value = conditionJSON.has("value") ? conditionJSON.getString("value") : null;
          if (field != null) {
            final MappingCondition condition = new MappingCondition();
            condition.field = field;
            condition.value = value;
            this.conditions.add(condition);
          }
        }
      }
      final JSONArray fieldsJSON = mappingTypeJSON.has("fields") ? mappingTypeJSON.getJSONArray("fields") : null;
      if (fieldsJSON != null) {
        for (int i = 0; i < fieldsJSON.length(); ++i) {
          final JSONObject fieldJSON = fieldsJSON.getJSONObject(i);
          final MappingField field2 = new MappingField(mgRoot, mapping, fieldJSON);
          this.fields.add(field2);
          this.rdfType.getFields().add(field2.getField());
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public boolean mapsToRdfInstance(final RdfInstance instance) {
    if (!instance.getType().equals(this.rdfType)) {
      return false;
    }
    final Iterator<MappingCondition> iterator = this.conditions.iterator();
    if (!iterator.hasNext()) {
      return true;
    }
    final MappingCondition condition = iterator.next();
    final RdfField field = instance.getType().getField(new RdfField(condition.field));
    final ArrayList<RdfInstanceValue> values = instance.getValue(field);
    if (values == null) {
      return false;
    }
    for (final RdfInstanceValue value : values) {
      if (value.toString().equals(condition.value)) {
        return true;
      }
    }
    return false;
  }

  public boolean mapsToMegaObject(final MegaObject mgobjObject) {
    return mgobjObject.getType(this.metaClass.megaField()).exists();
  }

  public HashMap<String, ArrayList<String>> rdfInstanceToMegaObject(final MegaRoot mgRoot, final RdfInstance instance) {
    HashMap<String, ArrayList<String>> sCreatedObject = new HashMap<String, ArrayList<String>>();
    MegaObject mgobjObject = mgRoot.getSelection("Select " + this.metaClass.megaField() + " Where " + "~CFmhlMxNT1iE[External Identifier]" + "\"" + instance + "\"", new Object[0]).get(1);
    if (!mgobjObject.exists()) {
      mgobjObject = mgRoot.getCollection((Object) this.metaClass.megaField(), new Object[0]).create();
      mgobjObject.setProp("~CFmhlMxNT1iE[External Identifier]", instance.toString());

    }
    mgobjObject.setProp("~Z20000000D60[Short Name]", instance.getName());
    //
    if (sCreatedObject.get(this.metaClass.megaField()) == null) {
      ArrayList<String> a = new ArrayList<String>();
      a.add(mgobjObject.getProp("~Z20000000D60[Short Name]"));
      sCreatedObject.put(this.metaClass.getProp("~Z20000000D60[Short Name]"), a);
    } else {
      sCreatedObject.get(this.metaClass.getProp("~Z20000000D60[Short Name]")).add(mgobjObject.getProp("~Z20000000D60[Short Name]"));
    }
    for (final MappingField field : this.fields) {
      if (field.getMetaAttribute() != null) {
        field.setMetaAttribute(mgobjObject, instance);
      } else if (field.getMetaAssociationEnd() != null) {
        field.setMetaAssociation(mgobjObject, instance);
      } else {
        if (field.getMacro() == null) {
          continue;
        }
        field.setMacro(mgobjObject, instance);
      }
    }
    this.mapping.getMegaObjects().add(mgobjObject);
    return sCreatedObject;
  }

  public void megaObjectToRdfInstance(final MegaRoot mgRoot, final MegaObject mgobjObject) {
    try {
      final RdfInstance instance = new RdfInstance(this.rdfType, URI.create(this.rdfType + "/" + URLEncoder.encode(mgobjObject.getProp("~Z20000000D60[Short Name]"), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20")).toString());
      mgobjObject.setProp("~CFmhlMxNT1iE[External Identifier]", instance.toString());
      for (final MappingField field : this.fields) {
        if (field.getMetaAttribute() != null) {
          field.setInstanceAttribute(instance, mgobjObject);
        } else {
          if (field.getMetaAssociationEnd() == null) {
            continue;
          }
          field.setInstanceAssociation(instance, mgobjObject);
        }
      }
      this.mapping.getRdfInstances().add(instance);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
