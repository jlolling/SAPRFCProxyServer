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
package de.jlo.talendcomp.sap;

import java.util.List;

public interface TableInput {

	public void setTableName(String tableName);
	
	public String getTableName();
	
	public void addField(String fieldName);
	
	public void setFilter(String whereCondition);
	
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
