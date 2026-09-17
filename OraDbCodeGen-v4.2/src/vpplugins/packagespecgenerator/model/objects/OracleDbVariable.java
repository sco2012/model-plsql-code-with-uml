package vpplugins.packagespecgenerator.model.objects;

public class OracleDbVariable extends OracleDbEntity {
   private String dataType;
   private String defaultValue;

   public String getDataType() {
      return this.dataType == null ? "" : this.dataType;
   }

   public void setDataType(String dataType) {
      this.dataType = dataType;
   }

   public String getDefaultValue() {
      return this.defaultValue == null ? "" : this.defaultValue;
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }
}
