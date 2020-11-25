package com.rdf;

import java.util.ArrayList;

public class RdfStore {
  public ArrayList<RdfInstance> instances;
  public ArrayList<RdfType>     types;
  public ArrayList<String>      namespaces;

  public RdfStore() {
    this.instances = new ArrayList<RdfInstance>();
    this.types = new ArrayList<RdfType>();
    this.namespaces = new ArrayList<String>();
  }
}
