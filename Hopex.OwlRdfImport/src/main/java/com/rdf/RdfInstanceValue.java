package com.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

public class RdfInstanceValue {
  private String      type;
  private Object      value;
  private RdfStore    store;
  private Model       model;
  private RdfInstance rdfInstance;
  private Value       object;

  public RdfInstanceValue(final RdfStore store, final Model model, final RdfInstance rdfInstance, final Value object) {
    this.store = store;
    this.model = model;
    this.rdfInstance = rdfInstance;
    this.object = object;
    if (object instanceof Literal) {
      final Literal lit = (Literal) object;
      final IRI datatype = lit.getDatatype();
      this.type = datatype.stringValue();
      this.value = lit.stringValue();
    } else if (object instanceof IRI) {
      final RdfInstance newRdfInstance = new RdfInstance(store, (Resource) object);
      this.value = newRdfInstance;
    }
  }

  public RdfInstanceValue(final String valueStr) {
    this.value = valueStr;
  }

  public String getType() {
    return this.type;
  }

  public Object getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.value.toString();
  }
}
