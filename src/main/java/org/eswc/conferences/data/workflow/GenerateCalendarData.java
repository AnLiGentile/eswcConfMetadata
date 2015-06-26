package org.eswc.conferences.data.workflow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;
import com.hp.hpl.jena.util.FileManager;

public class GenerateCalendarData {

	private Model calendarData;
	private ClassLoader classLoader;

	private Map<String, Resource> locationMap;

	public GenerateCalendarData(Model calendarData) {
		getClassLoader();
		this.calendarData = calendarData;
		this.locationMap = new HashMap<String, Resource>();

		InputStream inputStream = classLoader
				.getResourceAsStream("data/locations.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		String locationsJsonContent = "";
		String line = null;

		try {
			while ((line = reader.readLine()) != null)
				locationsJsonContent += line;

			inputStream.close();
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JSONObject locations = new JSONObject(locationsJsonContent);

			JSONArray locationsArray = locations.getJSONArray("locations");
			for (int i = 0, j = locationsArray.length(); i < j; i++) {
				JSONObject locationJson = locationsArray.getJSONObject(i);
				this.locationMap.put(
						locationJson.getString("name").trim(),
						ModelFactory.createDefaultModel().createResource(
								locationJson.getString("id").trim()));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String icaltzdNs = "http://www.w3.org/2002/12/cal/icaltzd#";

		String sparql = "PREFIX icaltzd: <http://www.w3.org/2002/12/cal/icaltzd#> "
				+ "SELECT ?event ?location "
				+ "WHERE {?event icaltzd:location ?location . FILTER(isLiteral(?location))}";
		Query query = QueryFactory.create(sparql, Syntax.syntaxARQ);
		QueryExecution queryExecution = QueryExecutionFactory.create(query,
				calendarData);
		ResultSet resultSet = queryExecution.execSelect();

		Property icaltzdLocation = calendarData.createProperty(icaltzdNs
				+ "location");
		List<Statement> removeStatements = new ArrayList<Statement>();
		List<Statement> addStatements = new ArrayList<Statement>();

		while (resultSet.hasNext()) {
			QuerySolution querySolution = resultSet.next();
			Resource event = querySolution.getResource("event");
			Literal locationLiteral = querySolution.getLiteral("location");

			if (locationLiteral != null) {
				String location = locationLiteral.getLexicalForm();

				String[] locationParts = location.split("\\-");

				String mainLocation = locationParts[0].trim();

				try {
					addStatements.add(new StatementImpl(event, icaltzdLocation,
							locationMap.get(mainLocation)));
				} catch (Exception e) {
					System.err.println(event);
					System.err.println(icaltzdLocation);
					System.err.println(locationMap.get(mainLocation));

					e.printStackTrace();
				}
				removeStatements.add(new StatementImpl(event, icaltzdLocation,
						locationLiteral));

				String[] mainLocationParts = mainLocation.split(" ");
				String mainLocationId = mainLocationParts[0].trim();

				for (int i = 1; i < locationParts.length; i++) {
					String loc = mainLocationId + " " + locationParts[i].trim();
					addStatements.add(new StatementImpl(event, icaltzdLocation,
							locationMap.get(loc)));
				}
			}
		}

		calendarData.remove(removeStatements);
		calendarData.add(addStatements);
	}

	public JSONObject generateJsonModel() {
		JSONObject jsonObject = null;

		InputStream xsltStream = classLoader
				.getResourceAsStream("xslt/json_events.xslt");
		TransformerFactory tFactory = TransformerFactory.newInstance();

		Transformer transformer = null;
		try {
			transformer = tFactory.newTransformer(new StreamSource(xsltStream));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (transformer != null) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			calendarData.write(out);

			InputStream inputStream = new ByteArrayInputStream(
					out.toByteArray());
			try {
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			StreamSource streamSource = new StreamSource(inputStream);

			try {
				out = new ByteArrayOutputStream();
				transformer.transform(streamSource, new StreamResult(out));
				inputStream = new ByteArrayInputStream(out.toByteArray());
				out.close();

				BufferedReader streamReader = new BufferedReader(
						new InputStreamReader(inputStream, "UTF-8"));
				StringBuilder responseStrBuilder = new StringBuilder();

				String line = null;
				while ((line = streamReader.readLine()) != null)
					responseStrBuilder.append(line);

				jsonObject = new JSONObject(responseStrBuilder.toString());
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return jsonObject;
	}

	private ClassLoader getClassLoader() {
		if (classLoader == null)
			classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader;
	}

	public static void main(String[] args) {
		// GenerateAppData generateAppData = new
		// GenerateAppData(FileManager.get().loadModel("eswc2015.rdf"));
		GenerateCalendarData generateAppData = new GenerateCalendarData(
				FileManager.get().loadModel(
						"calendar2015/main-with-calendar2015.rdf"));

		System.out.println("Generating data...");
		String jsonFolder = "json";
		OutputStream outputStream;
		JSONObject json = generateAppData.generateJsonModel();
		if (json != null) {
			try {
				outputStream = new FileOutputStream(jsonFolder + File.separator
						+ "events.json");
				outputStream.write(json.toString().getBytes());
				outputStream.flush();
				outputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
