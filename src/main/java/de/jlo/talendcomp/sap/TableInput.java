package de.jlo.talendcomp.sap;

import java.util.List;

public interface TableInput {

	public void setTableName(String tableName);
	
	public String getTableName();
	
	public void addField(String fieldName);
	
	public void setFilter(String whereCondition);
	
	public void setTableResultFieldDelimiter(String delimiter);
	
	public void setMaxRows(Integer maxRows);
	
	public void setRowsToSkip(Integer rowsToSkip);
	
	public void prepare() throws Exception;

	public void execute() throws Exception;
	
	public boolean next() throws Exception;
	
	public List<String> getCurrentRow();
	
	public String getFunctionDescription();
	
	public String getCurrentRawDataEscaped();
	
	public int getTotalRowCount();
	
	public int getCurrentRowIndex();
	
	
	
}
