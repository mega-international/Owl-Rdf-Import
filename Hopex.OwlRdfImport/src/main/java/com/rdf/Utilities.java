package com.rdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mega.modeling.api.MegaCollection;
import com.mega.modeling.api.MegaObject;
import com.mega.modeling.api.MegaRoot;
import com.mega.modeling.api.util.MegaProgressControl;

public class Utilities {
  HashMap<String, ArrayList<String>> sCreatedObject;

  public Model parseRdfFileToModel(final String rdfFile) {
    final Model model = new LinkedHashModel();
    try {
      final RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
      rdfParser.setRDFHandler(new StatementCollector(model));
      Throwable t = null;
      try {
        final InputStream input = new FileInputStream(rdfFile);
        try {
          final ParserConfig pc = rdfParser.getParserConfig();
          pc.set(BasicParserSettings.VERIFY_URI_SYNTAX, false);
          pc.set(BasicParserSettings.VERIFY_RELATIVE_URIS, false);
          rdfParser.setParserConfig(pc);
          rdfParser.parse(input, "https://community.mega.com/generic");
        } finally {
          if (input != null) {
            input.close();
          }
        }
      } finally {
        if (t == null) {
          final Throwable exception = null;
          t = exception;
        } else {
          final Throwable exception = null;
          if (t != exception) {
            t.addSuppressed(exception);
          }
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (RDFParseException e2) {
      e2.printStackTrace();
    } catch (UnsupportedRDFormatException e3) {
      e3.printStackTrace();
    } catch (IOException e4) {
      e4.printStackTrace();
    }
    return model;
  }

  public RdfStore parseRdfFileToStore(final String rdfFile) {
    final RdfStore store = new RdfStore();
    final Model model = this.parseRdfFileToModel(rdfFile);
    for (final Resource res : new ArrayList<Resource>(model.subjects())) {
      if (res instanceof IRI) {
        final RdfInstance instance = new RdfInstance(store, model, res);
        store.instances.add(instance);
      }
    }
    for (final RdfInstance instance2 : store.instances) {
      System.out.println(instance2.printObject());
    }
    for (final RdfType type : store.types) {
      System.out.println(type.printObject());
    }
    return store;
  }

  public void createMappingTemplateFile(final String rdfFile, final String mappingFile) {
    final JSONObject mapping = this.createMappingTemplateJSONObject(rdfFile);
    System.out.println(mapping.toString());
    try {
      final BufferedWriter writer = Files.newBufferedWriter(Paths.get(mappingFile, new String[0]), new OpenOption[0]);
      mapping.write(writer);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e2) {
      e2.printStackTrace();
    }
  }

  public String createMappingTemplateJSON(final String rdfFile) {
    final JSONObject mapping = this.createMappingTemplateJSONObject(rdfFile);
    final String ret = (mapping != null) ? mapping.toString() : "{}";
    return ret;
  }

  private JSONObject createMappingTemplateJSONObject(final String rdfFile) {
    JSONObject mapping = null;
    final RdfStore store = this.parseRdfFileToStore(rdfFile);
    try {
      mapping = new JSONObject();
      final JSONArray mappings = new JSONArray();
      for (final RdfType type : store.types) {
        System.out.println(type.printObject());
        final JSONObject mappingItem = new JSONObject();
        mappingItem.put("metaclass", "");
        mappingItem.put("rdf_type", type.toString());
        final JSONArray mappingItemConditions = new JSONArray();
        mappingItem.put("conditions", mappingItemConditions);
        final JSONObject mappingCondition = new JSONObject();
        mappingCondition.put("rdf_field", "");
        mappingCondition.put("value", "");
        mappingItemConditions.put(mappingCondition);
        final JSONArray mappingFields = new JSONArray();
        mappingItem.put("fields", mappingFields);
        for (final RdfField field : type.getFields()) {
          final JSONObject mappingField = new JSONObject();
          mappingField.put("rdf_field", field.toString());
          mappingField.put("rdf_type", "");
          mappingField.put("metaattribute", "");
          mappingField.put("metaassociation", "");
          mappingField.put("metaclass", "");
          mappingField.put("macro", "");
          final JSONArray mappingFieldValues = new JSONArray();
          final JSONObject mappingFieldValue = new JSONObject();
          mappingFieldValue.put("rdf_value", "");
          mappingFieldValue.put("mega_value", "");
          mappingFieldValues.put(mappingFieldValue);
          mappingField.put("value_map", mappingFieldValues);
          mappingFields.put(mappingField);
        }
        mappings.put(mappingItem);
      }
      mapping.put("mappings", mappings);
      for (final RdfInstance instance : store.instances) {
        System.out.println(instance.printObject());
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return mapping;
  }

  public void writeRdfFile(final String rdfFile, final RdfStore store) {
    try {
      final ValueFactory factory = SimpleValueFactory.getInstance();
      final Model model = new LinkedHashModel();
      for (final RdfInstance instance : store.instances) {
        for (final RdfField field : instance.getType().getFields()) {
          final IRI subject = factory.createIRI(instance.toString());
          final IRI predicate = factory.createIRI(field.toString());
          final ArrayList<RdfInstanceValue> values = instance.getValue(field);
          if (values == null) {
            continue;
          }
          for (final RdfInstanceValue value : values) {
            Value object = null;
            if (value.toString().indexOf("://") > -1) {
              object = factory.createIRI(value.toString());
            } else {
              object = factory.createLiteral(value.toString());
            }
            final Statement st = factory.createStatement(subject, predicate, object);
            //model.add((Object)st);
          }
        }
      }
      final FileOutputStream out = new FileOutputStream(rdfFile);
      try {
        Rio.write(model, out, RDFFormat.RDFXML);
      } finally {
        out.close();
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (RDFParseException e2) {
      e2.printStackTrace();
    } catch (UnsupportedRDFormatException e3) {
      e3.printStackTrace();
    } catch (IOException e4) {
      e4.printStackTrace();
    }
  }

  public void exportRdfUsingMappingFile(final MegaRoot mgRoot, final MegaCollection mgcolObjects, final String rdfFile, final String mappingFile) {
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
      final StringBuilder stringBuilder = new StringBuilder();
      String line = null;
      final String ls = System.getProperty("line.separator");
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      reader.close();
      final String content = stringBuilder.toString();
      final JSONObject mappingJSONObject = new JSONObject(content);
      this.exportRdfUsingMappingJSONObject(mgRoot, mgcolObjects, rdfFile, mappingJSONObject);
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e2) {
      e2.printStackTrace();
    } catch (IOException e3) {
      e3.printStackTrace();
    }
  }

  public void exportRdfUsingMappingJSON(final MegaRoot mgRoot, final MegaCollection mgcolObjects, final String rdfFile, final String mappingJSON) {
    try {
      final JSONObject mappingJSONObject = new JSONObject(mappingJSON);
      this.exportRdfUsingMappingJSONObject(mgRoot, mgcolObjects, rdfFile, mappingJSONObject);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void exportRdfUsingMappingJSONObject(final MegaRoot mgRoot, final MegaCollection mgcolObjects, final String rdfFile, final JSONObject mappingJSON) {
    final Mapping mapping = new Mapping(mgRoot, mappingJSON);
    final RdfStore store = new RdfStore();
    for (int i = 1, iMax = mgcolObjects.size(); i <= iMax; ++i) {
      final MegaObject mgobjObject = mgcolObjects.get(i);
      for (final MappingType mappingType : mapping.getMappingTypes()) {
        if (mappingType.mapsToMegaObject(mgobjObject)) {
          mappingType.megaObjectToRdfInstance(mgRoot, mgobjObject);
        }
      }
    }
    store.instances = mapping.getRdfInstances();
    this.writeRdfFile(rdfFile, store);
  }

  public void importRdfUsingMappingFile(final MegaRoot mgRoot, final String rdfFile, final String mappingFile) {
    this.importRdfUsingMappingFileWithProgress(mgRoot, rdfFile, mappingFile, null);
  }

  public String importRdfUsingMappingFileWithProgress(final MegaRoot mgRoot, final String rdfFile, final String sMappingFile, final MegaProgressControl progress) {
    this.sCreatedObject = new HashMap<String, ArrayList<String>>();
    String sResult = "";
    try {
      //String mappingFile = AnalysisRenderingToolbox.getResourceURL("rdf-owl mapping.json", mgRoot.currentEnvironment().getContext(), mgRoot).toString();

      final BufferedReader reader = new BufferedReader(new FileReader(sMappingFile));
      final StringBuilder stringBuilder = new StringBuilder();
      String line = null;
      final String ls = System.getProperty("line.separator");
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      reader.close();
      final String content = stringBuilder.toString();
      final JSONObject mappingJSONObject = new JSONObject(content);
      this.importRdfUsingMappingJSONObject(mgRoot, rdfFile, mappingJSONObject, progress);
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e2) {
      e2.printStackTrace();
    } catch (IOException e3) {
      e3.printStackTrace();
    }
    for (Entry<String, ArrayList<String>> item : this.sCreatedObject.entrySet()) {
      for (String ObjectName : item.getValue()) {
        sResult = sResult + "the " + item.getKey() + " " + ObjectName + " is imported" + "\n";
      }
    }
    return sResult;
  }

  public void importRdfUsingMappingJSON(final MegaRoot mgRoot, final String rdfFile, final String mappingJSON) {
    this.importRdfUsingMappingJSONWithProgress(mgRoot, rdfFile, mappingJSON, null);
  }

  public void importRdfUsingMappingJSONWithProgress(final MegaRoot mgRoot, final String rdfFile, final String mappingJSON, final MegaProgressControl progress) {
    try {
      final JSONObject mappingJSONObject = new JSONObject(mappingJSON);
      this.importRdfUsingMappingJSONObject(mgRoot, rdfFile, mappingJSONObject, progress);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void importRdfUsingMappingJSONObject(final MegaRoot mgRoot, final String rdfFile, final JSONObject mappingJSON, final MegaProgressControl progress) {
    final Mapping mapping = new Mapping(mgRoot, mappingJSON);
    final RdfStore store = this.parseRdfFileToStore(rdfFile);
    if ((progress != null) && (store.instances.size() > 0)) {
      progress.setRange(0, store.instances.size());
    }
    for (final RdfInstance instance : store.instances) {
      if (progress != null) {
        progress.incrementRange(1);
      }
      for (final MappingType mappingType : mapping.getMappingTypes()) {
        if (mappingType.mapsToRdfInstance(instance)) {
          HashMap<String, ArrayList<String>> tmp = mappingType.rdfInstanceToMegaObject(mgRoot, instance);

          for (Entry<String, ArrayList<String>> item : tmp.entrySet()) {
            if (!item.getValue().get(0).equals("")) {
              if (this.sCreatedObject.get(item.getKey()) == null) {
                this.sCreatedObject.put(item.getKey(), item.getValue());
              } else {
                this.sCreatedObject.get(item.getKey()).add(item.getValue().get(0));
              }
            }
          }
        }
      }
    }
  }
}
