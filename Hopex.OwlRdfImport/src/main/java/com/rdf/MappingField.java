package com.rdf;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mega.modeling.api.MegaCOMObject;
import com.mega.modeling.api.MegaCollection;
import com.mega.modeling.api.MegaObject;
import com.mega.modeling.api.MegaRoot;

public class MappingField {
  private RdfField                field;
  private String                  rdfType;
  private MegaObject              metaAttribute;
  private HashMap<String, String> rdfToMegaValues;
  private HashMap<String, String> megaToRdfValues;
  private MegaObject              metaAssociationEnd;
  private MegaObject              metaClass;
  private MegaObject              macro;
  private MegaCOMObject           macroCOM;

  public MegaObject getMetaAttribute() {
    return this.metaAttribute;
  }

  public MegaObject getMetaAssociationEnd() {
    return this.metaAssociationEnd;
  }

  public MegaObject getMacro() {
    return this.macro;
  }

  public MappingField(final MegaRoot mgRoot, final Mapping mapping, final JSONObject fieldJSON) {
    this.rdfToMegaValues = new HashMap<String, String>();
    this.megaToRdfValues = new HashMap<String, String>();
    try {
      final String rdfId = fieldJSON.has("rdf_field") ? fieldJSON.getString("rdf_field") : null;
      this.rdfType = (fieldJSON.has("rdf_type") ? fieldJSON.getString("rdf_type") : null);
      final String metaAttributeId = fieldJSON.has("metaattribute") ? fieldJSON.getString("metaattribute") : null;
      final String metaAssociationEndId = fieldJSON.has("metaassociation") ? fieldJSON.getString("metaassociation") : null;
      final String metaClassId = fieldJSON.has("metaclass") ? fieldJSON.getString("metaclass") : null;
      final String macroId = fieldJSON.has("macro") ? fieldJSON.getString("macro") : null;
      this.field = ((rdfId != null) ? new RdfField(rdfId) : null);
      this.metaAttribute = ((metaAttributeId != null) ? mgRoot.getObjectFromID(metaAttributeId) : null);
      this.metaAssociationEnd = ((metaAssociationEndId != null) ? mgRoot.getObjectFromID(metaAssociationEndId) : null);
      this.metaClass = ((metaClassId != null) ? mgRoot.getObjectFromID(metaClassId) : null);
      this.macro = ((macroId != null) ? mgRoot.getObjectFromID(macroId) : null);
      if ((this.macro != null) && this.macro.exists()) {
        this.macroCOM = (MegaCOMObject) mgRoot.currentEnvironment().getMacro(this.macro.megaField());
      }
      if ((this.metaAssociationEnd != null) && (this.metaClass == null)) {
        this.metaClass = this.metaAssociationEnd.getCollection((Object) "~j0000000C420[MetaClass]", new Object[0]).get(1);
      }
      final JSONArray mappingValuesJSON = fieldJSON.has("value_map") ? fieldJSON.getJSONArray("value_map") : null;
      if (mappingValuesJSON != null) {
        for (int i = 0; i < mappingValuesJSON.length(); ++i) {
          final JSONObject mappingValueJSON = mappingValuesJSON.getJSONObject(i);
          final String rdfValue = mappingValueJSON.has("rdf_value") ? mappingValueJSON.getString("rdf_value") : null;
          final String megaValue = mappingValueJSON.has("rdf_value") ? mappingValueJSON.getString("mega_value") : null;
          if ((rdfValue != null) && (megaValue != null)) {
            this.rdfToMegaValues.put(rdfValue, megaValue);
            this.megaToRdfValues.put(megaValue, rdfValue);
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public RdfField getField() {
    return this.field;
  }

  public void setMetaAttribute(final MegaObject mgobjObject, final RdfInstance instance) {
    final RdfField useField = instance.getType().getField(this.field);
    final ArrayList<RdfInstanceValue> values = instance.getValue(useField);
    if ((values == null) || (values.size() == 0)) {
      return;
    }
    final RdfInstanceValue value = values.get(0);
    String valueStr = value.toString();
    if (this.rdfToMegaValues.size() > 0) {
      valueStr = this.rdfToMegaValues.get(value.toString());
    }
    try {
      mgobjObject.setProp(this.metaAttribute.megaField(), valueStr);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setInstanceAttribute(final RdfInstance instance, final MegaObject mgobjObject) {
    String valueStr = (String) mgobjObject.getProp(this.metaAttribute.megaField(), "Display");
    if (this.megaToRdfValues.size() > 0) {
      valueStr = this.megaToRdfValues.get(valueStr);
    }
    final RdfInstanceValue value = new RdfInstanceValue(valueStr);
    instance.addValue(this.field, value);
  }

  public void setMacro(final MegaObject mgobjObject, final RdfInstance instance) {
    final RdfField useField = instance.getType().getField(this.field);
    final ArrayList<RdfInstanceValue> values = instance.getValue(useField);
    if ((values == null) || (values.size() == 0)) {
      return;
    }
    for (final RdfInstanceValue value : values) {
      String valueStr = value.toString();
      if (this.rdfToMegaValues.size() > 0) {
        valueStr = this.rdfToMegaValues.get(value.toString());
      }
      String valueShort = value.toString();
      if (valueShort.indexOf("#") > -1) {
        valueShort = valueShort.substring(valueShort.lastIndexOf("#") + 1);
      } else {
        valueShort = valueShort.substring(valueShort.lastIndexOf("/") + 1);
      }
      try {
        valueShort = URLDecoder.decode(valueShort, StandardCharsets.UTF_8.toString());
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      try {
        this.macroCOM.invokeMethod("RdfProcessValue", new Object[] { mgobjObject, instance.getType().toString(), useField.toString(), useField.getName(), valueStr, valueShort });
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }
  }

  public void setMetaAssociation(final MegaObject mgobjObject, final RdfInstance instance) {
    final RdfField useField = instance.getType().getField(this.field);
    final ArrayList<RdfInstanceValue> values = instance.getValue(useField);
    if (values == null) {
      return;
    }
    for (final RdfInstanceValue value : values) {
      String subject = value.toString();
      final String[] subjectSplit = subject.split(Pattern.quote("://"));
      subject = ((subjectSplit.length == 2) ? subjectSplit[1] : subject);
      if (subject.indexOf("#") > -1) {
        subject = subject.substring(0, subject.lastIndexOf("#"));
      } else if (subject.indexOf("/") > -1) {
        subject = subject.substring(0, subject.lastIndexOf("/"));
      }
      if (subjectSplit.length == 2) {
        subject = String.valueOf(subjectSplit[0]) + "://" + subject;
      }
      final RdfType typeToConnect = new RdfType(subject);
      final RdfInstance rdfToConnect = new RdfInstance(typeToConnect, value.toString());
      final String query = "Select " + this.metaClass.megaField() + " Where " + "~CFmhlMxNT1iE[External Identifier]" + "\"" + value.toString() + "\"";
      MegaObject mgobjToConnect = mgobjObject.getRoot().getSelection(query, new Object[0]).get(1);
      if (!mgobjToConnect.exists()) {
        mgobjToConnect = mgobjObject.getRoot().getCollection((Object) this.metaClass.megaField(), new Object[0]).create();
        mgobjToConnect.setProp("~CFmhlMxNT1iE[External Identifier]", rdfToConnect.toString());
        mgobjToConnect.setProp("~Z20000000D60[Short Name]", rdfToConnect.getName());
      }
      if (!mgobjObject.getCollection((Object) this.metaAssociationEnd.megaField(), new Object[0]).get(mgobjToConnect.megaField()).exists()) {
        mgobjObject.getCollection((Object) this.metaAssociationEnd.megaField(), new Object[0]).add(mgobjToConnect);
      }
    }
  }

  public void setInstanceAssociation(final RdfInstance instance, final MegaObject mgobjObject) {
    if (this.rdfType == null) {
      this.rdfType = "https://community.mega.com/metaclass/" + this.metaClass.getProp("~H20000000550[_HexaIdAbs]") + "/";
    }
    final MegaCollection mgcolCollection = mgobjObject.getCollection((Object) this.metaAssociationEnd.megaField(), new Object[0]);
    try {
      for (int i = 1, iMax = mgcolCollection.size(); i <= iMax; ++i) {
        final MegaObject mgobjToConnect = mgcolCollection.get(i);
        String rdfId = mgobjToConnect.getProp("~CFmhlMxNT1iE[External Identifier]");
        if (rdfId.length() == 0) {
          rdfId = URI.create(String.valueOf(this.rdfType) + "/" + URLEncoder.encode(mgobjToConnect.getProp("~Z20000000D60[Short Name]"), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20")).toString();
          mgobjToConnect.setProp("~CFmhlMxNT1iE[External Identifier]", rdfId);
        }
        final RdfInstanceValue value = new RdfInstanceValue(rdfId);
        instance.addValue(this.field, value);
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
