package vpplugins.packagespecgenerator.ui.builders;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SearchFunction {
   public JTextField SearchBar = new JTextField(15);
   public JButton NextSearchResultButton = new JButton("Next");
   public JButton PreviousSearchResultButton = new JButton("Previous");
   public JLabel SearchMessage = new JLabel();
   public JTextArea selectedArea;
   public int currentSearchResultIndex = -1;
   public int selectedResult = 0;
   @SuppressWarnings({ "unchecked", "rawtypes" })
   private ArrayList<Integer> searchResults = new ArrayList();

   private JTextArea getSelectedArea() {
      return this.selectedArea;
   }

   public void setSelectedArea(JTextArea selectedArea) {
      this.selectedArea = selectedArea;
   }

   public SearchFunction() {
      this.SearchBar.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) {
            SearchFunction.this.getMatches();
         }

         public void removeUpdate(DocumentEvent e) {
            SearchFunction.this.getMatches();
         }

         public void changedUpdate(DocumentEvent e) {
         }
      });
      this.NextSearchResultButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SearchFunction.this.nextResult();
         }
      });
      this.PreviousSearchResultButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SearchFunction.this.previousResult();
         }
      });
   }

   public ArrayList<Integer> getMatches() {
      String textName = this.getSelectedArea().getText().toLowerCase();
      String search = this.SearchBar.getText().toLowerCase();
      this.searchResults.clear();
      if (search.length() <= 0) {
         return null;
      } else {
         for(int i = -1; (i = textName.indexOf(search, i + 1)) != -1; ++i) {
            this.searchResults.add(i);
         }

         if (this.SearchBar.getText().length() == 0) {
            this.SearchMessage.setVisible(false);
         } else {
            this.SearchMessage.setVisible(true);
            this.SearchMessage.setText(Integer.toString(this.selectedResult) + "/" + this.searchResults.size());
         }

         return this.searchResults;
      }
   }

   private void nextResult() {
      ++this.currentSearchResultIndex;
      ++this.selectedResult;
      if (this.currentSearchResultIndex > this.getMatches().size() - 1) {
         this.currentSearchResultIndex = 0;
         this.selectedResult = 1;
      }

      String find = this.SearchBar.getText().toLowerCase();
      int i = (Integer)this.getMatches().get(this.currentSearchResultIndex);
      this.getSelectedArea().requestFocusInWindow();
      if (find.length() > 0) {
         int findLength = find.length();

         try {
            if (this.currentSearchResultIndex > this.getMatches().size()) {
               i = 0;
            }

            @SuppressWarnings("deprecation")
            Rectangle viewRect = this.getSelectedArea().modelToView(i);
            this.getSelectedArea().scrollRectToVisible(viewRect);
            this.getSelectedArea().setCaretPosition(i + findLength);
            this.getSelectedArea().moveCaretPosition(i);
         } catch (Exception err) {
            err.printStackTrace();
         }
      }

   }

   private void previousResult() {
      --this.currentSearchResultIndex;
      --this.selectedResult;
      if (this.currentSearchResultIndex < 0) {
         this.currentSearchResultIndex = this.getMatches().size() - 1;
         this.selectedResult = this.getMatches().size();
      }

      String find = this.SearchBar.getText().toLowerCase();
      int i = (Integer)this.getMatches().get(this.currentSearchResultIndex);
      this.getSelectedArea().requestFocusInWindow();
      if (find.length() > 0) {
         int findLength = find.length();

         try {
            @SuppressWarnings("deprecation")
            Rectangle viewRect = this.getSelectedArea().modelToView(i);
            this.getSelectedArea().scrollRectToVisible(viewRect);
            this.getSelectedArea().setCaretPosition(i + findLength);
            this.getSelectedArea().moveCaretPosition(i);
         } catch (Exception err) {
            err.printStackTrace();
         }
      }

   }
}
