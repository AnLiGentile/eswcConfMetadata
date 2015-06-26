package org.eswc.conferences.data.models;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AppJsonModels {

	private JSONObject contacts;
	private JSONObject organizations;
	private JSONObject articles;
	private JSONObject events;
	private JSONObject categories;
	private JSONObject locations;

	public AppJsonModels(JSONObject contacts, JSONObject organizations,
			JSONObject articles, JSONObject events, JSONObject categories,
			JSONObject locations) {
		this.contacts = contacts;
		this.organizations = organizations;
		this.articles = articles;
		this.events = events;
		this.locations = locations;
		this.categories = categories;
	}

	public JSONObject getArticles() {
		return articles;
	}

	public JSONObject getOrganizations() {
		return organizations;
	}

	public JSONObject getContacts() {
		return contacts;
	}

	public JSONObject getEvents() {
		return events;
	}

	public JSONObject getCategories() {
		return categories;
	}

	public JSONObject getLocations() {
		return locations;
	}

	public void write(OutputStream outputStream) throws IOException {

		String newLine = System.getProperty("line.separator");
		BufferedOutputStream out = new BufferedOutputStream(outputStream);
		out.write(("define({" + newLine).getBytes());
		out.flush();

		try {
			out.write(("\"organizations\":"
					+ organizations.getJSONArray("organizations").toString()
					+ "," + newLine).getBytes());
			out.flush();

			out.write(("\"persons\":"
					+ contacts.getJSONArray("persons").toString() + "," + newLine)
					.getBytes());
			out.flush();

			out.write(("\"publications\":"
					+ articles.getJSONArray("publications").toString() + "," + newLine)
					.getBytes());
			out.flush();

			out.write(("\"events\":" + events.getJSONArray("events").toString()
					+ "," + newLine).getBytes());
			out.flush();

			out.write(("\"locations\":"
					+ locations.getJSONArray("locations").toString() + "," + newLine)
					.getBytes());
			out.flush();

			out.write(("\"categories\":"
					+ categories.getJSONArray("categories").toString() + newLine)
					.getBytes());
			out.flush();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		out.write("});".getBytes());
		out.flush();
		out.close();

	}

	public void writePureJson(OutputStream outputStream) throws IOException {

		String newLine = System.getProperty("line.separator");
		BufferedOutputStream out = new BufferedOutputStream(outputStream);
		out.write(("{" + newLine).getBytes());
		out.flush();

		try {
			out.write(("\"organizations\":"
					+ organizations.getJSONArray("organizations").toString()
					+ "," + newLine).getBytes());
			out.flush();

			out.write(("\"persons\":"
					+ contacts.getJSONArray("persons").toString() + "," + newLine)
					.getBytes());
			out.flush();

			out.write(("\"publications\":"
					+ articles.getJSONArray("publications").toString() + "," + newLine)
					.getBytes());
			out.flush();

			out.write(("\"events\":" + events.getJSONArray("events").toString()
					+ "," + newLine).getBytes());
			out.flush();

			out.write(("\"locations\":"
					+ locations.getJSONArray("locations").toString() + "," + newLine)
					.getBytes());
			out.flush();

			out.write(("\"categories\":"
					+ categories.getJSONArray("categories").toString() + newLine)
					.getBytes());
			out.flush();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		out.write("}".getBytes());
		out.flush();
		out.close();

	}

}
