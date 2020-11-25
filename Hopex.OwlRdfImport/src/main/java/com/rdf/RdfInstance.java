package com.rdf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class RdfInstance {
  private String                                         rdfId;
  private String                                         name;
  private RdfType                                        type;
  private HashMap<RdfField, ArrayList<RdfInstanceValue>> values;
  private RdfStore                                       store;
  private Model                                          model;
  private Resource                                       resource;

  public RdfInstance(final RdfType type, final String rdfId) {
    this.type = type;
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
    this.values = new HashMap<RdfField, ArrayList<RdfInstanceValue>>();
  }

  public RdfInstance(final RdfStore store, final Model model, final Resource resource) {
    this.values = new HashMap<RdfField, ArrayList<RdfInstanceValue>>();
    this.store = store;
    this.model = model;
    this.resource = resource;
    RdfType type = new RdfType(resource);
    if (store.types.indexOf(type) > -1) {
      type = store.types.get(store.types.indexOf(type));
    } else {
      store.types.add(type);
    }
    this.type = type;
    this.rdfId = resource.stringValue();
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
    this.getInstanceValues();
  }

  public RdfInstance(final RdfStore store, final Resource resource) {
    this.store = store;
    this.values = new HashMap<RdfField, ArrayList<RdfInstanceValue>>();
    this.resource = resource;
    RdfType type = new RdfType(resource);
    if (store.types.indexOf(type) > -1) {
      type = store.types.get(store.types.indexOf(type));
    } else {
      store.types.add(type);
    }
    this.type = type;
    this.rdfId = resource.stringValue();
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

  private void getInstanceValues() {
    final Model modelIRI = this.model.filter(this.resource, (IRI) null, (Value) null, new Resource[0]);
    for (final IRI predicate : new ArrayList<IRI>(modelIRI.predicates())) {
      final Model modelObject = modelIRI.filter(this.resource, predicate, (Value) null, new Resource[0]);
      for (final Value object : new ArrayList<Value>(modelObject.objects())) {
        if (object instanceof BNode) {
          final RdfField field = new RdfField(this.type, predicate);
          this.getInstanceBNodeValues(field, (BNode) object);
        } else {
          RdfField field = new RdfField(this.type, predicate);
          if (this.type.getFields().indexOf(field) > -1) {
            field = this.type.getFields().get(this.type.getFields().indexOf(field));
          } else {
            this.type.getFields().add(field);
          }
          final RdfInstanceValue value = new RdfInstanceValue(this.store, this.model, this, object);
          this.addValue(field, value);
        }
      }
    }
  }

  private void getInstanceBNodeValues(RdfField field, final BNode bNode) {
    final Model modelBNode = this.model.filter(bNode, (IRI) null, (Value) null, new Resource[0]);
    for (final IRI predicateBNode : new ArrayList<IRI>(modelBNode.predicates())) {
      if (predicateBNode.equals(RDF.FIRST)) {
        final Model modelIRI = modelBNode.filter(bNode, predicateBNode, (Value) null, new Resource[0]);
        for (final Value object : new ArrayList<Value>(modelIRI.objects())) {
          if (object instanceof BNode) {
            System.out.println("Not Processing These");
          } else {
            if (this.type.getFields().indexOf(field) > -1) {
              field = this.type.getFields().get(this.type.getFields().indexOf(field));
            } else {
              this.type.getFields().add(field);
            }
            final RdfInstanceValue value = new RdfInstanceValue(this.store, modelIRI, this, object);
            this.addValue(field, value);
          }
        }
      } else {
        if (!predicateBNode.equals(RDF.REST)) {
          continue;
        }
        final Model modelIRI = modelBNode.filter(bNode, predicateBNode, (Value) null, new Resource[0]);
        for (final Value object : new ArrayList<Value>(modelIRI.objects())) {
          if (object instanceof BNode) {
            this.getInstanceBNodeValues(field, (BNode) object);
          }
        }
      }
    }
  }

  public void addValue(final RdfField field, final RdfInstanceValue value) {
    if (this.type.getFields().indexOf(field) > -1) {
      if (this.values.containsKey(field)) {
        final ArrayList<RdfInstanceValue> valueList = this.values.get(field);
        valueList.add(value);
      } else {
        final ArrayList<RdfInstanceValue> valueList = new ArrayList<RdfInstanceValue>();
        valueList.add(value);
        this.values.put(field, valueList);
      }
      return;
    }
    throw new RuntimeException("Field: " + field + " does not exist on the Type:" + this.type);
  }

  public ArrayList<RdfInstanceValue> getValue(final RdfField field) {
    if (this.type.getFields().indexOf(field) > -1) {
      return this.values.get(field);
    }
    return null;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.rdfId;
  }

  public RdfType getType() {
    return this.type;
  }

  public String printObject() {
    String ret = "Type: " + this.type + " ID: " + this.rdfId + " Name: " + this.name;
    for (final RdfField field : this.values.keySet()) {
      ret = String.valueOf(ret) + "\r\n\tField: " + field + " Values: ";
      for (final RdfInstanceValue value : this.values.get(field)) {
        ret = String.valueOf(ret) + "\r\n\t\t" + value;
      }
    }
    return ret;
  }

  @Override
  public boolean equals(final Object o) {
    return o.toString().equals(this.rdfId);
  }
}
