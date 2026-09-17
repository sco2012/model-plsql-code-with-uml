package vpplugins.packagespecgenerator.model.objects;

import vpplugins.packagespecgenerator.util.CodeFormattingUtils;

public abstract class OracleDbEntity {
   private String owningSchema;
   private String name;
   private String description;
   private String elementLocation;
   private String specialStereoTypeName;

   public String getElementLocation() {
      return this.elementLocation;
   }

   public void setElementLocation(String elementLocation) {
      this.elementLocation = elementLocation;
   }

   public String getOwningSchema() {
      return this.owningSchema == null ? "" : this.owningSchema;
   }

   public void setOwningSchema(String owningSchema) {
      this.owningSchema = owningSchema;
   }

   public String getName() {
      return this.name == null ? "" : this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return this.description == null ? "" : this.description;
   }

   public void setDescription(String description) {
      this.description = CodeFormattingUtils.stripTrailingSpacesAndNewlines(description);
   }

   public String getSpecialStereoTypeName() {
      return this.specialStereoTypeName == null ? "" : this.specialStereoTypeName;
   }

   public void setSpecialStereoTypeName(String specialStereoTypeName) {
      this.specialStereoTypeName = specialStereoTypeName;
   }


}
