package vpplugins.packagespecgenerator.model.objects;

import java.util.LinkedList;

public class OracleDbRefCursor extends OracleDbEntity {
   @SuppressWarnings({ "unchecked", "rawtypes" })
   private LinkedList<OracleDbRefCursor.OracleDbRefCursorColumn> columns = new LinkedList();
   private boolean returnsMultipleRows = false;

   public LinkedList<OracleDbRefCursor.OracleDbRefCursorColumn> getColumns() {
      return this.columns;
   }

   public void addColumns(LinkedList<OracleDbRefCursor.OracleDbRefCursorColumn> columns) {
      this.columns.addAll(columns);
   }

   public void addColumn(OracleDbRefCursor.OracleDbRefCursorColumn column) {
      this.columns.add(column);
   }

   public boolean isReturnsMultipleRows() {
      return this.returnsMultipleRows;
   }

   public void setReturnsMultipleRows(boolean returnsMultipleRows) {
      this.returnsMultipleRows = returnsMultipleRows;
   }

   public static class OracleDbRefCursorColumn extends OracleDbEntity {
      private String dataType;

      public String getDataType() {
         return this.dataType == null ? "" : this.dataType;
      }

      public void setDataType(String dataType) {
         this.dataType = dataType;
      }
   }
}
