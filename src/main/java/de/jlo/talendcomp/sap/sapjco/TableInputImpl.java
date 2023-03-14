package de.jlo.talendcomp.sap.sapjco;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoParameterFieldIterator;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.rt.DefaultTable;

import de.jlo.talendcomp.sap.TableInput;
import de.jlo.talendcomp.sap.TextSplitter;

/**
 * Copyright 2023 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TableInputImpl implements TableInput {

	private JCoDestination destination = null;
	private String filter = null;
	private String tableName = null;
	private String tableResultFieldDelimiter = "\b"; // according to the String.split method this is faster!
	private List<String> listFields = new ArrayList<>();
	private com.sap.conn.jco.JCoTable resultTable = null;
	private JCoFunction function = null;
	private List<String> currentRow = null;
	private String currentRawData = null;
	private int totalRowCount = 0;
	private int currentRowIndex = -1;
	private Integer rowCount = null;
	private Integer rowSkip = null;
	private String functionDescription = null;
	private char filterPartSeparator = ';';
	
	/**
	 * Create an instance if TableInput
	 * @param destination the actual connection to SAP server
	 */
	public TableInputImpl(JCoDestination destination) {
		if (destination == null) {
			throw new IllegalArgumentException("SAP destination cannot be null");
		}
		this.destination = destination;
	}
	
	/**
	 * Add a source table field
	 * @param name
	 */
	@Override
	public void addField(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Field name cannot be null or empty");
		}
		name = name.toUpperCase();
		if (listFields.contains(name) == false) {
			listFields.add(name);
		} else {
			throw new IllegalArgumentException("Field " + name + " already exist in the field list");
		}
	}
	
	@Override
	public void prepare() throws Exception {
		if (destination == null) {
			throw new IllegalStateException("No SAP destination set!");
		}
		JCoRepository repository = destination.getRepository();
		JCoFunctionTemplate functionTemplate_tSAPInput_1 = repository.getFunctionTemplate("RFC_READ_TABLE");
		if (functionTemplate_tSAPInput_1 == null) {
			com.sap.conn.jco.JCoContext.end(destination);
			throw new Exception("Function RFC_READ_TABLE does not exist or cannot be reached");
		}
		function = functionTemplate_tSAPInput_1.getFunction();
		JCoParameterList tableParameterList = function.getTableParameterList();
		// add where condition
		List<String> filterPartList = TextSplitter.split(filter, filterPartSeparator);
		if (filterPartList.size() > 0) {
			JCoTable tableInputOptions = tableParameterList.getTable("OPTIONS");
			tableInputOptions.appendRows(filterPartList.size());
			tableInputOptions.firstRow();
			for (String part : filterPartList) {
				if (part.length() > 72) {
					throw new Exception("The filter expression: <" + filter + "> contains a part with a length of " + part.length() + " which is larger than 72 chars.\nAffected part is: <"+ part + ">.\nPlease split the filter with the delimiter: <" + filterPartSeparator + "> into parts smaller than 72 chars");
				}
				tableInputOptions.setValue("TEXT", part);
				tableInputOptions.nextRow();
			}
		} 
		// add fields
		if (listFields.isEmpty()) {
			throw new Exception("List of expected fields cannot not be empty");
		}
		JCoTable tableInputFields = tableParameterList.getTable("FIELDS");
		tableInputFields.appendRows(listFields.size());
		tableInputFields.firstRow();
		for (String fieldName : listFields) {
			tableInputFields.setValue("FIELDNAME", fieldName);
			tableInputFields.nextRow();
		}
	}
	
	/**
	 * execute the query
	 * @throws Exception
	 */
	@Override
	public void execute() throws Exception {
		if (function == null) {
			throw new IllegalStateException("Function not prepared. Please call prepare() before.");
		}
		JCoParameterList importParameterList = function.getImportParameterList();
		// add parameter for source table
		if (tableName == null || tableName.trim().isEmpty()) {
			throw new Exception("Source table name cannot be null or empty!");
		}
		importParameterList.setValue("QUERY_TABLE", tableName);
		// delimiter to later separate the result fields
		importParameterList.setValue("DELIMITER", tableResultFieldDelimiter);
		// setup offset and limit
		if (rowCount != null && rowCount > 0) {
			importParameterList.setValue("ROWCOUNT", String.valueOf(rowCount));
		}
		if (rowSkip != null && rowSkip > 0) {
			importParameterList.setValue("ROWSKIPS", String.valueOf(rowSkip));
		}
		// execute the query
		try {
			JCoContext.begin(destination);
			function.execute(destination);
		} catch (java.lang.Exception e) {
			throw new Exception("Execute query failed: " + e.getMessage() + " using function:\n" + getFunctionDescription(function), e);
		} finally {
			JCoContext.end(destination);
		}
		// some meta information are only available after running the function
		functionDescription = getFunctionDescription(function);
		resultTable = function.getTableParameterList().getTable("DATA");
		if (resultTable == null) {
			throw new Exception("Exceute query returned no DATA table");
		}
		totalRowCount = resultTable.getNumRows();
		currentRowIndex = -1;
	}
	
	/**
	 * Iterate through the result data
	 * @return true if there is at least one record left
	 * @throws Exception
	 */
	@Override
	public boolean next() throws Exception {
		if (resultTable == null ) {
			throw new IllegalStateException("No query was executed before! Call execute() before");
		}
		if (currentRow != null) {
			currentRow.clear(); // help the GC a bit
		}
		currentRow = null; // reset currentRow
		currentRowIndex++; // was initialized with -1
		if (currentRowIndex == totalRowCount) {
			return false;
		} else {
			currentRawData = resultTable.getString("WA");
			if (currentRawData == null || currentRawData.trim().isEmpty()) {
				throw new Exception("Got no result data string");
			}
			fillCurrentRow();
			resultTable.nextRow();
			return true;
		}
	}
	
	public void fillCurrentRow() {
		// the last field will be ignored if empty.
		// to prevent that, add an space at the end if the last char is the delimiter
		// the space value will be ignored later on
		// the space will be later converted back to null in the TypeUtil
		if (currentRawData.endsWith(tableResultFieldDelimiter)) {
			currentRawData = currentRawData + " ";
		}
		String[] array = currentRawData.split(tableResultFieldDelimiter);
		currentRow = new ArrayList<>();
		for (String v : array) {
			if (v == null) {
				v = "";
			} else {
				v = v.trim();
			}
			currentRow.add(v);
		}
		if (currentRow.size() != listFields.size()) {
			throw new IllegalStateException("The received field count: " + currentRow.size() + " does not fit to the expected field count: " + listFields.size() + " in the current received line: <" + getCurrentRawDataEscaped() + "> , Query:\n" + getFunctionDescription());
		}
	}

	@Override
	public List<String> getCurrentRow() {
		if (currentRow == null) {
			throw new IllegalStateException("Table pointer is not moved to the next row");
		}
		return currentRow;
	}
	
	/**
	 * Get the raw field content addressed by its index. 
	 * @param   field index starts with 1
	 * @return String value
	 */
	public String getStringValue(int index) {
		if (currentRow == null) {
			throw new IllegalStateException("Table pointer is not moved to the next row");
		}
		if (index > currentRow.size()) {
			throw new IllegalArgumentException("Index cannot be greater than the current field count: " + currentRow.size());
		}
		if (index < 1) {
			throw new IllegalArgumentException("Index cannot less then 1. Index starts with 1");
		}
		return currentRow.get(index - 1);
	}
	
	public int getCurrentRowIndex() {
		return currentRowIndex;
	}
	
	@Override
	public String getCurrentRawDataEscaped() {
		return StringEscapeUtils.escapeJava(currentRawData);
	}

	public String getWhereCondition() {
		return filter;
	}

	@Override
	public void setFilter(String whereCondition) {
		if (whereCondition != null) {
			this.filter = whereCondition.trim();
		}
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableResultFieldDelimiter() {
		return tableResultFieldDelimiter;
	}

	public void setTableResultFieldDelimiter(String tableResultFieldDelimiter) {
		this.tableResultFieldDelimiter = tableResultFieldDelimiter;
	}

	@Override
	public int getTotalRowCount() {
		return totalRowCount;
	}

	public Integer getMaxRows() {
		return rowCount;
	}
	
	@Override
	public void setMaxRows(Integer rowCount) {
		this.rowCount = rowCount;
	}

	public Integer getRowSkip() {
		return rowSkip;
	}

	@Override
	public void setRowsToSkip(Integer rowSkip) {
		this.rowSkip = rowSkip;
	}
	
	/**
	 * Builds a readable text representation of the function (the actual query)
	 * @param function
	 * @return
	 */
	private static String getFunctionDescription(JCoFunction function) {
		StringBuilder sb = new StringBuilder();
		sb.append("Function: ");
		sb.append(function.getName());
		sb.append("\n");
		sb.append("Import parameters:\n");
		JCoParameterList il = function.getImportParameterList();
		JCoParameterFieldIterator it = il.getParameterFieldIterator();
		while (it.hasNextField()) {
			JCoField f = it.nextField();
			sb.append(f.getName());
			sb.append("=");
			sb.append(StringEscapeUtils.escapeJava(String.valueOf(f.getValue())));
			sb.append("\n");
		}
		sb.append("Table parameters:\n");
		JCoParameterList tl = function.getTableParameterList();
		JCoParameterFieldIterator tt = tl.getParameterFieldIterator();
		while (tt.hasNextField()) {
			JCoField f = tt.nextField();
			String name = f.getName();
			if ("DATA".equals(name)) {
				sb.append(name);
				sb.append("=<will contain the results of the query>\n");
			} else {
				sb.append(name);
				sb.append("=");
				Object v = f.getValue();
				if (v instanceof DefaultTable) {
					DefaultTable t = (DefaultTable) v;
					sb.append("\n");
					sb.append(t);
				} else {
					sb.append(f.getValue());
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	@Override
	public String getFunctionDescription() {
		return functionDescription;
	}

}
