package com.rdf;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Resource;

public class RdfType {
  private String              rdfId;
  private ArrayList<RdfField> fields;

  public RdfType(final String rdfId) {
    this.rdfId = rdfId;
    this.fields = new ArrayList<RdfField>();
  }

  public RdfType(final Resource resource) {
    String rdfId = resource.stringValue();
    final String[] rdfIdSplit = rdfId.split(Pattern.quote("://"));
    rdfId = ((rdfIdSplit.length == 2) ? rdfIdSplit[1] : rdfId);
    if (rdfId.indexOf("#") > -1) {
      rdfId = rdfId.substring(0, rdfId.lastIndexOf("#"));
    } else if (rdfId.indexOf("/") > -1) {
      rdfId = rdfId.substring(0, rdfId.lastIndexOf("/"));
    }
    if (rdfIdSplit.length == 2) {
      rdfId = String.valueOf(rdfIdSplit[0]) + "://" + rdfId;
    }
    this.rdfId = rdfId;
    this.fields = new ArrayList<RdfField>();
  }

  public ArrayList<RdfField> getFields() {
    return this.fields;
  }

  @Override
  public String toString() {
    return this.rdfId;
  }

  public String printObject() {
    String ret = "Type: " + this.rdfId;
    for (final RdfField field : this.fields) {
      ret = String.valueOf(ret) + "\r\n\tField: " + field;
    }
    return ret;
  }

  @Override
  public boolean equals(final Object o) {
    return o.toString().equals(this.rdfId);
  }

  public RdfField getField(final RdfField field) {
    for (final RdfField temp : this.fields) {
      if (temp.equals(field)) {
        return temp;
      }
    }
    return field;
  }
}
