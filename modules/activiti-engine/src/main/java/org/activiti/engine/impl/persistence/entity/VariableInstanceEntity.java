/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tom Baeyens
 * @author Marcus Klimstra
 */
public class VariableInstanceEntity implements ValueFields, PersistentObject, HasRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String name;
  protected VariableType type;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;

  protected Long longValue;
  protected Double doubleValue; 
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayValueId;

  protected Object cachedValue;
  boolean forcedUpdate;
  
  // Default constructor for SQL mapping
  protected VariableInstanceEntity() {
  }

  public static VariableInstanceEntity createAndInsert(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = create(name, type, value);

    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(variableInstance);
  
    return variableInstance;
  }
  
  public static VariableInstanceEntity create(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.type = type;
    variableInstance.setValue(value);
    
    return variableInstance;
  }

  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
    this.forcedUpdate = true;
  }

  public void delete() {
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
    
    if (byteArrayValueId != null) {
      Context
        .getCommandContext()
        .getByteArrayEntityManager()
        .deleteByteArrayById(byteArrayValueId);
    }
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    if (longValue != null) {
      persistentState.put("longValue", longValue);
    }
    if (doubleValue != null) {
      persistentState.put("doubleValue", doubleValue);
    }
    if (textValue != null) {
      persistentState.put("textValue", textValue);
    }
    if (textValue2 != null) {
      persistentState.put("textValue2", textValue2);
    }
    if (byteArrayValueId != null) {
      persistentState.put("byteArrayValueId", byteArrayValueId);
    }
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  // lazy initialized relations ///////////////////////////////////////////////

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  // byte array value /////////////////////////////////////////////////////////
  
  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity, 
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity 

  @Override
  public byte[] getBytes() {
    ByteArrayEntity byteArrayValue = getByteArrayEntity();
    return (byteArrayValue != null ? byteArrayValue.getBytes() : null);
  }

  @Override
  public void setBytes(byte[] bytes) {
    if (bytes == null) {
      if (byteArrayValueId != null) {
        Context.getCommandContext()
          .getByteArrayEntityManager()
          .deleteByteArrayById(byteArrayValueId);
        byteArrayValueId = null;
      }
    }
    else {
      if (byteArrayValueId == null) {
        byteArrayValue = ByteArrayEntity.createAndInsert("var-" + name, bytes);
        byteArrayValueId = byteArrayValue.getId();
      }
      else {
        ByteArrayEntity byteArrayValue = getByteArrayEntity();
        byteArrayValue.setBytes(bytes);
      }
    }
  }
  
  @Override @Deprecated
  public ByteArrayEntity getByteArrayValue() {
    return getByteArrayEntity();
  }
  
  @Override @Deprecated
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }

  @Override @Deprecated
  public void setByteArrayValue(byte[] bytes) {
    setBytes(bytes);
  }

  private ByteArrayEntity getByteArrayEntity() {
    if (byteArrayValueId != null && byteArrayValue == null) {
      byteArrayValue = Context.getCommandContext()
        .getByteArrayEntityManager()
        .findById(byteArrayValueId);
    }
    return byteArrayValue;
  }

  // value ////////////////////////////////////////////////////////////////////

  public Object getValue() {
    if (!type.isCachable() || cachedValue==null) {
      cachedValue = type.getValue(this);
    }
    return cachedValue;
  }

  public void setValue(Object value) {
    type.setValue(value, this);
    cachedValue = value;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public VariableType getType() {
    return type;
  }
  public void setType(VariableType type) {
    this.type = type;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getTaskId() {
    return taskId;
  }
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  public String getExecutionId() {
    return executionId;
  }
  
  public Long getLongValue() {
    return longValue;
  }
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }
  
  public Double getDoubleValue() {
    return doubleValue;
  }
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }
  
  public String getTextValue() {
    return textValue;
  }
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getTextValue2() {
    return textValue2;
  }
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  public Object getCachedValue() {
    return cachedValue;
  }
  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }

  // misc methods /////////////////////////////////////////////////////////////

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("VariableInstanceEntity[");
    sb.append("id=").append(id);
    sb.append(", name=").append(name);
    sb.append(", type=").append(type != null ? type.getTypeName() : "null");
    if (longValue != null) {
      sb.append(", longValue=").append(longValue);
    }
    if (doubleValue != null) {
      sb.append(", doubleValue=").append(doubleValue);
    }
    if (textValue != null) {
      sb.append(", textValue=").append(StringUtils.abbreviate(textValue, 40));
    }
    if (textValue2 != null) {
      sb.append(", textValue2=").append(StringUtils.abbreviate(textValue2, 40));
    }
    if (byteArrayValueId != null) {
      sb.append(", byteArrayValueId=").append(byteArrayValueId);
    }
    sb.append("]");
    return sb.toString();
  }
  
}
