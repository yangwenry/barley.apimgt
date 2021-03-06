/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package barley.apimgt.impl.generated.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionDTO implements org.apache.thrift.TBase<ConditionDTO, ConditionDTO._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConditionDTO");

  private static final org.apache.thrift.protocol.TField CONDITION_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("conditionType", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField CONDITION_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("conditionName", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField CONDITION_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("conditionValue", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField IS_INVERTED_FIELD_DESC = new org.apache.thrift.protocol.TField("isInverted", org.apache.thrift.protocol.TType.BOOL, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ConditionDTOStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ConditionDTOTupleSchemeFactory());
  }

  public String conditionType; // optional
  public String conditionName; // optional
  public String conditionValue; // optional
  public boolean isInverted; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CONDITION_TYPE((short)1, "conditionType"),
    CONDITION_NAME((short)2, "conditionName"),
    CONDITION_VALUE((short)3, "conditionValue"),
    IS_INVERTED((short)4, "isInverted");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // CONDITION_TYPE
          return CONDITION_TYPE;
        case 2: // CONDITION_NAME
          return CONDITION_NAME;
        case 3: // CONDITION_VALUE
          return CONDITION_VALUE;
        case 4: // IS_INVERTED
          return IS_INVERTED;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ISINVERTED_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  private _Fields optionals[] = {_Fields.CONDITION_TYPE,_Fields.CONDITION_NAME,_Fields.CONDITION_VALUE,_Fields.IS_INVERTED};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CONDITION_TYPE, new org.apache.thrift.meta_data.FieldMetaData("conditionType", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CONDITION_NAME, new org.apache.thrift.meta_data.FieldMetaData("conditionName", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CONDITION_VALUE, new org.apache.thrift.meta_data.FieldMetaData("conditionValue", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.IS_INVERTED, new org.apache.thrift.meta_data.FieldMetaData("isInverted", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ConditionDTO.class, metaDataMap);
  }

  public ConditionDTO() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ConditionDTO(ConditionDTO other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetConditionType()) {
      this.conditionType = other.conditionType;
    }
    if (other.isSetConditionName()) {
      this.conditionName = other.conditionName;
    }
    if (other.isSetConditionValue()) {
      this.conditionValue = other.conditionValue;
    }
    this.isInverted = other.isInverted;
  }

  public ConditionDTO deepCopy() {
    return new ConditionDTO(this);
  }

  @Override
  public void clear() {
    this.conditionType = null;
    this.conditionName = null;
    this.conditionValue = null;
    setIsInvertedIsSet(false);
    this.isInverted = false;
  }

  public String getConditionType() {
    return this.conditionType;
  }

  public ConditionDTO setConditionType(String conditionType) {
    this.conditionType = conditionType;
    return this;
  }

  public void unsetConditionType() {
    this.conditionType = null;
  }

  /** Returns true if field conditionType is set (has been assigned a value) and false otherwise */
  public boolean isSetConditionType() {
    return this.conditionType != null;
  }

  public void setConditionTypeIsSet(boolean value) {
    if (!value) {
      this.conditionType = null;
    }
  }

  public String getConditionName() {
    return this.conditionName;
  }

  public ConditionDTO setConditionName(String conditionName) {
    this.conditionName = conditionName;
    return this;
  }

  public void unsetConditionName() {
    this.conditionName = null;
  }

  /** Returns true if field conditionName is set (has been assigned a value) and false otherwise */
  public boolean isSetConditionName() {
    return this.conditionName != null;
  }

  public void setConditionNameIsSet(boolean value) {
    if (!value) {
      this.conditionName = null;
    }
  }

  public String getConditionValue() {
    return this.conditionValue;
  }

  public ConditionDTO setConditionValue(String conditionValue) {
    this.conditionValue = conditionValue;
    return this;
  }

  public void unsetConditionValue() {
    this.conditionValue = null;
  }

  /** Returns true if field conditionValue is set (has been assigned a value) and false otherwise */
  public boolean isSetConditionValue() {
    return this.conditionValue != null;
  }

  public void setConditionValueIsSet(boolean value) {
    if (!value) {
      this.conditionValue = null;
    }
  }

  public boolean isIsInverted() {
    return this.isInverted;
  }

  public ConditionDTO setIsInverted(boolean isInverted) {
    this.isInverted = isInverted;
    setIsInvertedIsSet(true);
    return this;
  }

  public void unsetIsInverted() {
    __isset_bit_vector.clear(__ISINVERTED_ISSET_ID);
  }

  /** Returns true if field isInverted is set (has been assigned a value) and false otherwise */
  public boolean isSetIsInverted() {
    return __isset_bit_vector.get(__ISINVERTED_ISSET_ID);
  }

  public void setIsInvertedIsSet(boolean value) {
    __isset_bit_vector.set(__ISINVERTED_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CONDITION_TYPE:
      if (value == null) {
        unsetConditionType();
      } else {
        setConditionType((String)value);
      }
      break;

    case CONDITION_NAME:
      if (value == null) {
        unsetConditionName();
      } else {
        setConditionName((String)value);
      }
      break;

    case CONDITION_VALUE:
      if (value == null) {
        unsetConditionValue();
      } else {
        setConditionValue((String)value);
      }
      break;

    case IS_INVERTED:
      if (value == null) {
        unsetIsInverted();
      } else {
        setIsInverted((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CONDITION_TYPE:
      return getConditionType();

    case CONDITION_NAME:
      return getConditionName();

    case CONDITION_VALUE:
      return getConditionValue();

    case IS_INVERTED:
      return Boolean.valueOf(isIsInverted());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CONDITION_TYPE:
      return isSetConditionType();
    case CONDITION_NAME:
      return isSetConditionName();
    case CONDITION_VALUE:
      return isSetConditionValue();
    case IS_INVERTED:
      return isSetIsInverted();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ConditionDTO)
      return this.equals((ConditionDTO)that);
    return false;
  }

  public boolean equals(ConditionDTO that) {
    if (that == null)
      return false;

    boolean this_present_conditionType = true && this.isSetConditionType();
    boolean that_present_conditionType = true && that.isSetConditionType();
    if (this_present_conditionType || that_present_conditionType) {
      if (!(this_present_conditionType && that_present_conditionType))
        return false;
      if (!this.conditionType.equals(that.conditionType))
        return false;
    }

    boolean this_present_conditionName = true && this.isSetConditionName();
    boolean that_present_conditionName = true && that.isSetConditionName();
    if (this_present_conditionName || that_present_conditionName) {
      if (!(this_present_conditionName && that_present_conditionName))
        return false;
      if (!this.conditionName.equals(that.conditionName))
        return false;
    }

    boolean this_present_conditionValue = true && this.isSetConditionValue();
    boolean that_present_conditionValue = true && that.isSetConditionValue();
    if (this_present_conditionValue || that_present_conditionValue) {
      if (!(this_present_conditionValue && that_present_conditionValue))
        return false;
      if (!this.conditionValue.equals(that.conditionValue))
        return false;
    }

    boolean this_present_isInverted = true && this.isSetIsInverted();
    boolean that_present_isInverted = true && that.isSetIsInverted();
    if (this_present_isInverted || that_present_isInverted) {
      if (!(this_present_isInverted && that_present_isInverted))
        return false;
      if (this.isInverted != that.isInverted)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(ConditionDTO other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ConditionDTO typedOther = (ConditionDTO)other;

    lastComparison = Boolean.valueOf(isSetConditionType()).compareTo(typedOther.isSetConditionType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConditionType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.conditionType, typedOther.conditionType);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetConditionName()).compareTo(typedOther.isSetConditionName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConditionName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.conditionName, typedOther.conditionName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetConditionValue()).compareTo(typedOther.isSetConditionValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConditionValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.conditionValue, typedOther.conditionValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIsInverted()).compareTo(typedOther.isSetIsInverted());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIsInverted()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.isInverted, typedOther.isInverted);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ConditionDTO(");
    boolean first = true;

    if (isSetConditionType()) {
      sb.append("conditionType:");
      if (this.conditionType == null) {
        sb.append("null");
      } else {
        sb.append(this.conditionType);
      }
      first = false;
    }
    if (isSetConditionName()) {
      if (!first) sb.append(", ");
      sb.append("conditionName:");
      if (this.conditionName == null) {
        sb.append("null");
      } else {
        sb.append(this.conditionName);
      }
      first = false;
    }
    if (isSetConditionValue()) {
      if (!first) sb.append(", ");
      sb.append("conditionValue:");
      if (this.conditionValue == null) {
        sb.append("null");
      } else {
        sb.append(this.conditionValue);
      }
      first = false;
    }
    if (isSetIsInverted()) {
      if (!first) sb.append(", ");
      sb.append("isInverted:");
      sb.append(this.isInverted);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ConditionDTOStandardSchemeFactory implements SchemeFactory {
    public ConditionDTOStandardScheme getScheme() {
      return new ConditionDTOStandardScheme();
    }
  }

  private static class ConditionDTOStandardScheme extends StandardScheme<ConditionDTO> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ConditionDTO struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CONDITION_TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.conditionType = iprot.readString();
              struct.setConditionTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // CONDITION_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.conditionName = iprot.readString();
              struct.setConditionNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CONDITION_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.conditionValue = iprot.readString();
              struct.setConditionValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // IS_INVERTED
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.isInverted = iprot.readBool();
              struct.setIsInvertedIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ConditionDTO struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.conditionType != null) {
        if (struct.isSetConditionType()) {
          oprot.writeFieldBegin(CONDITION_TYPE_FIELD_DESC);
          oprot.writeString(struct.conditionType);
          oprot.writeFieldEnd();
        }
      }
      if (struct.conditionName != null) {
        if (struct.isSetConditionName()) {
          oprot.writeFieldBegin(CONDITION_NAME_FIELD_DESC);
          oprot.writeString(struct.conditionName);
          oprot.writeFieldEnd();
        }
      }
      if (struct.conditionValue != null) {
        if (struct.isSetConditionValue()) {
          oprot.writeFieldBegin(CONDITION_VALUE_FIELD_DESC);
          oprot.writeString(struct.conditionValue);
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetIsInverted()) {
        oprot.writeFieldBegin(IS_INVERTED_FIELD_DESC);
        oprot.writeBool(struct.isInverted);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ConditionDTOTupleSchemeFactory implements SchemeFactory {
    public ConditionDTOTupleScheme getScheme() {
      return new ConditionDTOTupleScheme();
    }
  }

  private static class ConditionDTOTupleScheme extends TupleScheme<ConditionDTO> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ConditionDTO struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetConditionType()) {
        optionals.set(0);
      }
      if (struct.isSetConditionName()) {
        optionals.set(1);
      }
      if (struct.isSetConditionValue()) {
        optionals.set(2);
      }
      if (struct.isSetIsInverted()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetConditionType()) {
        oprot.writeString(struct.conditionType);
      }
      if (struct.isSetConditionName()) {
        oprot.writeString(struct.conditionName);
      }
      if (struct.isSetConditionValue()) {
        oprot.writeString(struct.conditionValue);
      }
      if (struct.isSetIsInverted()) {
        oprot.writeBool(struct.isInverted);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ConditionDTO struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.conditionType = iprot.readString();
        struct.setConditionTypeIsSet(true);
      }
      if (incoming.get(1)) {
        struct.conditionName = iprot.readString();
        struct.setConditionNameIsSet(true);
      }
      if (incoming.get(2)) {
        struct.conditionValue = iprot.readString();
        struct.setConditionValueIsSet(true);
      }
      if (incoming.get(3)) {
        struct.isInverted = iprot.readBool();
        struct.setIsInvertedIsSet(true);
      }
    }
  }

}

