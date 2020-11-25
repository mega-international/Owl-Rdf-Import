package com.rdf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.rdf4j.model.IRI;

public class RdfField {
  private String  rdfId;
  private String  name;
  private RdfType type;
  IRI             predicate;

  public RdfField(final String rdfId) {
    this.rdfId = rdfId;
    if (rdfId.indexOf("#") > -1) {
      this.name = rdfId.substring(rdfId.lastIndexOf("#") + 1);
    } else {
      this.name = rdfId.substring(rdfId.lastIndexOf("/") + 1);
    }
    try {
      this.name = URLDecoder.decode(this.name, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public RdfField(final RdfType type, final IRI predicate) {
    this.type = type;
    this.predicate = predicate;
    this.rdfId = predicate.stringValue();
    if (this.rdfId.indexOf("#") > -1) {
      this.name = this.rdfId.substring(this.rdfId.lastIndexOf("#") + 1);
    } else {
      this.name = this.rdfId.substring(this.rdfId.lastIndexOf("/") + 1);
    }
    try {
      this.name = URLDecoder.decode(this.name, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return this.rdfId;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public boolean equals(final Object o) {
    return o.toString().equals(this.rdfId);
  }
}
